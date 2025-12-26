package com.example.kimivoice

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 网络请求工具类 - 封装Moonshot API调用
 * 功能：
 * 1. 语音转文字（Whisper API）
 * 2. AI对话（Chat API）
 * 3. 支持重试机制
 * 4. 完善的异常处理
 */
object NetworkUtils {
    private val gson = Gson()
    
    // JSON媒体类型
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    
    /**
     * 创建OkHttpClient（带日志和重试）
     */
    private fun createHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d("HTTP: $message")
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(KimiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(KimiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(KimiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    private val httpClient by lazy { createHttpClient() }
    
    /**
     * 语音转文字请求响应
     */
    data class TranscriptionResponse(
        @SerializedName("text") val text: String,
        @SerializedName("duration") val duration: Double? = null
    )
    
    /**
     * AI对话请求体
     */
    data class ChatRequest(
        @SerializedName("model") val model: String = "moonshot-v1-8k",
        @SerializedName("messages") val messages: List<Message>,
        @SerializedName("temperature") val temperature: Double = 0.3
    )
    
    data class Message(
        @SerializedName("role") val role: String,
        @SerializedName("content") val content: String
    )
    
    /**
     * AI对话响应
     */
    data class ChatResponse(
        @SerializedName("choices") val choices: List<Choice>,
        @SerializedName("usage") val usage: Usage? = null
    )
    
    data class Choice(
        @SerializedName("message") val message: Message,
        @SerializedName("finish_reason") val finishReason: String
    )
    
    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int,
        @SerializedName("completion_tokens") val completionTokens: Int,
        @SerializedName("total_tokens") val totalTokens: Int
    )
    
    /**
     * 错误响应
     */
    data class ErrorResponse(
        @SerializedName("error") val error: ErrorDetail
    )
    
    data class ErrorDetail(
        @SerializedName("message") val message: String,
        @SerializedName("type") val type: String? = null
    )
    
    /**
     * 网络请求结果封装
     */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    }
    
    /**
     * 语音转文字（Whisper API）
     * @param audioFile 音频文件（WAV格式）
     * @param apiKey Moonshot API Key
     * @return 转写结果
     */
    suspend fun transcribeAudio(audioFile: File, apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                return@withContext Result.Error("音频文件不存在或为空")
            }
            
            if (apiKey.isEmpty()) {
                return@withContext Result.Error("API Key为空")
            }
            
            Timber.d("开始语音转文字，文件大小: ${audioFile.length()} 字节")
            
            // 构建multipart请求
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    audioFile.name,
                    audioFile.asRequestBody("audio/wav".toMediaType())
                )
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("response_format", "json")
                .build()
            
            val request = Request.Builder()
                .url(KimiConstants.MOONSHOT_TRANSCRIBE_URL)
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()
            
            // 执行请求（带重试）
            var lastException: Exception? = null
            repeat(KimiConstants.MAX_RETRY_COUNT + 1) { attempt ->
                try {
                    val response = httpClient.newCall(request).execute()
                    return@withContext handleTranscriptionResponse(response)
                } catch (e: IOException) {
                    lastException = e
                    if (attempt < KimiConstants.MAX_RETRY_COUNT) {
                        Timber.w("请求失败，重试 ${attempt + 1}/${KimiConstants.MAX_RETRY_COUNT}")
                        Thread.sleep(1000L * (attempt + 1)) // 递增延迟
                    }
                }
            }
            
            Result.Error("网络请求失败: ${lastException?.message}", null)
        } catch (e: Exception) {
            Timber.e(e, "语音转文字异常")
            Result.Error("语音转文字失败: ${e.message}")
        }
    }
    
    /**
     * 处理转写API响应
     */
    private fun handleTranscriptionResponse(response: Response): Result<String> {
        val body = response.body?.string() ?: ""
        
        return if (response.isSuccessful) {
            try {
                val result = gson.fromJson(body, TranscriptionResponse::class.java)
                if (result.text.isNotEmpty()) {
                    Timber.d("转写成功: ${result.text}")
                    Result.Success(result.text)
                } else {
                    Result.Error("转写结果为空")
                }
            } catch (e: Exception) {
                Timber.e(e, "解析转写响应失败")
                Result.Error("解析响应失败: ${e.message}")
            }
        } else {
            val errorMsg = try {
                val error = gson.fromJson(body, ErrorResponse::class.java)
                error.error.message
            } catch (e: Exception) {
                response.message
            }
            Timber.e("转写失败 [${response.code}]: $errorMsg")
            Result.Error(errorMsg, response.code)
        }
    }
    
    /**
     * AI对话（Chat API）
     * @param userMessage 用户消息
     * @param apiKey Moonshot API Key
     * @param systemPrompt 系统提示词
     * @return AI回复
     */
    suspend fun chatWithAI(
        userMessage: String,
        apiKey: String,
        systemPrompt: String = "你是Kimi智能助手，请将用户的语音指令转换为可执行的shell命令。只返回命令本身，不要有任何解释。"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (userMessage.isEmpty()) {
                return@withContext Result.Error("消息内容为空")
            }
            
            if (apiKey.isEmpty()) {
                return@withContext Result.Error("API Key为空")
            }
            
            Timber.d("发送AI对话: $userMessage")
            
            // 构建请求体
            val chatRequest = ChatRequest(
                messages = listOf(
                    Message("system", systemPrompt),
                    Message("user", userMessage)
                ),
                temperature = 0.3
            )
            
            val requestBody = gson.toJson(chatRequest).toRequestBody(JSON_MEDIA_TYPE)
            
            val request = Request.Builder()
                .url(KimiConstants.MOONSHOT_CHAT_URL)
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()
            
            // 执行请求（带重试）
            var lastException: Exception? = null
            repeat(KimiConstants.MAX_RETRY_COUNT + 1) { attempt ->
                try {
                    val response = httpClient.newCall(request).execute()
                    return@withContext handleChatResponse(response)
                } catch (e: IOException) {
                    lastException = e
                    if (attempt < KimiConstants.MAX_RETRY_COUNT) {
                        Timber.w("请求失败，重试 ${attempt + 1}/${KimiConstants.MAX_RETRY_COUNT}")
                        Thread.sleep(1000L * (attempt + 1))
                    }
                }
            }
            
            Result.Error("网络请求失败: ${lastException?.message}", null)
        } catch (e: Exception) {
            Timber.e(e, "AI对话异常")
            Result.Error("AI对话失败: ${e.message}")
        }
    }
    
    /**
     * 处理对话API响应
     */
    private fun handleChatResponse(response: Response): Result<String> {
        val body = response.body?.string() ?: ""
        
        return if (response.isSuccessful) {
            try {
                val result = gson.fromJson(body, ChatResponse::class.java)
                val reply = result.choices.firstOrNull()?.message?.content ?: ""
                if (reply.isNotEmpty()) {
                    Timber.d("AI回复: $reply")
                    Result.Success(reply)
                } else {
                    Result.Error("AI回复为空")
                }
            } catch (e: Exception) {
                Timber.e(e, "解析对话响应失败")
                Result.Error("解析响应失败: ${e.message}")
            }
        } else {
            val errorMsg = try {
                val error = gson.fromJson(body, ErrorResponse::class.java)
                error.error.message
            } catch (e: Exception) {
                response.message
            }
            Timber.e("对话失败 [${response.code}]: $errorMsg")
            Result.Error(errorMsg, response.code)
        }
    }
}

package com.example.kimivoice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * 语音监听服务（完整实现版 + TTS语音回复）
 * 功能：
 * 1. 实时录音
 * 2. VAD（语音活动检测）
 * 3. 语音转文字
 * 4. AI指令解析
 * 5. 指令执行
 * 6. TTS语音回复
 */
class ListenService : LifecycleService() {
    // 通知渠道ID
    private val CHANNEL_ID = "KimiVoice_Channel"
    private val NOTIFICATION_ID = KimiConstants.NOTIFICATION_ID
    
    // 音频录制
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    
    // 配置参数
    private var energyThreshold = KimiConstants.DEFAULT_ENERGY_THRESHOLD
    private var apiKey = ""
    
    // 缓冲区
    private var bufferSize = 0
    
    // VAD状态
    private var isSpeaking = false
    private var silenceFrames = 0
    private val maxSilenceFrames = 15 // 约15帧静音后结束
    private val minSpeechFrames = 5 // 至少5帧语音才开始记录
    private var speechFrames = 0
    private val audioBuffer = mutableListOf<ShortArray>()
    
    // TTS 语音合成
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("ListenService创建")
        
        // 创建通知渠道
        createNotificationChannel()
        
        // 启动为前台服务
        startForeground(NOTIFICATION_ID, createNotification("初始化中..."))
        
        // 加载配置
        loadConfiguration()
        
        // 初始化TTS
        initializeTTS()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("ListenService启动")
        
        // 检查API Key
        if (apiKey.isEmpty()) {
            Timber.e("API Key未配置，服务无法启动")
            updateNotification("错误：API Key未配置")
            stopSelf()
            return START_NOT_STICKY
        }
        
        // 启动录音
        startRecording()
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("ListenService销毁")
        
        // 停止录音
        stopRecording()
        
        // 清理缓存
        AudioUtils.clearAudioCache(this)
        
        // 释放TTS
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
    
    /**
     * 加载配置
     */
    private fun loadConfiguration() {
        try {
            // 读取API Key
            apiKey = SecurityUtils.getApiKey(this)
            
            // 读取能量阈值
            energyThreshold = getSharedPreferences("kimi_prefs", MODE_PRIVATE)
                .getInt("energy_threshold", KimiConstants.DEFAULT_ENERGY_THRESHOLD)
            
            Timber.d("配置加载成功: threshold=$energyThreshold")
        } catch (e: Exception) {
            Timber.e(e, "加载配置失败")
        }
    }
    
    /**
     * 初始化TTS
     */
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && 
                          result != TextToSpeech.LANG_NOT_SUPPORTED
                
                if (ttsReady) {
                    Timber.d("TTS初始化成功")
                    // 设置语速（可选）
                    textToSpeech?.setSpeechRate(1.0f)
                } else {
                    Timber.w("TTS中文语言不支持")
                }
            } else {
                Timber.e("TTS初始化失败")
            }
        }
    }
    
    /**
     * 语音播报
     */
    private fun speak(text: String) {
        if (ttsReady && textToSpeech != null) {
            Timber.d("TTS播报: $text")
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Timber.w("TTS未就绪，无法播报")
        }
    }
    
    /**
     * 启动录音
     */
    private fun startRecording() {
        if (isRecording) {
            Timber.w("录音已在进行中")
            return
        }
        
        try {
            // 计算缓冲区大小
            bufferSize = AudioRecord.getMinBufferSize(
                KimiConstants.AUDIO_SAMPLE_RATE,
                KimiConstants.AUDIO_CHANNEL,
                KimiConstants.AUDIO_ENCODING
            ) * KimiConstants.RECORD_BUFFER_MULTIPLIER
            
            if (bufferSize <= 0) {
                Timber.e("无效的缓冲区大小: $bufferSize")
                updateNotification("错误：音频配置失败")
                return
            }
            
            // 创建AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                KimiConstants.AUDIO_SAMPLE_RATE,
                KimiConstants.AUDIO_CHANNEL,
                KimiConstants.AUDIO_ENCODING,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Timber.e("AudioRecord初始化失败")
                updateNotification("错误：麦克风无法访问")
                return
            }
            
            // 开始录音
            audioRecord?.startRecording()
            isRecording = true
            
            updateNotification("正在监听...")
            Timber.d("录音已启动，缓冲区大小: $bufferSize")
            
            // 开始录音循环
            startRecordingLoop()
        } catch (e: SecurityException) {
            Timber.e(e, "缺少录音权限")
            updateNotification("错误：缺少录音权限")
        } catch (e: Exception) {
            Timber.e(e, "启动录音失败")
            updateNotification("错误：${e.message}")
        }
    }
    
    /**
     * 录音循环（带VAD检测）
     */
    private fun startRecordingLoop() {
        recordingJob = lifecycleScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(bufferSize / 2)
            
            while (isRecording && isActive) {
                try {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    if (readSize > 0) {
                        processAudioFrame(buffer.copyOf(readSize))
                    } else if (readSize < 0) {
                        Timber.e("录音错误: $readSize")
                        break
                    }
                } catch (e: Exception) {
                    Timber.e(e, "录音循环异常")
                    break
                }
            }
        }
    }
    
    /**
     * 处理音频帧（VAD检测）
     */
    private suspend fun processAudioFrame(frame: ShortArray) {
        val energy = AudioUtils.calculateAudioEnergy(frame)
        val isSpeech = energy > energyThreshold
        
        if (isSpeech) {
            // 检测到语音
            speechFrames++
            silenceFrames = 0
            
            if (!isSpeaking && speechFrames >= minSpeechFrames) {
                // 开始记录
                isSpeaking = true
                audioBuffer.clear()
                withContext(Dispatchers.Main) {
                    updateNotification("正在记录...")
                }
                Timber.d("检测到语音，开始记录")
            }
            
            if (isSpeaking) {
                audioBuffer.add(frame)
            }
        } else {
            // 检测到静音
            speechFrames = 0
            
            if (isSpeaking) {
                silenceFrames++
                audioBuffer.add(frame) // 保留静音帧，避免截断
                
                if (silenceFrames >= maxSilenceFrames) {
                    // 结束记录
                    isSpeaking = false
                    silenceFrames = 0
                    speechFrames = 0
                    
                    withContext(Dispatchers.Main) {
                        updateNotification("处理中...")
                    }
                    Timber.d("语音结束，开始处理")
                    
                    // 处理录音
                    processRecording()
                }
            }
        }
    }
    
    /**
     * 处理录音（转文字+AI解析+执行）
     */
    private suspend fun processRecording() {
        if (audioBuffer.isEmpty()) {
            Timber.w("音频缓冲区为空")
            withContext(Dispatchers.Main) {
                updateNotification("正在监听...")
            }
            return
        }
        
        try {
            // 合并音频帧
            val totalSize = audioBuffer.sumOf { it.size }
            val mergedData = ShortArray(totalSize)
            var offset = 0
            audioBuffer.forEach { frame ->
                System.arraycopy(frame, 0, mergedData, offset, frame.size)
                offset += frame.size
            }
            audioBuffer.clear()
            
            val duration = AudioUtils.getAudioDuration(mergedData)
            Timber.d("录音时长: ${String.format("%.2f", duration)}秒")
            
            // 太短的录音直接丢弃
            if (duration < 0.5) {
                Timber.d("录音太短，忽略")
                withContext(Dispatchers.Main) {
                    updateNotification("正在监听...")
                }
                return
            }
            
            // 保存为WAV文件
            val audioFile = AudioUtils.saveWavFile(this@ListenService, mergedData)
            if (audioFile == null) {
                Timber.e("保存音频文件失败")
                withContext(Dispatchers.Main) {
                    updateNotification("正在监听...")
                }
                return
            }
            
            // 语音转文字
            when (val result = NetworkUtils.transcribeAudio(audioFile, apiKey)) {
                is NetworkUtils.Result.Success -> {
                    val text = result.data
                    Timber.d("转写结果: $text")
                    
                    // AI解析指令
                    processCommand(text)
                }
                is NetworkUtils.Result.Error -> {
                    Timber.e("转写失败: ${result.message}")
                    withContext(Dispatchers.Main) {
                        updateNotification("正在监听...")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "处理录音异常")
            withContext(Dispatchers.Main) {
                updateNotification("正在监听...")
            }
        }
    }
    
    /**
     * 处理指令
     */
    private suspend fun processCommand(userText: String) {
        try {
            // 调用AI生成指令和回复
            when (val result = NetworkUtils.chatWithAI(userText, apiKey)) {
                is NetworkUtils.Result.Success -> {
                    val aiResponse = result.data.trim()
                    Timber.d("AI回复: $aiResponse")
                    
                    // 语音播报AI回复
                    withContext(Dispatchers.Main) {
                        speak(aiResponse)
                    }
                    
                    // 尝试从回复中提取shell命令（如果有）
                    val command = extractCommand(aiResponse)
                    if (command.isNotEmpty()) {
                        // 执行指令
                        executeCommand(command)
                    } else {
                        // 只是对话，没有指令，回到监听状态
                        withContext(Dispatchers.Main) {
                            updateNotification("正在监听...")
                        }
                    }
                }
                is NetworkUtils.Result.Error -> {
                    Timber.e("AI处理失败: ${result.message}")
                    withContext(Dispatchers.Main) {
                        speak("抱歉，我没有理解您的意思")
                        updateNotification("正在监听...")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "处理指令异常")
            withContext(Dispatchers.Main) {
                updateNotification("正在监听...")
            }
        }
    }
    
    /**
     * 从AI回复中提取shell命令
     */
    private fun extractCommand(aiResponse: String): String {
        // 检查是否包含shell命令（通常AI会明确说明）
        val commandPrefixes = listOf("svc ", "am ", "settings ", "input ", "cmd ", "pm ", "dumpsys ")
        
        for (prefix in commandPrefixes) {
            if (aiResponse.contains(prefix)) {
                // 尝试提取命令行
                val lines = aiResponse.lines()
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith(prefix)) {
                        return trimmed
                    }
                }
            }
        }
        
        return ""
    }
    
    /**
     * 执行指令
     */
    private suspend fun executeCommand(command: String) {
        try {
            // 通过广播发送给 CmdReceiver 执行
            Timber.d("发送指令广播: $command")
            val intent = Intent("com.example.kimivoice.EXECUTE_COMMAND")
            intent.putExtra("command", command)
            intent.setPackage(packageName)
            sendBroadcast(intent)
            
            withContext(Dispatchers.Main) {
                updateNotification("正在监听...")
            }
        } catch (e: Exception) {
            Timber.e(e, "执行指令异常")
            withContext(Dispatchers.Main) {
                updateNotification("正在监听...")
            }
        }
    }
    
    /**
     * 停止录音
     */
    private fun stopRecording() {
        try {
            isRecording = false
            
            // 取消录音协程
            recordingJob?.cancel()
            recordingJob = null
            
            // 释放AudioRecord
            audioRecord?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                }
                release()
            }
            audioRecord = null
            
            // 清理缓冲区
            audioBuffer.clear()
            
            Timber.d("录音已停止")
        } catch (e: Exception) {
            Timber.e(e, "停止录音异常")
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "语音监听渠道",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "KimiVoice语音监听服务"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建通知
     */
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        
        return builder
            .setContentTitle("KimiVoice")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }
}

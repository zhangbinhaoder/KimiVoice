package com.example.kimivoice

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 音频处理工具类
 * 优化内容：
 * 1. 改进性能 - 使用缓冲区优化、减少对象创建
 * 2. 添加完善的异常处理
 * 3. 优化内存使用 - 及时释放资源
 * 4. 添加更多音频处理功能
 */
object AudioUtils {
    private const val WAV_HEADER_SIZE = 44
    
    /**
     * 保存WAV文件（优化版）
     * @param context 上下文
     * @param data 音频数据
     * @return 保存的文件，失败返回null
     */
    fun saveWavFile(context: Context, data: ShortArray): File? {
        if (data.isEmpty()) {
            Timber.w("音频数据为空，无法保存")
            return null
        }
        
        val audioFile = File(context.cacheDir, KimiConstants.AUDIO_CACHE_NAME)
        
        return try {
            // 清理旧文件
            if (audioFile.exists()) {
                audioFile.delete()
            }
            
            audioFile.outputStream().use { out ->
                writeWavHeader(out, data.size)
                writeAudioData(out, data)
            }
            
            Timber.d("WAV文件保存成功: ${audioFile.length()} 字节")
            audioFile
        } catch (e: Exception) {
            Timber.e(e, "保存WAV文件失败")
            // 清理损坏的文件
            if (audioFile.exists()) {
                audioFile.delete()
            }
            null
        }
    }
    
    /**
     * 写入WAV文件头
     */
    private fun writeWavHeader(out: OutputStream, dataSize: Int) {
        val byteRate = KimiConstants.AUDIO_SAMPLE_RATE * 2 // 16-bit mono
        val dataLen = dataSize * 2
        val totalLen = dataLen + 36
        
        // RIFF chunk
        out.write("RIFF".toByteArray())
        writeInt(out, totalLen)
        out.write("WAVE".toByteArray())
        
        // fmt chunk
        out.write("fmt ".toByteArray())
        writeInt(out, 16) // fmt chunk size
        writeShort(out, 1) // PCM format
        writeShort(out, 1) // mono
        writeInt(out, KimiConstants.AUDIO_SAMPLE_RATE)
        writeInt(out, byteRate)
        writeShort(out, 2) // block align
        writeShort(out, 16) // bits per sample
        
        // data chunk
        out.write("data".toByteArray())
        writeInt(out, dataLen)
    }
    
    /**
     * 写入音频数据（优化内存使用）
     */
    private fun writeAudioData(out: OutputStream, data: ShortArray) {
        val bufferSize = 8192 // 8KB缓冲区
        val buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)
        
        var index = 0
        while (index < data.size) {
            buffer.clear()
            
            // 填充缓冲区
            while (buffer.remaining() >= 2 && index < data.size) {
                buffer.putShort(data[index])
                index++
            }
            
            // 写入缓冲区数据
            out.write(buffer.array(), 0, buffer.position())
        }
    }
    
    /**
     * 计算音频能量（优化算法）
     * 使用RMS（均方根）算法计算音量
     */
    fun calculateAudioEnergy(data: ShortArray): Long {
        if (data.isEmpty()) return 0L
        
        var sum = 0L
        var count = 0
        
        // 采样计算，降低CPU占用（每4个采样点取1个）
        for (i in data.indices step 4) {
            val sample = data[i].toLong()
            sum += sample * sample
            count++
        }
        
        return if (count > 0) sum / count else 0L
    }
    
    /**
     * 计算音频峰值
     */
    fun calculatePeakAmplitude(data: ShortArray): Int {
        if (data.isEmpty()) return 0
        
        var peak = 0
        for (i in data.indices step 2) {
            val abs = kotlin.math.abs(data[i].toInt())
            if (abs > peak) {
                peak = abs
            }
        }
        
        return peak
    }
    
    /**
     * 检测是否为静音
     * @param data 音频数据
     * @param threshold 能量阈值
     * @return true表示静音
     */
    fun isSilence(data: ShortArray, threshold: Int = KimiConstants.DEFAULT_ENERGY_THRESHOLD): Boolean {
        val energy = calculateAudioEnergy(data)
        return energy < threshold
    }
    
    /**
     * 音频数据归一化（防止溢出）
     */
    fun normalizeAudio(data: ShortArray): ShortArray {
        if (data.isEmpty()) return data
        
        val peak = calculatePeakAmplitude(data)
        if (peak == 0 || peak < Short.MAX_VALUE / 2) {
            return data
        }
        
        // 计算缩放因子
        val scale = (Short.MAX_VALUE * 0.9) / peak
        
        return ShortArray(data.size) { i ->
            (data[i] * scale).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }
    
    /**
     * 清理音频缓存
     */
    fun clearAudioCache(context: Context) {
        try {
            val cacheFile = File(context.cacheDir, KimiConstants.AUDIO_CACHE_NAME)
            if (cacheFile.exists() && cacheFile.delete()) {
                Timber.d("音频缓存已清理")
            }
        } catch (e: Exception) {
            Timber.e(e, "清理音频缓存失败")
        }
    }
    
    /**
     * 获取音频文件时长（秒）
     */
    fun getAudioDuration(data: ShortArray): Double {
        if (data.isEmpty()) return 0.0
        return data.size.toDouble() / KimiConstants.AUDIO_SAMPLE_RATE
    }
    
    /**
     * 写入4字节整数（小端序）
     */
    private fun writeInt(out: OutputStream, v: Int) {
        out.write(byteArrayOf(
            (v and 0xff).toByte(),
            ((v shr 8) and 0xff).toByte(),
            ((v shr 16) and 0xff).toByte(),
            ((v shr 24) and 0xff).toByte()
        ))
    }
    
    /**
     * 写入2字节短整数（小端序）
     */
    private fun writeShort(out: OutputStream, v: Int) {
        out.write(byteArrayOf(
            (v and 0xff).toByte(),
            ((v shr 8) and 0xff).toByte()
        ))
    }
}
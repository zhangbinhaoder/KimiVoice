package com.example.kimivoice

import android.os.Build

object KimiConstants {
    // 服务配置
    const val CHANNEL_ID = "kimi_listen_channel"
    const val NOTIFICATION_ID = 1001
    const val PERMISSION_REQUEST_CODE = 1002

    // 音频配置
    const val AUDIO_SAMPLE_RATE = 16000
    const val AUDIO_CHANNEL = android.media.AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_ENCODING = android.media.AudioFormat.ENCODING_PCM_16BIT
    const val DEFAULT_ENERGY_THRESHOLD = 2000
    const val RECORD_BUFFER_MULTIPLIER = 2
    const val AUDIO_CACHE_NAME = "query.wav"

    // 网络配置
    const val MOONSHOT_TRANSCRIBE_URL = "https://api.moonshot.cn/v1/audio/transcriptions"
    const val MOONSHOT_CHAT_URL = "https://api.moonshot.cn/v1/chat/completions"
    const val CONNECT_TIMEOUT = 10L // 秒
    const val READ_TIMEOUT = 15L // 秒
    const val WRITE_TIMEOUT = 15L // 秒
    const val MAX_RETRY_COUNT = 2 // 最大重试次数

    // 指令白名单
    val ALLOWED_COMMANDS = listOf(
        "svc wifi", "settings put", "am start", "am broadcast",
        "service call", "input tap", "input swipe"
    )

    // Android版本适配
    val isAndroid14Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val isAndroid13Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val isAndroid10Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}
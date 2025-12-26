#!/system/bin/sh
# Magisk service script - KimiVoice TTS version

MODDIR=${0%/*}

# Auto grant permissions
PKG="com.example.kimivoice"

# Record audio permission
pm grant $PKG android.permission.RECORD_AUDIO 2>/dev/null

# Foreground service permission
pm grant $PKG android.permission.FOREGROUND_SERVICE 2>/dev/null

# Foreground service microphone permission
pm grant $PKG android.permission.FOREGROUND_SERVICE_MICROPHONE 2>/dev/null
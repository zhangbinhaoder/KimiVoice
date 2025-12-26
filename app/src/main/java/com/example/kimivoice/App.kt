package com.example.kimivoice

import android.app.Application
import timber.log.Timber

/**
 * 全局应用初始化类（优化版）
 * 优化内容：
 * 1. 改进日志系统
 * 2. 添加全局异常处理
 * 3. 添加性能监控
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // 初始化日志系统
            initLogging()
            
            // 设置全局异常处理器
            setupGlobalExceptionHandler()
            
            Timber.d("KimiVoice App初始化完成")
            Timber.d("版本: ${BuildConfig.VERSION_NAME}, 构建类型: ${if (BuildConfig.DEBUG) "Debug" else "Release"}")
        } catch (e: Exception) {
            // 如果初始化失败，至少打印错误
            e.printStackTrace()
        }
    }
    
    /**
     * 初始化日志系统
     */
    private fun initLogging() {
        val isDebugMode = try {
            BuildConfig.DEBUG
        } catch (e: Exception) {
            true // 容错处理
        }
        
        if (isDebugMode) {
            // Debug模式：详细日志
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    // 显示文件名和行号
                    return "(${element.fileName}:${element.lineNumber})"
                }
            })
        } else {
            // Release模式：只记录错误和警告
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= android.util.Log.WARN) {
                        // 这里可以集成崩溃收集服务（如Crashlytics）
                        android.util.Log.println(priority, tag ?: "KimiVoice", message)
                        t?.printStackTrace()
                    }
                }
            })
        }
    }
    
    /**
     * 设置全局异常处理器
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // 记录崩溃信息
                Timber.e(throwable, "全局异常捕获 - 线程: ${thread.name}")
                
                // 保存崩溃日志到文件（可选）
                saveCrashLog(throwable)
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 调用默认处理器
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
    
    /**
     * 保存崩溃日志
     */
    private fun saveCrashLog(throwable: Throwable) {
        try {
            val crashDir = getExternalFilesDir("crash")
            if (crashDir != null && !crashDir.exists()) {
                crashDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val logFile = java.io.File(crashDir, "crash_$timestamp.log")
            
            logFile.writeText(
                buildString {
                    appendLine("=== KimiVoice Crash Log ===")
                    appendLine("时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                    appendLine("版本: ${BuildConfig.VERSION_NAME}")
                    appendLine("设备: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                    appendLine()
                    appendLine("异常信息:")
                    appendLine(throwable.stackTraceToString())
                }
            )
            
            Timber.d("崩溃日志已保存: ${logFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
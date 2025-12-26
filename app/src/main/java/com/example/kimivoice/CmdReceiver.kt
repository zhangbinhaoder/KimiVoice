package com.example.kimivoice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * 指令接收广播（完整实现版）
 * 功能：
 * 1. 接收外部指令
 * 2. 验证指令安全性
 * 3. 执行允许的指令
 */
class CmdReceiver : BroadcastReceiver() {
    companion object {
        // 自定义Action
        const val ACTION_EXECUTE_COMMAND = "com.example.kimivoice.EXECUTE_COMMAND"
        const val EXTRA_COMMAND = "command"
        
        /**
         * 验证指令是否在白名单中
         */
        fun isCommandAllowed(command: String): Boolean {
            if (command.isEmpty()) return false
            
            // 检查是否以允许的前缀开头
            return KimiConstants.ALLOWED_COMMANDS.any { allowed ->
                command.trim().startsWith(allowed)
            }
        }
        
        /**
         * 验证指令安全性（防止危险操作）
         */
        fun isCommandSafe(command: String): Boolean {
            val lowerCmd = command.lowercase().trim()
            
            // 危险指令黑名单
            val dangerousKeywords = listOf(
                "rm -rf",
                "format",
                "mkfs",
                "dd if=",
                ">/dev/",
                "shutdown",
                "reboot -p",
                "fastboot",
                "recovery"
            )
            
            // 检查是否包含危险关键词
            return dangerousKeywords.none { lowerCmd.contains(it) }
        }
        
        /**
         * 全面验证指令
         */
        fun validateCommand(command: String): Boolean {
            return isCommandAllowed(command) && isCommandSafe(command)
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Timber.w("无效的广播接收参数")
            return
        }
        
        try {
            when (intent.action) {
                ACTION_EXECUTE_COMMAND -> {
                    val command = intent.getStringExtra(EXTRA_COMMAND) ?: ""
                    handleExecuteCommand(context, command)
                }
                else -> {
                    Timber.d("收到未知指令: ${intent.action}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "处理广播异常")
        }
    }
    
    /**
     * 处理执行指令请求
     */
    private fun handleExecuteCommand(context: Context, command: String) {
        Timber.d("收到执行指令请求: $command")
        
        if (command.isEmpty()) {
            Timber.w("指令为空")
            return
        }
        
        // 验证指令
        if (!validateCommand(command)) {
            Timber.w("指令未通过安全验证: $command")
            return
        }
        
        // 执行指令
        executeCommand(command)
    }
    
    /**
     * 执行系统指令
     */
    private fun executeCommand(command: String) {
        try {
            Timber.d("执行指令: $command")
            
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Timber.d("指令执行成功")
            } else {
                Timber.w("指令执行失败，退出码: $exitCode")
            }
        } catch (e: Exception) {
            Timber.e(e, "执行指令异常")
        }
    }
}
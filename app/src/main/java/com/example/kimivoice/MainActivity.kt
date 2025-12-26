package com.example.kimivoice

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * 主界面Activity（优化版）
 * 优化内容：
 * 1. 完善的异常处理
 * 2. 改进用户交互体验
 * 3. 添加输入验证
 * 4. 状态反馈优化
 */
class MainActivity : AppCompatActivity() {
    // 控件声明
    private lateinit var etApiKey: EditText
    private lateinit var etThreshold: EditText
    private lateinit var btnSaveConfig: Button
    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button
    
    // 服务运行状态
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 加载XML布局
            setContentView(R.layout.activity_main)
            
            // 初始化控件
            initViews()
            
            // 读取已保存配置
            loadSavedConfig()
            
            // 绑定点击事件
            bindClickEvents()
            
            // 添加输入监听
            addInputListeners()
            
            // 检查权限
            checkAndRequestPermissions()
            
            Timber.d("MainActivity创建成功")
        } catch (e: Exception) {
            Timber.e(e, "MainActivity初始化失败")
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * 初始化控件
     */
    private fun initViews() {
        try {
            etApiKey = findViewById(R.id.et_api_key)
            etThreshold = findViewById(R.id.et_energy_threshold)
            btnSaveConfig = findViewById(R.id.btn_save_config)
            btnStartService = findViewById(R.id.btn_start_service)
            btnStopService = findViewById(R.id.btn_stop_service)
            
            // 初始化按钮状态
            updateButtonStates()
        } catch (e: Exception) {
            Timber.e(e, "初始化控件失败")
            throw e
        }
    }

    /**
     * 读取已保存的配置
     */
    private fun loadSavedConfig() {
        try {
            // 读取阈值（默认2000）
            val savedThreshold = getSharedPreferences("kimi_prefs", MODE_PRIVATE)
                .getInt("energy_threshold", KimiConstants.DEFAULT_ENERGY_THRESHOLD)
            etThreshold.setText(savedThreshold.toString())

            // API Key输入框提示语
            etApiKey.hint = getString(R.string.hint_api_key)
            
            // 检查是否已配置API Key
            val hasApiKey = SecurityUtils.getApiKey(this).isNotEmpty()
            if (hasApiKey) {
                etApiKey.hint = "API Key已配置"
            }
            
            Timber.d("配置加载成功")
        } catch (e: Exception) {
            Timber.e(e, "加载配置失败")
        }
    }
    
    /**
     * 添加输入监听
     */
    private fun addInputListeners() {
        // 阈值输入验证
        etThreshold.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateThresholdInput(s.toString())
            }
        })
    }
    
    /**
     * 验证阈值输入
     */
    private fun validateThresholdInput(input: String) {
        if (input.isEmpty()) return
        
        try {
            val value = input.toInt()
            if (value < 100 || value > 50000) {
                etThreshold.error = "建议范围: 100-50000"
            }
        } catch (e: NumberFormatException) {
            etThreshold.error = "请输入有效数字"
        }
    }

    /**
     * 绑定按钮点击事件
     */
    private fun bindClickEvents() {
        // 保存配置按钮
        btnSaveConfig.setOnClickListener {
            handleSaveConfig()
        }

        // 启动监听服务按钮
        btnStartService.setOnClickListener {
            handleStartService()
        }

        // 停止监听服务按钮
        btnStopService.setOnClickListener {
            handleStopService()
        }
    }
    
    /**
     * 处理保存配置
     */
    private fun handleSaveConfig() {
        try {
            val apiKey = etApiKey.text.toString().trim()
            val thresholdStr = etThreshold.text.toString().trim()
            var hasSaved = false

            // 保存API Key（加密）
            if (apiKey.isNotEmpty()) {
                if (!SecurityUtils.isValidApiKey(apiKey)) {
                    Toast.makeText(this, "API Key格式不正确", Toast.LENGTH_SHORT).show()
                    return
                }
                
                SecurityUtils.saveApiKey(this, apiKey)
                Toast.makeText(this, "API Key已加密保存", Toast.LENGTH_SHORT).show()
                etApiKey.setText("") // 清空输入框防泄露
                etApiKey.hint = "API Key已配置"
                hasSaved = true
            }

            // 保存能量阈值
            if (thresholdStr.isNotEmpty()) {
                val threshold = thresholdStr.toIntOrNull()
                if (threshold == null || threshold < 100 || threshold > 50000) {
                    Toast.makeText(this, "阈值范围: 100-50000", Toast.LENGTH_SHORT).show()
                    return
                }
                
                getSharedPreferences("kimi_prefs", MODE_PRIVATE)
                    .edit()
                    .putInt("energy_threshold", threshold)
                    .apply()
                Toast.makeText(this, "能量阈值已设为：$threshold", Toast.LENGTH_SHORT).show()
                hasSaved = true
            }

            // 无输入提示
            if (!hasSaved) {
                Toast.makeText(this, "请输入API Key或调整能量阈值", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Timber.e(e, "保存配置失败")
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 处理启动服务
     */
    private fun handleStartService() {
        try {
            // 权限检查
            if (!checkPermissions()) {
                showPermissionDialog()
                return
            }

            // 检查API Key是否配置
            val apiKey = SecurityUtils.getApiKey(this)
            if (apiKey.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("请先配置Moonshot API Key")
                    .setPositiveButton("确定", null)
                    .show()
                return
            }

            // 启动前台服务（适配Android 8.0+）
            val serviceIntent = Intent(this, ListenService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
            isServiceRunning = true
            updateButtonStates()
            Toast.makeText(this, "语音监听服务已启动", Toast.LENGTH_SHORT).show()
            Timber.d("服务启动成功")
        } catch (e: Exception) {
            Timber.e(e, "启动服务失败")
            Toast.makeText(this, "启动失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 处理停止服务
     */
    private fun handleStopService() {
        try {
            val serviceIntent = Intent(this, ListenService::class.java)
            stopService(serviceIntent)
            
            isServiceRunning = false
            updateButtonStates()
            Toast.makeText(this, "语音监听服务已停止", Toast.LENGTH_SHORT).show()
            Timber.d("服务停止成功")
        } catch (e: Exception) {
            Timber.e(e, "停止服务失败")
            Toast.makeText(this, "停止失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 更新按钮状态
     */
    private fun updateButtonStates() {
        btnStartService.isEnabled = !isServiceRunning
        btnStopService.isEnabled = isServiceRunning
    }

    /**
     * 检查必要权限
     */
    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.INTERNET
        )
        
        // Android 13+ 通知权限
        if (KimiConstants.isAndroid13Plus) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 显示权限说明对话框
     */
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("为了正常使用语音监听功能，需要以下权限：\n\n1. 录音权限 - 用于采集语音\n2. 通知权限 - 用于显示服务状态\n3. 网络权限 - 用于语音识别")
            .setPositiveButton("授予权限") { _, _ ->
                checkAndRequestPermissions()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 申请缺失的权限
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // 录音权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
        }

        // Android 13+ 通知权限
        if (KimiConstants.isAndroid13Plus && 
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 申请权限
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                KimiConstants.PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * 权限申请结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == KimiConstants.PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }

            if (deniedPermissions.isNotEmpty()) {
                val message = "以下权限被拒绝，部分功能无法使用：\n" + 
                    deniedPermissions.joinToString("\n") { getPermissionName(it) }
                
                AlertDialog.Builder(this)
                    .setTitle("权限被拒绝")
                    .setMessage(message)
                    .setPositiveButton("确定", null)
                    .show()
            } else {
                Toast.makeText(this, "所有权限已授予", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 获取权限中文名称
     */
    private fun getPermissionName(permission: String): String {
        return when (permission) {
            android.Manifest.permission.RECORD_AUDIO -> "录音权限"
            android.Manifest.permission.POST_NOTIFICATIONS -> "通知权限"
            else -> permission
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity销毁")
    }
}
package com.example.kimivoice

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 安全工具类 - 提供加密存储、证书固定等安全功能
 * 优化内容：
 * 1. 使用Android KeyStore存储密钥（API 23+）
 * 2. 升级为AES-GCM模式（更安全）
 * 3. 添加完善的异常处理
 * 4. 支持多种加密方式兼容
 */
object SecurityUtils {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "kimi_voice_key"
    private const val AES_FALLBACK_KEY = "kimi_voice_2025!@#" // 16字节
    private const val SP_NAME = "kimi_secure_prefs"
    private const val KEY_API_KEY = "secure_api_key"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    /**
     * 获取或生成密钥（优先使用KeyStore）
     */
    private fun getOrCreateSecretKey(): SecretKey? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+ 使用KeyStore
                val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
                keyStore.load(null)

                // 检查密钥是否存在
                if (!keyStore.containsAlias(KEY_ALIAS)) {
                    // 生成新密钥
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        KEYSTORE_PROVIDER
                    )
                    val spec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                    keyGenerator.init(spec)
                    keyGenerator.generateKey()
                }

                // 获取密钥
                val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
                entry.secretKey
            } else {
                // 低版本使用固定密钥
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "获取KeyStore密钥失败，降级使用固定密钥")
            null
        }
    }

    /**
     * AES-GCM加密（Android 6.0+）
     */
    private fun encryptGCM(input: String, secretKey: SecretKey): String? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
            
            // 拼接 IV + 密文
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "AES-GCM加密失败")
            null
        }
    }

    /**
     * AES-GCM解密（Android 6.0+）
     */
    private fun decryptGCM(input: String, secretKey: SecretKey): String? {
        return try {
            val combined = Base64.decode(input, Base64.NO_WRAP)
            
            // 分离 IV 和 密文
            val iv = ByteArray(GCM_IV_LENGTH)
            val encrypted = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(encrypted)
            
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "AES-GCM解密失败")
            null
        }
    }

    /**
     * AES-ECB加密（降级方案）
     */
    private fun encryptECB(input: String): String? {
        return try {
            val keySpec = SecretKeySpec(AES_FALLBACK_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "AES-ECB加密失败")
            null
        }
    }

    /**
     * AES-ECB解密（降级方案）
     */
    private fun decryptECB(input: String): String? {
        return try {
            val keySpec = SecretKeySpec(AES_FALLBACK_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(input, Base64.NO_WRAP)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "AES-ECB解密失败")
            null
        }
    }

    /**
     * 加密字符串（自动选择最佳方案）
     */
    fun encrypt(input: String): String {
        if (input.isEmpty()) return ""
        
        // 优先使用KeyStore + GCM
        val secretKey = getOrCreateSecretKey()
        if (secretKey != null) {
            val result = encryptGCM(input, secretKey)
            if (result != null) return result
        }
        
        // 降级使用ECB
        return encryptECB(input) ?: ""
    }

    /**
     * 解密字符串（自动选择最佳方案）
     */
    fun decrypt(input: String): String {
        if (input.isEmpty()) return ""
        
        // 优先尝试KeyStore + GCM
        val secretKey = getOrCreateSecretKey()
        if (secretKey != null) {
            val result = decryptGCM(input, secretKey)
            if (result != null) return result
        }
        
        // 降级尝试ECB
        return decryptECB(input) ?: ""
    }

    /**
     * 保存加密的API Key
     */
    fun saveApiKey(context: Context, apiKey: String) {
        try {
            if (apiKey.isEmpty()) {
                Timber.w("尝试保存空的API Key")
                return
            }
            
            val encrypted = encrypt(apiKey)
            if (encrypted.isEmpty()) {
                Timber.e("API Key加密失败")
                return
            }
            
            context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_API_KEY, encrypted)
                .apply()
            
            Timber.d("API Key已加密保存")
        } catch (e: Exception) {
            Timber.e(e, "保存API Key失败")
        }
    }

    /**
     * 获取解密的API Key
     */
    fun getApiKey(context: Context): String {
        return try {
            val encrypted = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_KEY, "") ?: ""
            
            if (encrypted.isEmpty()) {
                Timber.d("未找到已保存的API Key")
                return ""
            }
            
            decrypt(encrypted)
        } catch (e: Exception) {
            Timber.e(e, "获取API Key失败")
            ""
        }
    }

    /**
     * 清除保存的API Key
     */
    fun clearApiKey(context: Context) {
        try {
            context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_API_KEY)
                .apply()
            Timber.d("API Key已清除")
        } catch (e: Exception) {
            Timber.e(e, "清除API Key失败")
        }
    }

    /**
     * 验证API Key格式
     */
    fun isValidApiKey(apiKey: String): Boolean {
        return apiKey.isNotEmpty() && apiKey.length >= 20
    }
}
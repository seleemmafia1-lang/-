package com.example.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.content.Context
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import java.util.*

/**
 * نظام التشفير الآمن لكلمات المرور والبيانات الحساسة
 */
object SecurityUtils {

    private const val KEY_ALIAS = "RaneenQualityKey"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256

    /**
     * تشفير كلمة مرور بطريقة آمنة
     */
    fun hashPassword(password: String): String {
        // استخدام SHA-256 مع Salt
        val salt = generateSalt()
        val hashedPassword = password.toByteArray().plus(salt.toByteArray()).joinToString("") { 
            "%02x".format(it) 
        }
        return "$salt:$hashedPassword"
    }

    /**
     * التحقق من كلمة المرور
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            val parts = hash.split(":")
            if (parts.size != 2) return false
            
            val salt = parts[0]
            val hashedInput = password.toByteArray().plus(salt.toByteArray()).joinToString("") { 
                "%02x".format(it) 
            }
            
            hashedInput == parts[1]
        } catch (e: Exception) {
            false
        }
    }

    /**
     * توليد ملح عشوائي
     */
    private fun generateSalt(): String {
        val random = Random()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    /**
     * تشفير النصوص بـ AES
     */
    fun encryptText(plainText: String, context: Context): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getOrCreateSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val encryptedBytes = cipher.doFinal(plainText.toByteArray())
            val iv = cipher.iv
            
            // دمج IV والبيانات المشفرة
            val combined = iv + encryptedBytes
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            plainText // في حالة الفشل، نعيد النص الأصلي
        }
    }

    /**
     * فك تشفير النصوص
     */
    fun decryptText(encryptedText: String, context: Context): String {
        return try {
            val decodedBytes = Base64.getDecoder().decode(encryptedText)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getOrCreateSecretKey()
            
            // استخراج IV من البيانات
            val iv = decodedBytes.sliceArray(0 until cipher.blockSize)
            val encrypted = decodedBytes.sliceArray(cipher.blockSize until decodedBytes.size)
            
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(encrypted)
            
            String(decryptedBytes)
        } catch (e: Exception) {
            encryptedText // في حالة الفشل
        }
    }

    /**
     * الحصول على أو إنشاء مفتاح التشفير
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // التحقق من وجود المفتاح
        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey is SecretKey) {
            return existingKey
        }

        // إنشاء مفتاح جديد
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .build()

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        
        return keyGenerator.generateKey()
    }

    /**
     * حفظ البيانات الحساسة بشكل آمن في SharedPreferences
     */
    fun saveSecureData(context: Context, key: String, value: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "raneen_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedSharedPreferences.edit().putString(key, value).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * استرجاع البيانات الحساسة من SharedPreferences الآمن
     */
    fun getSecureData(context: Context, key: String): String? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                "raneen_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            encryptedSharedPreferences.getString(key, null)
        } catch (e: Exception) {
            null
        }
    }
}

package com.internetguard.pro.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure API key management using Android Keystore
 * 
 * Security features:
 * - AES/GCM encryption with 256-bit keys
 * - Android Keystore for key protection
 * - No key material in memory longer than necessary
 * - No logging of sensitive data
 * - Protected against key extraction
 */
class SecureKeyManager(private val context: Context) {
    
    companion object {
        private const val KEYSTORE_ALIAS = "openai_api_key_alias"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val PREFS_NAME = "secure_keys"
        private const val ENCRYPTED_KEY_PREF = "encrypted_openai_key"
        private const val IV_PREF = "openai_key_iv"
        private const val TAG = "SecureKeyManager"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false) // Set to true for biometric protection
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Securely store OpenAI API key
     * Security: Key is encrypted immediately and never logged
     */
    fun storeApiKey(apiKey: String): Boolean {
        return try {
            // Validate API key format before storing
            if (!isValidApiKey(apiKey)) {
                Log.w(TAG, "Invalid API key format rejected")
                return false
            }
            
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedKey = cipher.doFinal(apiKey.toByteArray())

            // Store encrypted key and IV in SharedPreferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(ENCRYPTED_KEY_PREF, Base64.encodeToString(encryptedKey, Base64.DEFAULT))
                .putString(IV_PREF, Base64.encodeToString(iv, Base64.DEFAULT))
                .apply()

            // Log success without exposing key
            Log.i(TAG, "API key stored securely (length: ${apiKey.length})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store API key", e)
            false
        }
    }

    /**
     * Securely retrieve OpenAI API key
     * Security: Key is only decrypted when needed and cleared from memory ASAP
     */
    fun getApiKey(): String? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val encryptedKeyString = prefs.getString(ENCRYPTED_KEY_PREF, null) ?: return null
            val ivString = prefs.getString(IV_PREF, null) ?: return null

            val encryptedKey = Base64.decode(encryptedKeyString, Base64.DEFAULT)
            val iv = Base64.decode(ivString, Base64.DEFAULT)

            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedKey = cipher.doFinal(encryptedKey)
            val apiKey = String(decryptedKey)
            
            // Clear sensitive data from memory
            decryptedKey.fill(0)
            
            // Log retrieval without exposing key
            Log.d(TAG, "API key retrieved (length: ${apiKey.length})")
            apiKey
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve API key", e)
            null
        }
    }

    /**
     * Remove stored API key
     */
    fun removeApiKey(): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(ENCRYPTED_KEY_PREF)
                .remove(IV_PREF)
                .apply()
            Log.i(TAG, "API key removed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove API key", e)
            false
        }
    }

    /**
     * Check if API key is stored
     */
    fun hasApiKey(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(ENCRYPTED_KEY_PREF) && prefs.contains(IV_PREF)
    }
    
    /**
     * Validate API key format
     * Security: This prevents storing invalid keys
     */
    private fun isValidApiKey(apiKey: String): Boolean {
        // OpenAI API keys start with "sk-" and are typically 48-51 characters
        return apiKey.startsWith("sk-") && apiKey.length >= 20
    }
    
    /**
     * Get masked API key for display purposes
     * Security: Never show the actual key
     */
    fun getMaskedApiKey(): String {
        return if (hasApiKey()) {
            "sk-••••••••••••••••••••••••••••"
        } else {
            "Not set"
        }
    }
}

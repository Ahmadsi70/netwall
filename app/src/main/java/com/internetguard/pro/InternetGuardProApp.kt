package com.internetguard.pro

import android.app.Application
import com.internetguard.pro.data.database.AppDatabase
import com.internetguard.pro.utils.LanguageManager
// import com.internetguard.pro.security.DatabaseEncryptionManager // Removed for size optimization

class InternetGuardProApp : Application() {
    
    // 🚀 OPTIMIZATION: Lazy initialization of database (only when first accessed)
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    // lateinit var encryptionManager: DatabaseEncryptionManager // Removed for size optimization
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 🌍 LANGUAGE: Initialize language system based on device settings
        initializeLanguageSystem()
        
        // Initialize encryption manager
        // encryptionManager = DatabaseEncryptionManager(this) // Removed for size optimization
        
        // Database will be initialized lazily when first accessed
    }
    
    /**
     * Initializes the language system based on device language settings
     */
    private fun initializeLanguageSystem() {
        try {
            // Get the best matching language for the current system
            val systemLanguage = LanguageManager.getBestMatchingLanguage(this)
            
            // Apply the language to the application context
            LanguageManager.applyLanguage(this, systemLanguage)
            
            android.util.Log.d("InternetGuardProApp", "Language system initialized with: $systemLanguage")
        } catch (e: Exception) {
            android.util.Log.e("InternetGuardProApp", "Error initializing language system: ${e.message}", e)
            // Fallback to English if there's an error
            LanguageManager.applyLanguage(this, "en")
        }
    }

    companion object {
        @Volatile
        private var instance: InternetGuardProApp? = null

        fun getInstance(): InternetGuardProApp? = instance
    }
}

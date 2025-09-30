package com.internetguard.pro

import android.app.Application
import com.internetguard.pro.data.database.AppDatabase
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
        
        // Initialize encryption manager
        // encryptionManager = DatabaseEncryptionManager(this) // Removed for size optimization
        
        // Database will be initialized lazily when first accessed
    }

    companion object {
        @Volatile
        private var instance: InternetGuardProApp? = null

        fun getInstance(): InternetGuardProApp? = instance
    }
}

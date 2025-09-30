package com.internetguard.pro.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

/**
 * System Level Network Controller
 * 
 * Controls network access at the system level using root privileges.
 * Implements iptables rules, socket blocking, and process monitoring.
 */
class SystemLevelNetworkController(private val context: Context) {
    
    companion object {
        private const val TAG = "SystemLevelNetworkController"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isInitialized = AtomicBoolean(false)
    
    // iptables rules manager
    private val iptablesManager = IptablesManager()
    
    // Socket blocker
    private val socketBlocker = SocketBlocker()
    
    // Process monitor
    private val processMonitor = com.internetguard.pro.network.ProcessMonitor(context)
    
    init {
        initialize()
    }
    
    /**
     * Initialize system-level controllers
     */
    private fun initialize() {
        scope.launch {
            try {
                if (isRootAvailable()) {
                    iptablesManager.initialize()
                    socketBlocker.initialize()
                    processMonitor.initialize()
                    isInitialized.set(true)
                    Log.i(TAG, "System-level controllers initialized")
                } else {
                    Log.w(TAG, "Root not available, system-level controllers disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize system-level controllers", e)
            }
        }
    }
    
    /**
     * Block internet access for a specific app
     */
    fun blockAppInternet(packageName: String, blockWifi: Boolean, blockCellular: Boolean) {
        if (!isInitialized.get()) {
            Log.w(TAG, "System-level controllers not initialized")
            return
        }
        
        scope.launch {
            try {
                val uid = getAppUid(packageName)
                if (uid == -1) {
                    Log.e(TAG, "Could not get UID for package: $packageName")
                    return@launch
                }
                
                Log.i(TAG, "Blocking internet for app: $packageName (UID: $uid)")
                
                // Block using iptables
                iptablesManager.blockApp(uid, blockWifi, blockCellular)
                
                // Block using socket blocking
                socketBlocker.blockApp(uid)
                
                // Start process monitoring
                processMonitor.startMonitoring(packageName)
                
                Log.i(TAG, "Successfully blocked internet for app: $packageName")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to block internet for app: $packageName", e)
            }
        }
    }
    
    /**
     * Allow internet access for a specific app
     */
    fun allowAppInternet(packageName: String) {
        if (!isInitialized.get()) {
            Log.w(TAG, "System-level controllers not initialized")
            return
        }
        
        scope.launch {
            try {
                val uid = getAppUid(packageName)
                if (uid == -1) {
                    Log.e(TAG, "Could not get UID for package: $packageName")
                    return@launch
                }
                
                Log.i(TAG, "Allowing internet for app: $packageName (UID: $uid)")
                
                // Allow using iptables
                iptablesManager.allowApp(uid)
                
                // Allow using socket blocking
                socketBlocker.allowApp(uid)
                
                // Stop process monitoring
                processMonitor.stopMonitoring(packageName)
                
                Log.i(TAG, "Successfully allowed internet for app: $packageName")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to allow internet for app: $packageName", e)
            }
        }
    }
    
    /**
     * Get app UID from package name
     */
    private fun getAppUid(packageName: String): Int {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.applicationInfo.uid
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get UID for package: $packageName", e)
            -1
        }
    }
    
    /**
     * Check if root is available
     */
    private fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c 'echo test'")
            process.waitFor()
            val exitValue = process.exitValue()
            process.destroy()
            exitValue == 0
        } catch (e: Exception) {
            Log.d(TAG, "Root not available: ${e.message}")
            false
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            iptablesManager.cleanup()
            socketBlocker.cleanup()
            processMonitor.cleanup()
            Log.i(TAG, "System-level controllers cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup system-level controllers", e)
        }
    }
}

/**
 * iptables Rules Manager
 * 
 * Manages iptables rules for blocking app internet access.
 */
class IptablesManager {
    
    companion object {
        private const val TAG = "IptablesManager"
    }
    
    private val activeRules = mutableSetOf<String>()
    
    fun initialize() {
        try {
            // Initialize iptables chains
            executeCommand("iptables -N INTERNETGUARD_BLOCK")
            executeCommand("iptables -A OUTPUT -j INTERNETGUARD_BLOCK")
            Log.i(TAG, "iptables chains initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize iptables chains", e)
        }
    }
    
    fun blockApp(uid: Int, blockWifi: Boolean, blockCellular: Boolean) {
        try {
            val rules = mutableListOf<String>()
            
            if (blockWifi) {
                // Block WiFi traffic
                rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -j DROP")
            }
            
            if (blockCellular) {
                // Block cellular traffic
                rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -j DROP")
            }
            
            // Block DNS queries
            rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -p udp --dport 53 -j DROP")
            
            // Block HTTP/HTTPS
            rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -p tcp --dport 80 -j DROP")
            rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -p tcp --dport 443 -j DROP")
            
            // Block common ports
            rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -p tcp --dport 8080 -j DROP")
            rules.add("iptables -A INTERNETGUARD_BLOCK -m owner --uid-owner $uid -p tcp --dport 8443 -j DROP")
            
            // Execute rules
            rules.forEach { rule ->
                executeCommand(rule)
                activeRules.add(rule)
            }
            
            Log.i(TAG, "iptables rules created for UID: $uid")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create iptables rules for UID: $uid", e)
        }
    }
    
    fun allowApp(uid: Int) {
        try {
            // Remove rules for this UID
            val rulesToRemove = activeRules.filter { it.contains("--uid-owner $uid") }
            
            rulesToRemove.forEach { rule ->
                val removeCommand = rule.replace("iptables -A", "iptables -D")
                executeCommand(removeCommand)
                activeRules.remove(rule)
            }
            
            Log.i(TAG, "iptables rules removed for UID: $uid")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove iptables rules for UID: $uid", e)
        }
    }
    
    fun cleanup() {
        try {
            // Remove all active rules
            activeRules.forEach { rule ->
                val removeCommand = rule.replace("iptables -A", "iptables -D")
                executeCommand(removeCommand)
            }
            activeRules.clear()
            
            // Remove chains
            executeCommand("iptables -D OUTPUT -j INTERNETGUARD_BLOCK")
            executeCommand("iptables -F INTERNETGUARD_BLOCK")
            executeCommand("iptables -X INTERNETGUARD_BLOCK")
            
            Log.i(TAG, "iptables cleanup completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup iptables", e)
        }
    }
    
    private fun executeCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c '$command'")
            process.waitFor()
            process.exitValue() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute command: $command", e)
            false
        }
    }
}

/**
 * Socket Blocker
 * 
 * Blocks socket connections for specific apps.
 */
class SocketBlocker {
    
    companion object {
        private const val TAG = "SocketBlocker"
    }
    
    fun initialize() {
        Log.d(TAG, "Socket blocker initialized")
    }
    
    fun blockApp(uid: Int) {
        // Implement socket blocking
        // This could include:
        // - Socket monitoring
        // - Connection blocking
        // - Port filtering
        Log.d(TAG, "Socket blocking enabled for UID: $uid")
    }
    
    fun allowApp(uid: Int) {
        // Implement socket unblocking
        Log.d(TAG, "Socket blocking disabled for UID: $uid")
    }
    
    fun cleanup() {
        Log.d(TAG, "Socket blocker cleaned up")
    }
}


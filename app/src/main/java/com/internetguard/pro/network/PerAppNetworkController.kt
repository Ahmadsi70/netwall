package com.internetguard.pro.network

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Per-App Network Controller
 * 
 * Controls internet access for individual apps without requiring a global VPN.
 * Uses multiple layers of protection to prevent bypass attempts.
 */
class PerAppNetworkController(private val context: Context) {
    
    companion object {
        private const val TAG = "PerAppNetworkController"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val packageManager = context.packageManager
    
    // App-level controllers
    private val proxyServers = ConcurrentHashMap<String, AppProxyServer>()
    private val networkInterceptors = ConcurrentHashMap<String, NetworkInterceptor>()
    private val appBlockStates = ConcurrentHashMap<String, AppBlockState>()
    
    // System-level controllers (requires root)
    private var systemLevelController: SystemLevelNetworkController? = null
    private var antiBypassDetector: AntiBypassDetector? = null
    private var processMonitor: com.internetguard.pro.network.ProcessMonitor? = null
    
    // Network interface controller
    private var networkInterfaceController: NetworkInterfaceController? = null
    
    init {
        initializeControllers()
    }
    
    /**
     * Initialize all network controllers
     */
    private fun initializeControllers() {
        try {
            // Initialize system-level controllers if root is available
            if (isRootAvailable()) {
                systemLevelController = SystemLevelNetworkController(context)
                antiBypassDetector = AntiBypassDetector(context)
                processMonitor = com.internetguard.pro.network.ProcessMonitor(context)
                networkInterfaceController = NetworkInterfaceController(context)
                
                Log.i(TAG, "System-level controllers initialized (Root available)")
            } else {
                Log.i(TAG, "App-level controllers only (No root access)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize controllers", e)
        }
    }
    
    /**
     * Block internet access for a specific app
     */
    fun blockAppInternet(packageName: String, blockWifi: Boolean = true, blockCellular: Boolean = true) {
        try {
            Log.i(TAG, "Blocking internet for app: $packageName (WiFi: $blockWifi, Cellular: $blockCellular)")
            
            // Create app block state
            val blockState = AppBlockState(
                packageName = packageName,
                blockWifi = blockWifi,
                blockCellular = blockCellular,
                isBlocked = true,
                timestamp = System.currentTimeMillis()
            )
            
            // Store block state immediately (synchronous)
            appBlockStates[packageName] = blockState
            
            // Start async operations
            scope.launch {
                try {
                    // Layer 1: App-level blocking (no root required)
                    blockAppLevel(packageName, blockState)
                    
                    // Layer 2: System-level blocking (root required)
                    if (systemLevelController != null) {
                        systemLevelController?.blockAppInternet(packageName, blockWifi, blockCellular)
                    }
                    
                    // Layer 3: Kernel-level blocking (root required)
                    if (networkInterfaceController != null) {
                        networkInterfaceController?.blockAppInternet(packageName)
                    }
                    
                    // Start anti-bypass monitoring
                    antiBypassDetector?.startMonitoring(packageName)
                    
                    // Start process monitoring
                    processMonitor?.startMonitoring(packageName)
                    
                    Log.i(TAG, "Successfully blocked internet for app: $packageName")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to block internet for app: $packageName", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to block internet for app: $packageName", e)
        }
    }
    
    /**
     * Allow internet access for a specific app
     */
    fun allowAppInternet(packageName: String) {
        try {
            Log.i(TAG, "Allowing internet for app: $packageName")
            
            // Update app block state immediately (synchronous)
            appBlockStates[packageName]?.let { state ->
                appBlockStates[packageName] = state.copy(isBlocked = false)
            }
            
            // Start async operations
            scope.launch {
                try {
                    // Layer 1: App-level unblocking
                    unblockAppLevel(packageName)
                    
                    // Layer 2: System-level unblocking
                    systemLevelController?.allowAppInternet(packageName)
                    
                    // Layer 3: Kernel-level unblocking
                    networkInterfaceController?.allowAppInternet(packageName)
                    
                    // Stop monitoring
                    antiBypassDetector?.stopMonitoring(packageName)
                    processMonitor?.stopMonitoring(packageName)
                    
                    Log.i(TAG, "Successfully allowed internet for app: $packageName")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to allow internet for app: $packageName", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to allow internet for app: $packageName", e)
        }
    }
    
    /**
     * App-level blocking (no root required)
     */
    private fun blockAppLevel(packageName: String, blockState: AppBlockState) {
        try {
            // Create proxy server for the app
            val proxyServer = AppProxyServer(context, packageName, blockState)
            proxyServers[packageName] = proxyServer
            proxyServer.start()
            
            // Create network interceptor
            val interceptor = NetworkInterceptor(context, packageName, blockState)
            networkInterceptors[packageName] = interceptor
            interceptor.start()
            
            // Configure network security
            configureNetworkSecurity(packageName, proxyServer.port)
            
            Log.d(TAG, "App-level blocking configured for: $packageName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure app-level blocking for: $packageName", e)
        }
    }
    
    /**
     * App-level unblocking
     */
    private fun unblockAppLevel(packageName: String) {
        try {
            // Stop proxy server
            proxyServers[packageName]?.stop()
            proxyServers.remove(packageName)
            
            // Stop network interceptor
            networkInterceptors[packageName]?.stop()
            networkInterceptors.remove(packageName)
            
            // Restore network security
            restoreNetworkSecurity(packageName)
            
            Log.d(TAG, "App-level unblocking configured for: $packageName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure app-level unblocking for: $packageName", e)
        }
    }
    
    /**
     * Configure network security for blocked app
     */
    private fun configureNetworkSecurity(packageName: String, proxyPort: Int) {
        try {
            // This would typically involve modifying the app's network security config
            // For now, we'll log the configuration
            Log.d(TAG, "Configuring network security for $packageName with proxy port $proxyPort")
            
            // In a real implementation, you would:
            // 1. Modify the app's network security config
            // 2. Set up proxy settings
            // 3. Configure DNS settings
            // 4. Set up certificate pinning
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure network security for: $packageName", e)
        }
    }
    
    /**
     * Restore network security for unblocked app
     */
    private fun restoreNetworkSecurity(packageName: String) {
        try {
            Log.d(TAG, "Restoring network security for $packageName")
            
            // In a real implementation, you would:
            // 1. Restore original network security config
            // 2. Remove proxy settings
            // 3. Restore DNS settings
            // 4. Remove certificate pinning
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore network security for: $packageName", e)
        }
    }
    
    /**
     * Check if app is currently blocked
     */
    fun isAppBlocked(packageName: String): Boolean {
        return appBlockStates[packageName]?.isBlocked ?: false
    }
    
    /**
     * Get app block state
     */
    fun getAppBlockState(packageName: String): AppBlockState? {
        return appBlockStates[packageName]
    }
    
    /**
     * Get all blocked apps
     */
    fun getAllBlockedApps(): List<String> {
        return appBlockStates.values
            .filter { it.isBlocked }
            .map { it.packageName }
    }
    
    /**
     * Check if root is available
     */
    private fun isRootAvailable(): Boolean {
        return try {
            // Check if we can execute root commands
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
            // Stop all proxy servers
            proxyServers.values.forEach { it.stop() }
            proxyServers.clear()
            
            // Stop all network interceptors
            networkInterceptors.values.forEach { it.stop() }
            networkInterceptors.clear()
            
            // Stop system-level controllers
            systemLevelController?.cleanup()
            antiBypassDetector?.cleanup()
            processMonitor?.cleanup()
            networkInterfaceController?.cleanup()
            
            Log.i(TAG, "PerAppNetworkController cleaned up")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup PerAppNetworkController", e)
        }
    }
}

/**
 * Data class representing app block state
 */
data class AppBlockState(
    val packageName: String,
    val blockWifi: Boolean,
    val blockCellular: Boolean,
    val isBlocked: Boolean,
    val timestamp: Long
)

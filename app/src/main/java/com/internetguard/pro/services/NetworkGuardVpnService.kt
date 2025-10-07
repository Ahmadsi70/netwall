package com.internetguard.pro.services

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Network Guard VPN Service
 * 
 * Provides real network blocking functionality using Android VPN Service.
 * Intercepts all network traffic and blocks specific apps based on rules.
 */
class NetworkGuardVpnService : VpnService() {
    
    companion object {
        private const val TAG = "NetworkGuardVpnService"
        const val ACTION_REFRESH_RULES = "com.internetguard.pro.REFRESH_RULES"
        const val ACTION_START_VPN = "com.internetguard.pro.START_VPN"
        const val ACTION_STOP_VPN = "com.internetguard.pro.STOP_VPN"
        const val ACTION_CHECK_STATUS = "com.internetguard.pro.CHECK_STATUS"
        const val ACTION_BLOCK_APP = "com.internetguard.pro.BLOCK_APP"
        const val ACTION_UNBLOCK_APP = "com.internetguard.pro.UNBLOCK_APP"
        
        private var vpnInterface: ParcelFileDescriptor? = null
        private var isVpnRunning = AtomicBoolean(false)
        private val appUidMap = ConcurrentHashMap<Int, String>()
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val vpnThread = AtomicBoolean(false)
    private var connectivityCallback: ConnectivityManager.NetworkCallback? = null
    @Volatile private var currentTransport: Transport = Transport.NONE
    // Rule refinement based on network type (instance level)
    private val blockedWifiApps = ConcurrentHashMap<String, Boolean>()
    private val blockedCellularApps = ConcurrentHashMap<String, Boolean>()
    // Set of last routed apps inside VPN (to prevent unnecessary restarts)
    private var lastConfiguredAllowedApps: Set<String> = emptySet()
    // VPN Update Manager for batching updates
    private lateinit var vpnUpdateManager: VpnUpdateManager
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "NetworkGuardVpnService created")
        
        // Initialize VPN Update Manager
        vpnUpdateManager = VpnUpdateManager(this, batchDelayMs = 300)
        
        // Start as foreground service
        startForegroundService()
        registerNetworkCallback()
        // Initial determination of active network type
        try {
            val cm = getSystemService(ConnectivityManager::class.java)
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            currentTransport = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> Transport.WIFI
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> Transport.CELLULAR
                else -> Transport.NONE
            }
        } catch (_: Exception) {}
    }
    
    /**
     * Start as foreground service
     */
    private fun startForegroundService() {
        try {
            val notification = createNotification()
            startForeground(1, notification)
            Log.i(TAG, "VPN service started as foreground service")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
        }
    }
    
    /**
     * Create notification for foreground service
     */
    private fun createNotification(): android.app.Notification {
        val channelId = "vpn_service_channel"
        val channelName = "VPN Service"
        val importance = android.app.NotificationManager.IMPORTANCE_LOW
        
        // Create notification channel
        val channel = android.app.NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        
        // Create notification
        return android.app.Notification.Builder(this, channelId)
            .setContentTitle("InternetGuard Pro")
            .setContentText("Protecting your device from unwanted internet access")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_VPN -> {
                val packageName = intent.getStringExtra("block_app")
                if (packageName != null) {
                    // Default: block both networks
                    blockApp(packageName, networkType = intent.getStringExtra("network_type"))
                }
                startVpn()
            }
            ACTION_STOP_VPN -> {
                val packageName = intent.getStringExtra("unblock_app")
                if (packageName != null) {
                    unblockApp(packageName, networkType = intent.getStringExtra("network_type"))
                }
                // Only stop VPN if no apps are blocked
                if (blockedWifiApps.isEmpty() && blockedCellularApps.isEmpty()) {
                    stopVpn()
                }
            }
            ACTION_BLOCK_APP -> {
                val packageName = intent.getStringExtra("package_name")
                if (packageName != null) {
                    blockApp(packageName, networkType = intent.getStringExtra("network_type"))
                }
            }
            ACTION_UNBLOCK_APP -> {
                val packageName = intent.getStringExtra("package_name")
                if (packageName != null) {
                    unblockApp(packageName, networkType = intent.getStringExtra("network_type"))
                }
            }
            ACTION_REFRESH_RULES -> refreshRules()
            ACTION_CHECK_STATUS -> {
                sendVpnStatusBroadcast()
            }
        }
        return START_STICKY
    }
    
    /**
     * Start VPN service
     */
    private fun startVpn() {
        if (isVpnRunning.get()) {
            Log.w(TAG, "VPN already running")
            return
        }
        
        try {
            // Check if VPN permission is already granted
            val intent = VpnService.prepare(this)
            if (intent != null) {
                Log.e(TAG, "VPN permission not granted - cannot start VPN service")
                // Send broadcast to request VPN permission
                sendVpnPermissionRequest()
                return
            }
            
            // Configure VPN interface - Route all traffic through VPN for blocking
            val builder = Builder()
                .setSession("NetworkGuard")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("8.8.8.8")
                // IPv6 support
                .also {
                    try {
                        it.addAddress("fd00:1:2:3::2", 128)
                    } catch (_: Exception) {}
                }

            // Only tunnel blocked apps' traffic inside VPN so we can drop it
            val allowedApps = getActiveBlockedPackages()
            for (pkg in allowedApps) {
                try {
                    builder.addAllowedApplication(pkg)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to allow app in VPN: $pkg", e)
                }
            }

            // If no apps to tunnel, no general routing needed
            if (allowedApps.isNotEmpty()) {
                builder.addRoute("0.0.0.0", 0)
                try { builder.addRoute("::", 0) } catch (_: Exception) {}
            }

            // Log removed for performance optimization

            vpnInterface = builder.establish()
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                return
            }
            
            isVpnRunning.set(true)
            lastConfiguredAllowedApps = allowedApps
            startVpnThread()
            
            Log.i(TAG, "VPN service started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN service", e)
        }
    }
    
    /**
     * Stop VPN service
     */
    private fun stopVpn() {
        try {
            isVpnRunning.set(false)
            vpnThread.set(false)
            
            vpnInterface?.close()
            vpnInterface = null
            
            Log.i(TAG, "VPN service stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop VPN service", e)
        }
    }
    
    /**
     * Refresh blocking rules
     */
    private fun refreshRules() {
        Log.d(TAG, "Refreshing blocking rules")
        // Rules will be updated by the main app
    }
    
    /**
     * Start VPN thread to handle traffic - Optimized for better performance
     */
    private fun startVpnThread() {
        if (vpnThread.get()) {
            Log.w(TAG, "VPN thread already running")
            return
        }
        
        vpnThread.set(true)
        scope.launch(Dispatchers.IO) { // Specify dispatcher for I/O operations
            try {
                val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
                
                // Optimization: use larger buffer for better performance
                val buffer = ByteBuffer.allocateDirect(65536) // 64KB buffer
                val tempArray = ByteArray(65536)
                
                // Optimization: cache blocked apps list
                var cachedBlockedApps = getActiveBlockedPackages()
                var lastCacheUpdate = System.currentTimeMillis()
                val cacheUpdateInterval = 5000L // 5 seconds
                
                while (vpnThread.get() && isVpnRunning.get()) {
                    try {
                        val bytesRead = vpnInput.read(tempArray)
                        if (bytesRead > 0) {
                            // Update blocked apps cache
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastCacheUpdate > cacheUpdateInterval) {
                                cachedBlockedApps = getActiveBlockedPackages()
                                lastCacheUpdate = currentTime
                            }
                            
                            // If no apps blocked, don't process
                            if (cachedBlockedApps.isEmpty()) {
                                continue
                            }
                            
                            buffer.clear()
                            buffer.put(tempArray, 0, bytesRead)
                            buffer.flip()
                            
                            // Process packet - all packets are dropped
                            // (because only blocked apps are routed to VPN)
                        }
                    } catch (e: Exception) {
                        // Continue loop in case of error in one packet
                        if (vpnThread.get()) {
                            Thread.sleep(10) // Brief rest
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in VPN thread", e)
            } finally {
                vpnThread.set(false)
            }
        }
    }
    
    /**
     * Process network packet - Block traffic for blocked apps
     */
    fun processPacket(packet: ByteBuffer): PacketResult {
        try {
            // If no blocked apps for active network, allow packet exchange (no packets should enter VPN)
            val activeBlocked = getActiveBlockedPackages()
            if (activeBlocked.isEmpty()) {
                return PacketResult(false, null)
            }

            // Since only blocked apps are routed to VPN, all packets should be dropped
            return PacketResult(true, null)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet", e)
            return PacketResult(false, null)
        }
    }
    
    /**
     * Check if app is blocked
     */
    private fun isAppBlocked(packageName: String): Boolean {
        return (blockedWifiApps[packageName] == true) || (blockedCellularApps[packageName] == true)
    }
    
    /**
     * Get source IP from packet
     */
    private fun getSourceIpFromPacket(packet: ByteBuffer): String? {
        try {
            if (packet.remaining() < 20) return null // Minimum IP header size
            
            // Read IP header
            val versionAndIhl = packet.get(0).toInt() and 0xFF
            val version = versionAndIhl shr 4
            if (version != 4) return null // Only IPv4
            
            val ihl = versionAndIhl and 0x0F
            if (ihl < 5) return null // Invalid header length
            
            // Extract source IP address (bytes 12-15)
            val sourceIp = StringBuilder()
            for (i in 12..15) {
                if (i > 12) sourceIp.append(".")
                sourceIp.append(packet.get(i).toInt() and 0xFF)
            }
            
            return sourceIp.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting source IP from packet", e)
            return null
        }
    }
    
    /**
     * Get package name for IP address - Map IP to package using /proc/net/tcp
     */
    private fun getPackageNameForIp(ip: String): String? {
        try {
            // Read /proc/net/tcp to find which process owns the connection
            val process = Runtime.getRuntime().exec("cat /proc/net/tcp")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val parts = line!!.split("\\s+".toRegex())
                if (parts.size >= 4) {
                    val localAddress = parts[1]
                    val localIp = convertHexToIp(localAddress.split(":")[0])
                    
                    if (localIp == ip) {
                        val uid = parts[7].toIntOrNull()
                        if (uid != null) {
                            val packageName = getPackageNameByUid(uid)
                            if (packageName != null && isAppBlocked(packageName)) {
                return packageName
                            }
                        }
                    }
                }
            }
            
            reader.close()
            process.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping IP to package: $ip", e)
        }
        
        return null
    }
    
    /**
     * Convert hex IP to dotted decimal format
     */
    private fun convertHexToIp(hex: String): String {
        try {
            val ip = hex.toLong(16)
            return "${(ip and 0xFF)}.${(ip shr 8) and 0xFF}.${(ip shr 16) and 0xFF}.${(ip shr 24) and 0xFF}"
        } catch (e: Exception) {
            Log.e(TAG, "Error converting hex to IP: $hex", e)
            return ""
        }
    }
    
    
    /**
     * Get package name by UID - Cached for performance
     */
    private fun getPackageNameByUid(uid: Int): String? {
        return appUidMap[uid] ?: run {
            try {
                val packageManager = packageManager
                val packages = packageManager.getInstalledPackages(0)
                
                for (packageInfo in packages) {
                    if (packageInfo.applicationInfo.uid == uid) {
                        val packageName = packageInfo.packageName
                        appUidMap[uid] = packageName
                        return packageName
                    }
                }
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error getting package name for UID: $uid", e)
                null
            }
        }
    }
    
    
    
    /**
     * Block internet for selected app
     */
    	fun blockApp(packageName: String, networkType: String? = null) {
		val type = networkType?.lowercase()
		val blockWifi = type == "wifi" || type == null
		val blockCellular = type == "cellular" || type == null
		Log.d(TAG, "Queue block for app: $packageName on ${type ?: "all"}")
		vpnUpdateManager.queueBlockUpdate(packageName, blockWifi = blockWifi, blockCellular = blockCellular)
	}
    
    /**
     * Allow internet for selected app
     */
    	fun unblockApp(packageName: String, networkType: String? = null) {
		val type = networkType?.lowercase()
		val unWifi = type == "wifi" || type == null
		val unCellular = type == "cellular" || type == null
		Log.d(TAG, "Queue unblock for app: $packageName on ${type ?: "all"}")
		vpnUpdateManager.queueUnblockUpdate(packageName, unblockWifi = unWifi, unblockCellular = unCellular)
	}
    
    /**
     * Apply batch updates from VpnUpdateManager
     * This is much more efficient than calling blockApp/unblockApp multiple times
     */
    fun applyBatchUpdates(updates: List<AppBlockUpdate>) {
        if (updates.isEmpty()) {
            Log.d(TAG, "No updates to apply")
            return
        }
        
        Log.i(TAG, "Applying ${updates.size} batched updates")
        
        // Apply all updates to internal maps
        var hasChanges = false
        for (update in updates) {
            if (update.shouldRemove) {
                // Remove from both maps
                val wifiRemoved = blockedWifiApps.remove(update.packageName) != null
                val cellularRemoved = blockedCellularApps.remove(update.packageName) != null
                hasChanges = hasChanges || wifiRemoved || cellularRemoved
                Log.d(TAG, "Removed ${update.packageName} from block list")
            } else {
                // Add/update blocking rules
                if (update.blockWifi) {
                    blockedWifiApps[update.packageName] = true
                    hasChanges = true
                } else {
                    val removed = blockedWifiApps.remove(update.packageName) != null
                    hasChanges = hasChanges || removed
                }
                
                if (update.blockCellular) {
                    blockedCellularApps[update.packageName] = true
                    hasChanges = true
                } else {
                    val removed = blockedCellularApps.remove(update.packageName) != null
                    hasChanges = hasChanges || removed
                }
                
                Log.d(TAG, "Updated ${update.packageName}: WiFi=${update.blockWifi}, Cellular=${update.blockCellular}")
            }
        }
        
        if (!hasChanges) {
            Log.d(TAG, "No actual changes detected, skipping VPN reconfiguration")
            return
        }
        
        // Reconfigure VPN once for all changes
        if (blockedWifiApps.isEmpty() && blockedCellularApps.isEmpty()) {
            stopVpn()
            Log.d(TAG, "All apps unblocked - VPN stopped")
        } else if (!isVpnRunning.get()) {
            startVpn()
            Log.d(TAG, "VPN started with ${blockedWifiApps.size + blockedCellularApps.size} blocked apps")
        } else {
            reconfigureVpnIfNeeded()
            Log.d(TAG, "VPN reconfigured with batch updates")
        }
    }
    
    /**
     * Check if VPN is running
     */
    fun isRunning(): Boolean {
        return isVpnRunning.get()
    }
    
    /**
     * Get list of blocked apps
     */
    fun getBlockedApps(): Set<String> {
        return (blockedWifiApps.keys + blockedCellularApps.keys).toSet()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            val cm = getSystemService(ConnectivityManager::class.java)
            connectivityCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (_: Exception) {}
        
        // Release VPN Update Manager
        if (::vpnUpdateManager.isInitialized) {
            vpnUpdateManager.release()
        }
        
        stopVpn()
        Log.i(TAG, "NetworkGuardVpnService destroyed")
    }
    
    /**
     * Send broadcast to request VPN permission
     */
    private fun sendVpnPermissionRequest() {
        try {
            val intent = Intent("com.internetguard.pro.REQUEST_VPN_PERMISSION")
            intent.putExtra("message", "VPN permission required for app blocking")
            sendBroadcast(intent)
            Log.i(TAG, "VPN permission request broadcast sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send VPN permission request", e)
        }
    }
    
    /**
     * Send VPN status broadcast to dashboard
     */
    private fun sendVpnStatusBroadcast() {
        try {
            val intent = Intent("com.internetguard.pro.VPN_STATUS_UPDATE")
            intent.putExtra("is_connected", isVpnRunning.get())
            val allBlocked = getBlockedApps()
            intent.putExtra("blocked_apps_count", allBlocked.size)
            intent.putExtra("blocked_apps", allBlocked.toTypedArray())
            sendBroadcast(intent)
            Log.i(TAG, "VPN status broadcast sent: connected=${isVpnRunning.get()}, blocked=${allBlocked.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send VPN status broadcast", e)
        }
    }
    
    /**
     * Data class for packet processing result
     */
    data class PacketResult(
        val shouldBlock: Boolean,
        val packageName: String?
    )
    
    // ===== Helpers (class level) =====
    private fun getActiveBlockedPackages(): Set<String> {
        return when (currentTransport) {
            Transport.WIFI -> blockedWifiApps.keys.toSet()
            Transport.CELLULAR -> blockedCellularApps.keys.toSet()
            Transport.NONE -> emptySet()
        }
    }

    private fun reconfigureVpnIfNeeded() {
        val current = getActiveBlockedPackages()
        if (current == lastConfiguredAllowedApps) {
            Log.d(TAG, "Reconfiguration not needed; allowed set unchanged")
            return
        }
        if (isRunning()) {
            Log.d(TAG, "Reconfiguring VPN with allowed apps: $current")
            stopVpn()
            startVpn()
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = getSystemService(ConnectivityManager::class.java)
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // Update network type cache and reconfigure if needed
                    updateCurrentTransport()
                    reconfigureVpnIfNeeded()
                }
                override fun onLost(network: Network) {
                    updateCurrentTransport()
                    reconfigureVpnIfNeeded()
                }
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    updateCurrentTransport(networkCapabilities)
                    reconfigureVpnIfNeeded()
                }
            }
            cm.registerDefaultNetworkCallback(callback)
            connectivityCallback = callback
            Log.d(TAG, "NetworkCallback registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    private fun updateCurrentTransport(caps: NetworkCapabilities? = null) {
        try {
            val capabilities = caps ?: getSystemService(ConnectivityManager::class.java).getNetworkCapabilities(
                getSystemService(ConnectivityManager::class.java).activeNetwork
            )
            currentTransport = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> Transport.WIFI
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> Transport.CELLULAR
                else -> Transport.NONE
            }
        } catch (_: Exception) {}
    }
}

private enum class Transport { WIFI, CELLULAR, NONE }

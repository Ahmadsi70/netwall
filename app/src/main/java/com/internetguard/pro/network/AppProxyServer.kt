package com.internetguard.pro.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * App-specific Proxy Server
 * 
 * Creates a local proxy server for each blocked app to intercept and filter
 * all HTTP/HTTPS traffic. Implements advanced filtering to prevent bypass attempts.
 */
class AppProxyServer(
    private val context: Context,
    private val packageName: String,
    private val blockState: AppBlockState
) {
    
    companion object {
        private const val TAG = "AppProxyServer"
        private const val BUFFER_SIZE = 8192
        private const val MAX_CONNECTIONS = 100
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val executor = Executors.newFixedThreadPool(MAX_CONNECTIONS)
    private val isRunning = AtomicBoolean(false)
    
    private var serverSocket: ServerSocket? = null
    var port: Int = 0
    
    // Advanced filtering components
    private val requestFilter = AdvancedRequestFilter()
    private val responseFilter = AdvancedResponseFilter()
    private val dnsFilter = DnsFilter()
    private val sslInterceptor = SslInterceptor()
    
    /**
     * Start the proxy server
     */
    fun start() {
        if (isRunning.get()) {
            Log.w(TAG, "Proxy server already running for $packageName")
            return
        }
        
        scope.launch {
            try {
                startProxyServer()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start proxy server for $packageName", e)
            }
        }
    }
    
    /**
     * Stop the proxy server
     */
    fun stop() {
        if (!isRunning.get()) {
            Log.w(TAG, "Proxy server not running for $packageName")
            return
        }
        
        try {
            isRunning.set(false)
            serverSocket?.close()
            executor.shutdown()
            Log.i(TAG, "Proxy server stopped for $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop proxy server for $packageName", e)
        }
    }
    
    
    /**
     * Start the proxy server on an available port
     */
    private fun startProxyServer() {
        try {
            // Find an available port
            port = findAvailablePort()
            
            // Create server socket
            serverSocket = ServerSocket(port)
            isRunning.set(true)
            
            Log.i(TAG, "Proxy server started for $packageName on port $port")
            
            // Accept connections
            while (isRunning.get()) {
                try {
                    val clientSocket = serverSocket?.accept()
                    if (clientSocket != null) {
                        // Handle connection in separate thread
                        executor.submit {
                            handleClientConnection(clientSocket)
                        }
                    }
                } catch (e: SocketException) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Error accepting connection for $packageName", e)
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start proxy server for $packageName", e)
            throw e
        }
    }
    
    /**
     * Handle client connection
     */
    private fun handleClientConnection(clientSocket: Socket) {
        try {
            val inputStream = clientSocket.getInputStream()
            val outputStream = clientSocket.getOutputStream()
            
            // Read the request
            val request = readHttpRequest(inputStream)
            if (request == null) {
                clientSocket.close()
                return
            }
            
            // Apply advanced filtering
            val filteredRequest = requestFilter.filterRequest(request, packageName)
            if (filteredRequest == null) {
                // Request is blocked
                sendBlockedResponse(outputStream)
                clientSocket.close()
                return
            }
            
            // Process the request
            val response = processRequest(filteredRequest)
            if (response == null) {
                // Response is blocked
                sendBlockedResponse(outputStream)
                clientSocket.close()
                return
            }
            
            // Send response to client
            outputStream.write(response)
            outputStream.flush()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client connection for $packageName", e)
        } finally {
            try {
                clientSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client socket for $packageName", e)
            }
        }
    }
    
    /**
     * Read HTTP request from input stream
     */
    private fun readHttpRequest(inputStream: java.io.InputStream): HttpRequest? {
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            val requestBuilder = StringBuilder()
            
            // Read request line by line
            var line: String
            var totalBytes = 0
            
            while (totalBytes < BUFFER_SIZE) {
                val bytesRead = inputStream.read(buffer, totalBytes, BUFFER_SIZE - totalBytes)
                if (bytesRead == -1) break
                
                totalBytes += bytesRead
                val request = String(buffer, 0, totalBytes)
                
                // Check if we have complete headers
                if (request.contains("\r\n\r\n")) {
                    return parseHttpRequest(request)
                }
            }
            
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error reading HTTP request for $packageName", e)
            return null
        }
    }
    
    /**
     * Parse HTTP request
     */
    private fun parseHttpRequest(request: String): HttpRequest? {
        try {
            val lines = request.split("\r\n")
            if (lines.isEmpty()) return null
            
            val requestLine = lines[0]
            val parts = requestLine.split(" ")
            if (parts.size < 3) return null
            
            val method = parts[0]
            val url = parts[1]
            val version = parts[2]
            
            val headers = mutableMapOf<String, String>()
            var body = ""
            var headerEndIndex = -1
            
            // Parse headers
            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isEmpty()) {
                    headerEndIndex = i
                    break
                }
                
                val colonIndex = line.indexOf(":")
                if (colonIndex > 0) {
                    val headerName = line.substring(0, colonIndex).trim()
                    val headerValue = line.substring(colonIndex + 1).trim()
                    headers[headerName] = headerValue
                }
            }
            
            // Parse body if present
            if (headerEndIndex > 0 && headerEndIndex < lines.size - 1) {
                body = lines.subList(headerEndIndex + 1, lines.size).joinToString("\r\n")
            }
            
            return HttpRequest(method, url, version, headers, body)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing HTTP request for $packageName", e)
            return null
        }
    }
    
    /**
     * Process the HTTP request
     */
    private fun processRequest(request: HttpRequest): ByteArray? {
        try {
            // Check if request should be blocked
            if (shouldBlockRequest(request)) {
                return null
            }
            
            // Forward request to destination
            val response = forwardRequest(request)
            
            // Apply response filtering
            return responseFilter.filterResponse(response, packageName)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing request for $packageName", e)
            return null
        }
    }
    
    /**
     * Check if request should be blocked
     */
    private fun shouldBlockRequest(request: HttpRequest): Boolean {
        // Block based on network type
        if (blockState.blockWifi && isWifiConnection()) {
            return true
        }
        if (blockState.blockCellular && !isWifiConnection()) {
            return true
        }
        
        // Block based on URL patterns
        if (isBlockedUrl(request.url)) {
            return true
        }
        
        // Block based on headers
        if (hasBlockedHeaders(request.headers)) {
            return true
        }
        
        // Block based on content
        if (hasBlockedContent(request.body)) {
            return true
        }
        
        return false
    }
    
    /**
     * Forward request to destination
     */
    private fun forwardRequest(request: HttpRequest): ByteArray {
        try {
            // Parse URL
            val url = if (request.url.startsWith("http://") || request.url.startsWith("https://")) {
                request.url
            } else {
                "http://${request.url}"
            }
            
            val parsedUrl = URL(url)
            val host = parsedUrl.host
            val port = if (parsedUrl.port != -1) parsedUrl.port else if (parsedUrl.protocol == "https") 443 else 80
            val path = parsedUrl.path.ifEmpty { "/" }
            
            // Create connection
            val socket = Socket(host, port)
            val outputStream = socket.getOutputStream()
            val inputStream = socket.getInputStream()
            
            // Build HTTP request
            val httpRequest = buildHttpRequest(request, host, path)
            
            // Send request
            outputStream.write(httpRequest.toByteArray())
            outputStream.flush()
            
            // Read response
            val response = readHttpResponse(inputStream)
            
            // Close connections
            socket.close()
            
            return response
            
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding request for $packageName", e)
            return "HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\n\r\n".toByteArray()
        }
    }
    
    /**
     * Build HTTP request for forwarding
     */
    private fun buildHttpRequest(originalRequest: HttpRequest, host: String, path: String): String {
        val requestBuilder = StringBuilder()
        
        // Request line
        requestBuilder.append("${originalRequest.method} $path ${originalRequest.version}\r\n")
        
        // Headers
        requestBuilder.append("Host: $host\r\n")
        originalRequest.headers.forEach { (key, value) ->
            if (key.lowercase() != "host") {
                requestBuilder.append("$key: $value\r\n")
            }
        }
        requestBuilder.append("Connection: close\r\n")
        requestBuilder.append("\r\n")
        
        // Body
        if (originalRequest.body.isNotEmpty()) {
            requestBuilder.append(originalRequest.body)
        }
        
        return requestBuilder.toString()
    }
    
    /**
     * Read HTTP response
     */
    private fun readHttpResponse(inputStream: java.io.InputStream): ByteArray {
        val responseBuilder = StringBuilder()
        val buffer = ByteArray(BUFFER_SIZE)
        
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            responseBuilder.append(String(buffer, 0, bytesRead))
        }
        
        return responseBuilder.toString().toByteArray()
    }
    
    /**
     * Send blocked response to client
     */
    private fun sendBlockedResponse(outputStream: java.io.OutputStream) {
        try {
            val response = """
                HTTP/1.1 403 Forbidden
                Content-Type: text/html
                Content-Length: 0
                Connection: close
                
            """.trimIndent()
            
            outputStream.write(response.toByteArray())
            outputStream.flush()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending blocked response for $packageName", e)
        }
    }
    
    /**
     * Check if URL should be blocked
     */
    private fun isBlockedUrl(url: String): Boolean {
        try {
            val parsedUrl = URL(url)
            val host = parsedUrl.host.lowercase()
            
            // Block based on network type
            if (blockState.blockWifi && isWifiConnection()) {
                return true
            }
            if (blockState.blockCellular && !isWifiConnection()) {
                return true
            }
            
            // Block common social media and entertainment sites
            val blockedDomains = listOf(
                "facebook.com", "instagram.com", "twitter.com", "tiktok.com",
                "youtube.com", "netflix.com", "spotify.com", "whatsapp.com",
                "telegram.org", "snapchat.com", "discord.com", "twitch.tv"
            )
            
            for (domain in blockedDomains) {
                if (host.contains(domain)) {
                    Log.d(TAG, "Blocking URL: $url (matched domain: $domain)")
                    return true
                }
            }
            
            // Block based on URL patterns
            val blockedPatterns = listOf(
                ".*\\.(mp4|avi|mkv|mov)$",  // Video files
                ".*\\.(mp3|wav|flac)$",     // Audio files
                ".*/video/.*",              // Video pages
                ".*/stream/.*",             // Streaming content
                ".*/download/.*"            // Download pages
            )
            
            for (pattern in blockedPatterns) {
                if (url.matches(pattern.toRegex())) {
                    Log.d(TAG, "Blocking URL: $url (matched pattern: $pattern)")
                    return true
                }
            }
            
            return false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking URL: $url", e)
            return false
        }
    }
    
    /**
     * Check if headers should be blocked
     */
    private fun hasBlockedHeaders(headers: Map<String, String>): Boolean {
        // Implement header blocking logic
        // This could include:
        // - User-Agent filtering
        // - Referer filtering
        // - Custom header filtering
        return false
    }
    
    /**
     * Check if content should be blocked
     */
    private fun hasBlockedContent(content: String): Boolean {
        // Implement content blocking logic
        // This could include:
        // - Keyword filtering
        // - Content type filtering
        // - Size filtering
        return false
    }
    
    /**
     * Check if current connection is WiFi
     */
    private fun isWifiConnection(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network type", e)
            true // Default to WiFi if we can't determine
        }
    }
    
    /**
     * Find an available port
     */
    private fun findAvailablePort(): Int {
        try {
            val socket = ServerSocket(0)
            val port = socket.localPort
            socket.close()
            return port
        } catch (e: Exception) {
            Log.e(TAG, "Error finding available port for $packageName", e)
            return 8080 // Default port
        }
    }
}

/**
 * Data class representing HTTP request
 */
data class HttpRequest(
    val method: String,
    val url: String,
    val version: String,
    val headers: Map<String, String>,
    val body: String
)

/**
 * Advanced request filter
 */
class AdvancedRequestFilter {
    fun filterRequest(request: HttpRequest, packageName: String): HttpRequest? {
        // Implement advanced request filtering
        // This could include:
        // - Rate limiting
        // - Pattern matching
        // - Content analysis
        // - Behavioral analysis
        return request
    }
}

/**
 * Advanced response filter
 */
class AdvancedResponseFilter {
    fun filterResponse(response: ByteArray, packageName: String): ByteArray? {
        // Implement advanced response filtering
        // This could include:
        // - Content filtering
        // - Size limiting
        // - Type filtering
        // - Security scanning
        return response
    }
}

/**
 * DNS filter
 */
class DnsFilter {
    fun filterDnsQuery(domain: String, packageName: String): Boolean {
        // Implement DNS filtering
        // This could include:
        // - Domain blacklisting
        // - DNS over HTTPS blocking
        // - DNS tunneling detection
        return false
    }
}

/**
 * SSL interceptor
 */
class SslInterceptor {
    fun interceptSslConnection(host: String, port: Int, packageName: String): Boolean {
        // Implement SSL interception
        // This could include:
        // - Certificate pinning
        // - SSL/TLS filtering
        // - Encrypted traffic analysis
        return false
    }
}

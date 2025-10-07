package com.internetguard.pro.services

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.R
import com.internetguard.pro.ai.CustomSmartDetector
import com.internetguard.pro.ai.api.ModerationClient
import com.internetguard.pro.ai.api.LocalBackendClient
import com.internetguard.pro.utils.TextRedactor
import com.internetguard.pro.ai.UserAction
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.entities.KeywordLogs
import com.internetguard.pro.data.entities.AppBlockRules
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Accessibility service for monitoring and blocking content containing blacklisted keywords.
 * 
 * Monitors text inputs, search bars, and other UI elements across all apps
 * to detect and block inappropriate content.
 */
class KeywordAccessibilityService : AccessibilityService() {
	
	companion object {
		private const val TAG = "KeywordAccessibilityService"
		private const val NOTIFICATION_CHANNEL_ID = "keyword_blocking_channel"
		private const val NOTIFICATION_ID = 1001
	}
	
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val database by lazy { (application as InternetGuardProApp).database }
	private val appRepository by lazy { com.internetguard.pro.data.repository.AppRepository(database.appBlockRulesDao(), applicationContext) }
	private val keywordCache = ConcurrentHashMap<Long, KeywordBlacklist>()
	// Optimization: HashMap for fast keyword search
	private val keywordHashMap = ConcurrentHashMap<String, KeywordBlacklist>()
	private val keywordHashMapLowercase = ConcurrentHashMap<String, KeywordBlacklist>()
	private var lastCacheUpdate = 0L
	private val cacheValidityDuration = 30000L // 30 seconds
	
    // AI engines
    private lateinit var customAI: CustomSmartDetector
    private var isAIEnabled = true // Opt-in for cloud moderation
    private var aiInitialized = false
    private var moderationClient: LocalBackendClient? = null
	
	override fun onServiceConnected() {
		super.onServiceConnected()
		Log.d(TAG, "Accessibility service connected")
		createNotificationChannel()
		loadKeywordsToCache()
		initializeAI()
	}
	
	override fun onAccessibilityEvent(event: AccessibilityEvent?) {
		event ?: return
		
		when (event.eventType) {
			AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
			AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED,
			AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
				checkForBlockedContent(event)
			}
		}
	}
	
	override fun onInterrupt() {
		Log.d(TAG, "Accessibility service interrupted")
	}
	
	/**
	 * Initializes Custom AI Engine for enhanced keyword detection
	 */
	private fun initializeAI() {
		serviceScope.launch {
			try {
                customAI = CustomSmartDetector(applicationContext)
                aiInitialized = true
                Log.i(TAG, "Custom AI Engine initialized successfully")

                // Initialize cloud moderation client if opted-in (using local backend)
                val optIn = getCloudOptInFromPrefs()
                if (optIn) {
                    // Use local backend server with subscription system
                    val backendUrl = com.internetguard.pro.ai.api.RemoteConfig.MODERATE_URL
                    moderationClient = LocalBackendClient(endpoint = backendUrl, timeoutMs = 2500)
                    Log.i(TAG, "Local backend client initialized with subscription system")
                } else {
                    moderationClient = null
                    Log.i(TAG, "Cloud moderation disabled")
                }
			} catch (e: Exception) {
				Log.e(TAG, "Error initializing Custom AI Engine", e)
				isAIEnabled = false
				aiInitialized = false
			}
		}
	}
	
	/**
	 * Creates notification channel for blocking notifications
	 */
	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				NOTIFICATION_CHANNEL_ID,
				"Keyword Blocking",
				NotificationManager.IMPORTANCE_DEFAULT
			).apply {
				description = "Notifications for blocked content"
			}
			
			val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}
	
	/**
	 * Loads keywords from database into cache with HashMap optimization
	 */
	private fun loadKeywordsToCache() {
		serviceScope.launch {
			try {
				database.keywordBlacklistDao().observeAll()
					.collect { keywordList: List<KeywordBlacklist> ->
						// Clear old caches
						keywordCache.clear()
						keywordHashMap.clear()
						keywordHashMapLowercase.clear()
						
						// Load into different caches for optimization
						keywordList.forEach { keyword: KeywordBlacklist ->
							keywordCache[keyword.id] = keyword
							
							// HashMap for fast search
							keywordHashMap[keyword.keyword] = keyword
							keywordHashMapLowercase[keyword.keyword.lowercase()] = keyword
							
							// Add single words for better search
							keyword.keyword.split(" ").forEach { word ->
								if (word.length > 2) { // Only words longer than 2 characters
									keywordHashMap[word] = keyword
									keywordHashMapLowercase[word.lowercase()] = keyword
								}
							}
						}
						
						lastCacheUpdate = System.currentTimeMillis()
						// Log removed for performance optimization in production
					}
			} catch (e: Exception) {
				Log.e(TAG, "Failed to load keywords", e)
			}
		}
	}
	
	/**
	 * Checks if content contains blocked keywords
	 */
	private fun checkForBlockedContent(event: AccessibilityEvent) {
		val source = event.source
		val packageName = event.packageName?.toString() ?: return
		
		// Skip system apps and this app itself
		if (packageName.startsWith("android") || 
			packageName == applicationContext.packageName) {
			return
		}
		
		// Check if cache needs refresh
		if (System.currentTimeMillis() - lastCacheUpdate > cacheValidityDuration) {
			loadKeywordsToCache()
		}
		
		// Extract text from multiple sources
		val textContent = buildString {
			// From event text
			event.text?.forEach { text ->
				if (!text.isNullOrBlank()) {
					append(text.toString())
					append(" ")
				}
			}
			
			// From node content
			source?.let { node ->
				node.text?.let { append(it.toString()).append(" ") }
				node.contentDescription?.let { append(it.toString()).append(" ") }
			}
		}.trim()
		
		if (textContent.isBlank()) return
		
		Log.v(TAG, "Checking text in $packageName: ${textContent.take(50)}")
		
		// Check against app-specific blacklisted keywords
		serviceScope.launch {
			val blockedKeyword = findBlockedKeywordForApp(textContent, packageName)
			if (blockedKeyword != null) {
				Log.i(TAG, "Blocked keyword '${blockedKeyword.keyword}' found in $packageName")
				source?.let { handleBlockedContent(blockedKeyword, textContent, packageName, it) }
			}
		}
	}
	
	/**
	 * Finds blocked keyword in text content for specific app using Custom AI
	 */
    private suspend fun findBlockedKeywordForApp(text: String, packageName: String): KeywordBlacklist? {
        // 🚀 FIXED: Check traditional keyword matching FIRST (user-defined keywords have priority)
        val traditionalResult = findBlockedKeywordTraditional(text, packageName)
        if (traditionalResult != null) {
            Log.d(TAG, "Traditional keyword matching found blocked content: ${traditionalResult.keyword}")
            return traditionalResult
        }
        
        // 1) Try cloud moderation (opt-in) - only if no traditional keywords matched
        moderationClient?.let { client ->
            try {
                val redacted = TextRedactor.redact(text)
                val langHint = detectLanguageHint(text)
                val res = client.moderate(redacted, langHint)
                // Decision based on action in server response; if not available, use confidence
                val shouldBlock = when (res.action) {
                    "block" -> true
                    "review" -> res.confidence > 0.7f
                    else -> res.isInappropriate && res.confidence > 0.7f
                }
                if (shouldBlock) {
                    Log.d(TAG, "Cloud moderation flagged content: ${res.category}")
                    return KeywordBlacklist(
                        id = -1,
                        keyword = "CLOUD_${res.category ?: "inappropriate"}",
                        category = res.category,
                        caseSensitive = false,
                        language = res.language ?: "mixed",
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    // Cloud moderation did not flag content
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cloud moderation error", e)
            }
        }

        // 2) Local custom AI - only if no traditional keywords matched
        if (isAIEnabled && aiInitialized && ::customAI.isInitialized) {
            try {
                val aiResult = customAI.detectContent(text)
                if (aiResult.isInappropriate && aiResult.confidence > 0.7f) {
                    Log.d(TAG, "Custom AI detected inappropriate content: ${aiResult.reasoning}")
                    return KeywordBlacklist(
                        id = -1,
                        keyword = "CUSTOM_AI_${aiResult.category}",
                        category = aiResult.category,
                        caseSensitive = false,
                        language = aiResult.language ?: "mixed",
                        createdAt = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in Custom AI detection", e)
            }
        }

        // No blocking content found
        return null
    }

    private fun getCloudOptInFromPrefs(): Boolean {
        return try {
            val prefs = getSharedPreferences("cloud_moderation_prefs", Context.MODE_PRIVATE)
            // Default is enabled so feature is available unless user has disabled it
            prefs.getBoolean("opt_in", true)
        } catch (_: Exception) { true }
    }



    private fun detectLanguageHint(text: String): String? {
        // Style detection: if Persian characters dominate → fa, if Arabic → ar, otherwise null
        val arPersian = text.count { it in '\u0600'..'\u06FF' }
        val latin = text.count { it in 'A'..'Z' || it in 'a'..'z' }
        return when {
            arPersian > latin && arPersian > 2 -> "fa"
            latin > arPersian && latin > 2 -> "en"
            else -> null
        }
    }
	
	/**
	 * Traditional keyword matching (original method) - FIXED
	 */
	private suspend fun findBlockedKeywordTraditional(text: String, packageName: String): KeywordBlacklist? {
		// Get app-specific keyword rules
		val appRules = database.appKeywordRulesDao().getActiveRulesForApp(packageName)
		
		// Check app-specific keywords first
		for (rule in appRules) {
			val keyword = keywordCache[rule.keywordId]
			if (keyword != null && matchesKeywordEnhanced(text, keyword)) {
				Log.d(TAG, "Found app-specific blocked keyword: ${keyword.keyword} for app: $packageName")
				return keyword
			}
		}
		
		// 🚀 FIXED: Always check global keywords (keywords without specific app assignments)
		// Get all keywords that don't have specific app rules
		val allKeywords = keywordCache.values
		val globalKeywords = allKeywords.filter { keyword ->
			// Check if this keyword has any app-specific rules
			val hasAppRules = database.appKeywordRulesDao().getRulesForKeywordSync(keyword.id).isNotEmpty()
			!hasAppRules // Global keywords are those without app-specific rules
		}
		
		// Check global keywords
		for (keyword in globalKeywords) {
			if (matchesKeywordEnhanced(text, keyword)) {
				Log.d(TAG, "Found global blocked keyword: ${keyword.keyword} for app: $packageName")
				return keyword
			}
		}
		
		Log.v(TAG, "No blocked keywords found for app: $packageName in text: ${text.take(50)}")
		return null
	}
	
	/**
	 * Extracts text content from accessibility node
	 */
	private fun extractTextFromNode(node: AccessibilityNodeInfo): String {
		val text = node.text?.toString() ?: ""
		val contentDescription = node.contentDescription?.toString() ?: ""
		return "$text $contentDescription".trim()
	}
	
	/**
	 * Finds blocked keyword in text content - Optimized with HashMap
	 */
	private fun findBlockedKeyword(text: String): KeywordBlacklist? {
		// Optimization: Fast search with HashMap
		val words = text.split(Regex("\\s+"))
		
		// Direct search in HashMap
		for (word in words) {
			// Exact search
			keywordHashMap[word]?.let { return it }
			
			// Case-insensitive search
			keywordHashMapLowercase[word.lowercase()]?.let { keyword ->
				if (!keyword.caseSensitive) return keyword
			}
		}
		
		// Full text search for multi-word phrases
		keywordHashMap[text]?.let { return it }
		keywordHashMapLowercase[text.lowercase()]?.let { keyword ->
			if (!keyword.caseSensitive) return keyword
		}
		
		// Fallback: Traditional search only for complex cases
		for (keyword in keywordCache.values) {
			if (text.contains(keyword.keyword, ignoreCase = !keyword.caseSensitive)) {
				if (matchesKeywordEnhanced(text, keyword)) {
					return keyword
				}
			}
		}
		
		return null
	}
	
	/**
	 * Enhanced keyword matching with better detection capabilities
	 */
	private fun matchesKeywordEnhanced(text: String, keyword: KeywordBlacklist): Boolean {
		val keywordText = keyword.keyword
		val searchText = if (keyword.caseSensitive) text else text.lowercase()
		val searchKeyword = if (keyword.caseSensitive) keywordText else keywordText.lowercase()
		
		// Basic containment check
		if (searchText.contains(searchKeyword)) {
			return true
		}
		
		// Enhanced matching for character substitutions
		val normalizedText = normalizeText(searchText)
		val normalizedKeyword = normalizeText(searchKeyword)
		
		if (normalizedText.contains(normalizedKeyword)) {
			return true
		}
		
		// Check for word boundaries to avoid false positives
		if (searchKeyword.length > 3) {
			val wordBoundaryPattern = "\\b${Regex.escape(searchKeyword)}\\b".toRegex(RegexOption.IGNORE_CASE)
			if (wordBoundaryPattern.containsMatchIn(searchText)) {
				return true
			}
		}
		
		return false
	}
	
	/**
	 * Normalizes text to catch character substitutions (e.g., p0rn -> porn)
	 */
	private fun normalizeText(text: String): String {
		return text
			.replace("0", "o")
			.replace("1", "i")
			.replace("3", "e")
			.replace("4", "a")
			.replace("5", "s")
			.replace("7", "t")
			.replace("@", "a")
			.replace("$", "s")
			.replace("+", "plus")
			.replace(Regex("[^\\p{L}\\p{N}\\s]"), "") // Remove special characters
			.replace(Regex("\\s+"), " ") // Normalize spaces
	}
	
	/**
	 * Legacy method for backward compatibility
	 */
	private fun matchesKeyword(text: String, keyword: KeywordBlacklist): Boolean {
		return matchesKeywordEnhanced(text, keyword)
	}
	
	/**
	 * Handles blocked content by clearing input, blocking app, and showing notification
	 */
	private fun handleBlockedContent(
		keyword: KeywordBlacklist,
		blockedContent: String,
		packageName: String,
		node: AccessibilityNodeInfo
	) {
		Log.d(TAG, "Blocked content: '$blockedContent' in $packageName")
		
		// Clear the input field
		clearInputField(node)
		
		// Show toast notification
		showBlockedToast(keyword.keyword)
		
		// Log the blocked attempt
		logBlockedAttempt(keyword, packageName, blockedContent)
		
		// Send notification
		sendBlockingNotification(keyword.keyword, packageName)
		
		// 🚀 NEW: Block the app temporarily when inappropriate content is detected
		blockAppTemporarily(packageName, keyword)
	}
	
	/**
	 * Clears the input field by setting empty text
	 */
	private fun clearInputField(node: AccessibilityNodeInfo) {
		try {
			// Clear main node
			if (node.isEditable) {
				val arguments = android.os.Bundle().apply {
					putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
				}
				node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
				Log.d(TAG, "Cleared main node")
				return
			}
			
			// Search in child nodes for editable fields
			findAndClearEditableNodes(node)
		} catch (e: Exception) {
			Log.e(TAG, "Failed to clear input field", e)
		}
	}
	
	/**
	 * Search and clear all editable nodes
	 */
	private fun findAndClearEditableNodes(node: AccessibilityNodeInfo) {
		try {
			for (i in 0 until node.childCount) {
				val child = node.getChild(i) ?: continue
				
				if (child.isEditable && child.text != null) {
					val arguments = android.os.Bundle().apply {
						putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
					}
					child.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
					Log.d(TAG, "Cleared child editable node")
					child.recycle()
					return
				}
				
				// Check deeper children
				findAndClearEditableNodes(child)
				child.recycle()
			}
		} catch (e: Exception) {
			Log.e(TAG, "Error finding editable nodes", e)
		}
	}
	
	/**
	 * Clean up resources when service is destroyed
	 */
	override fun onDestroy() {
		super.onDestroy()
		
		// Release Custom AI Engine resources
		if (aiInitialized && ::customAI.isInitialized) {
			customAI.release()
		}
		
		Log.d(TAG, "KeywordAccessibilityService destroyed")
	}
	
	/**
	 * Shows toast notification for blocked content
	 */
	private fun showBlockedToast(keyword: String) {
		Toast.makeText(
			this,
			"Blocked inappropriate content: $keyword",
			Toast.LENGTH_SHORT
		).show()
	}
	
	/**
	 * 🚀 NEW: Blocks app temporarily when inappropriate content is detected
	 */
	private fun blockAppTemporarily(packageName: String, keyword: KeywordBlacklist) {
		serviceScope.launch {
			try {
				Log.i(TAG, "Blocking app $packageName due to inappropriate content: ${keyword.keyword}")
				
				// 1. Create or update app block rule in database
				val existingRule = try {
					appRepository.getRuleByPackage(packageName)
				} catch (e: Exception) {
					Log.e(TAG, "Error getting existing rule for $packageName", e)
					null
				}
				val appInfo = try {
					val pm = packageManager
					val app = pm.getApplicationInfo(packageName, 0)
					com.internetguard.pro.data.model.AppInfo(
						uid = app.uid,
						packageName = packageName,
						appName = pm.getApplicationLabel(app).toString(),
						icon = null,
						blockWifi = true,
						blockCellular = true,
						blockMode = "keyword_blocked"
					)
				} catch (e: Exception) {
					Log.e(TAG, "Error getting app info for $packageName", e)
					return@launch
				}
				
				val blockRule = if (existingRule != null) {
					// Update existing rule to block both WiFi and Cellular
					existingRule.copy(
						blockWifi = true,
						blockCellular = true,
						blockMode = "keyword_blocked"
					)
				} else {
					// Create new rule
					AppBlockRules(
						appUid = appInfo.uid,
						appPackageName = packageName,
						appName = appInfo.appName,
						blockWifi = true,
						blockCellular = true,
						blockMode = "keyword_blocked",
						createdAt = System.currentTimeMillis()
					)
				}
				
				// Save/update rule in database
				try {
					appRepository.saveRule(blockRule)
				} catch (e: Exception) {
					Log.e(TAG, "Error saving/updating rule for $packageName", e)
				}
				
				// 2. Send intent to VPN service to block the app
				val intent = android.content.Intent(this@KeywordAccessibilityService, com.internetguard.pro.services.NetworkGuardVpnService::class.java)
				intent.action = com.internetguard.pro.services.NetworkGuardVpnService.ACTION_BLOCK_APP
				intent.putExtra("package_name", packageName)
				intent.putExtra("block_reason", "inappropriate_content")
				intent.putExtra("keyword", keyword.keyword)
				intent.putExtra("block_duration", 10 * 60 * 1000L) // 10 minutes
				startForegroundService(intent)
				
				// 3. Schedule automatic unblocking after 10 minutes
				scheduleAppUnblocking(packageName, 10 * 60 * 1000L)
				
				// 4. Show enhanced notification
				showAppBlockedNotification(packageName, keyword.keyword)
				
				Log.i(TAG, "Successfully blocked app $packageName for 10 minutes due to inappropriate content")
				
			} catch (e: Exception) {
				Log.e(TAG, "Error blocking app $packageName", e)
			}
		}
	}
	
	/**
	 * 🚀 NEW: Schedules automatic unblocking of app after specified duration
	 */
	private fun scheduleAppUnblocking(packageName: String, durationMs: Long) {
		serviceScope.launch {
			try {
				kotlinx.coroutines.delay(durationMs)
				
				Log.i(TAG, "Auto-unblocking app $packageName after keyword block timeout")
				
				// Send intent to VPN service to unblock the app
				val intent = android.content.Intent(this@KeywordAccessibilityService, com.internetguard.pro.services.NetworkGuardVpnService::class.java)
				intent.action = com.internetguard.pro.services.NetworkGuardVpnService.ACTION_UNBLOCK_APP
				intent.putExtra("package_name", packageName)
				intent.putExtra("unblock_reason", "keyword_block_timeout")
				startForegroundService(intent)
				
				// Update database rule to unblock
				try {
					val existingRule = appRepository.getRuleByPackage(packageName)
					if (existingRule != null) {
						val updatedRule = existingRule.copy(
							blockWifi = false,
							blockCellular = false,
							blockMode = "manual"
						)
						appRepository.updateRule(updatedRule)
					}
				} catch (e: Exception) {
					Log.e(TAG, "Error updating rule for $packageName", e)
				}
				
				// Show unblock notification
				showAppUnblockedNotification(packageName)
				
			} catch (e: Exception) {
				Log.e(TAG, "Error auto-unblocking app $packageName", e)
			}
		}
	}
	
	/**
	 * 🚀 NEW: Shows notification when app is blocked due to inappropriate content
	 */
	private fun showAppBlockedNotification(packageName: String, keyword: String) {
		try {
			val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
			
			val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setSmallIcon(android.R.drawable.ic_dialog_alert)
				.setContentTitle("App Blocked - Inappropriate Content")
				.setContentText("$packageName blocked for 10 minutes due to: $keyword")
				.setStyle(NotificationCompat.BigTextStyle()
					.bigText("The app '$packageName' has been temporarily blocked for 10 minutes because inappropriate content was detected: '$keyword'. The app will be automatically unblocked after this period."))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.build()
			
			notificationManager.notify(NOTIFICATION_ID + 1, notification)
		} catch (e: Exception) {
			Log.e(TAG, "Error showing app blocked notification", e)
		}
	}
	
	/**
	 * 🚀 NEW: Shows notification when app is automatically unblocked
	 */
	private fun showAppUnblockedNotification(packageName: String) {
		try {
			val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
			
			val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setSmallIcon(android.R.drawable.ic_dialog_info)
				.setContentTitle("App Unblocked")
				.setContentText("$packageName has been automatically unblocked")
				.setStyle(NotificationCompat.BigTextStyle()
					.bigText("The app '$packageName' has been automatically unblocked after the keyword block timeout period."))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(true)
				.build()
			
			notificationManager.notify(NOTIFICATION_ID + 2, notification)
		} catch (e: Exception) {
			Log.e(TAG, "Error showing app unblocked notification", e)
		}
	}
	
	/**
	 * Logs blocked attempt to database
	 */
	private fun logBlockedAttempt(
		keyword: KeywordBlacklist,
		packageName: String,
		blockedContent: String
	) {
		serviceScope.launch {
			try {
				val log = KeywordLogs(
					keywordId = keyword.id,
					appPackageName = packageName,
					attemptTime = System.currentTimeMillis(),
					blockedContent = blockedContent
				)
				database.keywordLogsDao().insert(log)
			} catch (e: Exception) {
				Log.e(TAG, "Failed to log blocked attempt", e)
			}
		}
	}
	
	/**
	 * Sends notification about blocked content
	 */
	private fun sendBlockingNotification(keyword: String, packageName: String) {
		val notification = android.app.Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
			.setContentTitle("Content Blocked")
			.setContentText("Blocked '$keyword' in $packageName")
			.setSmallIcon(R.drawable.ic_keyword_block)
			.setAutoCancel(true)
			.build()
		
		val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.notify(NOTIFICATION_ID, notification)
	}
}

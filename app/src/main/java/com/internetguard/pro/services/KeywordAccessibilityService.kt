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
import com.internetguard.pro.ai.api.RemoteModerationClient
import com.internetguard.pro.utils.TextRedactor
import com.internetguard.pro.ai.UserAction
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.entities.KeywordLogs
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
	private val keywordCache = ConcurrentHashMap<Long, KeywordBlacklist>()
	// بهینه‌سازی: HashMap برای جستجوی سریع کلمات
	private val keywordHashMap = ConcurrentHashMap<String, KeywordBlacklist>()
	private val keywordHashMapLowercase = ConcurrentHashMap<String, KeywordBlacklist>()
	private var lastCacheUpdate = 0L
	private val cacheValidityDuration = 30000L // 30 seconds
	
    // AI engines
    private lateinit var customAI: CustomSmartDetector
    private var isAIEnabled = true // Opt-in for cloud moderation
    private var aiInitialized = false
    private var moderationClient: ModerationClient? = null
	
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

                // Initialize cloud moderation client if opted-in (using developer's proxy)
                val optIn = getCloudOptInFromPrefs()
                if (optIn) {
                    // Always use proxy server with developer's API key
                    val proxyUrl = "http://localhost:3000/api/moderate"
                    moderationClient = RemoteModerationClient(endpoint = proxyUrl, timeoutMs = 2500)
                    Log.i(TAG, "Cloud moderation client initialized with developer proxy")
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
						// پاک کردن cache های قدیمی
						keywordCache.clear()
						keywordHashMap.clear()
						keywordHashMapLowercase.clear()
						
						// بارگذاری به cache های مختلف برای بهینه‌سازی
						keywordList.forEach { keyword: KeywordBlacklist ->
							keywordCache[keyword.id] = keyword
							
							// HashMap برای جستجوی سریع
							keywordHashMap[keyword.keyword] = keyword
							keywordHashMapLowercase[keyword.keyword.lowercase()] = keyword
							
							// اضافه کردن کلمات تکی برای جستجوی بهتر
							keyword.keyword.split(" ").forEach { word ->
								if (word.length > 2) { // فقط کلمات بیش از 2 حرف
									keywordHashMap[word] = keyword
									keywordHashMapLowercase[word.lowercase()] = keyword
								}
							}
						}
						
						lastCacheUpdate = System.currentTimeMillis()
						// Log حذف شده برای بهینه‌سازی عملکرد در production
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
			// من event text
			event.text?.forEach { text ->
				if (!text.isNullOrBlank()) {
					append(text.toString())
					append(" ")
				}
			}
			
			// از node content
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
        // 1) Try cloud moderation (opt-in)
        moderationClient?.let { client ->
            try {
                val redacted = TextRedactor.redact(text)
                val langHint = detectLanguageHint(text)
                val res = client.moderate(redacted, langHint)
                // تصمیم‌گیری بر اساس action در پاسخ سرور؛ در صورت نبود، از confidence استفاده می‌کنیم
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

        // 2) Local custom AI
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

        // 3) Fallback to traditional matching
        return findBlockedKeywordTraditional(text, packageName)
    }

    private fun getCloudOptInFromPrefs(): Boolean {
        return try {
            val prefs = getSharedPreferences("cloud_moderation_prefs", Context.MODE_PRIVATE)
            // پیش‌فرض را روشن می‌کنیم تا قابلیت در دسترس باشد، مگر اینکه کاربر خاموش کرده باشد
            prefs.getBoolean("opt_in", true)
        } catch (_: Exception) { true }
    }



    private fun detectLanguageHint(text: String): String? {
        // تشخیص سبک: اگر حروف فارسی غالب باشد → fa، اگر عربی → ar، در غیر اینصورت null
        val arPersian = text.count { it in '\u0600'..'\u06FF' }
        val latin = text.count { it in 'A'..'Z' || it in 'a'..'z' }
        return when {
            arPersian > latin && arPersian > 2 -> "fa"
            latin > arPersian && latin > 2 -> "en"
            else -> null
        }
    }
	
	/**
	 * Traditional keyword matching (original method)
	 */
	private suspend fun findBlockedKeywordTraditional(text: String, packageName: String): KeywordBlacklist? {
		// Get app-specific keyword rules
		val appRules = database.appKeywordRulesDao().getActiveRulesForApp(packageName)
		
		// Check app-specific keywords first
		for (rule in appRules) {
			val keyword = keywordCache[rule.keywordId]
			if (keyword != null && matchesKeywordEnhanced(text, keyword)) {
				return keyword
			}
		}
		
		// If no app-specific rules, check global keywords
		if (appRules.isEmpty()) {
			for (keyword in keywordCache.values) {
				if (matchesKeywordEnhanced(text, keyword)) {
					return keyword
				}
			}
		}
		
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
		// بهینه‌سازی: جستجوی سریع با HashMap
		val words = text.split(Regex("\\s+"))
		
		// جستجوی مستقیم در HashMap
		for (word in words) {
			// جستجوی دقیق
			keywordHashMap[word]?.let { return it }
			
			// جستجوی case-insensitive
			keywordHashMapLowercase[word.lowercase()]?.let { keyword ->
				if (!keyword.caseSensitive) return keyword
			}
		}
		
		// جستجوی کامل متن برای عبارات چندکلمه‌ای
		keywordHashMap[text]?.let { return it }
		keywordHashMapLowercase[text.lowercase()]?.let { keyword ->
			if (!keyword.caseSensitive) return keyword
		}
		
		// Fallback: جستجوی سنتی فقط برای موارد پیچیده
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
	 * Handles blocked content by clearing input and showing notification
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
	}
	
	/**
	 * Clears the input field by setting empty text
	 */
	private fun clearInputField(node: AccessibilityNodeInfo) {
		try {
			// پاک کردن node اصلی
			if (node.isEditable) {
				val arguments = android.os.Bundle().apply {
					putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
				}
				node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
				Log.d(TAG, "Cleared main node")
				return
			}
			
			// جستجو در child nodes برای editable fields
			findAndClearEditableNodes(node)
		} catch (e: Exception) {
			Log.e(TAG, "Failed to clear input field", e)
		}
	}
	
	/**
	 * جستجو و پاک کردن همه editable nodes
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
				
				// بررسی child های عمیق‌تر
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

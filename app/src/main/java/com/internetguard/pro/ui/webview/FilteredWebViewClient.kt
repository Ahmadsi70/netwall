package com.internetguard.pro.ui.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.entities.KeywordBlacklist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Custom WebViewClient that filters HTML content for blacklisted keywords.
 * 
 * Intercepts web page content and blocks pages containing inappropriate
 * keywords before they are displayed to the user.
 */
class FilteredWebViewClient(
	private val onContentBlocked: (String, String) -> Unit
) : WebViewClient() {
	
	private val database by lazy { (InternetGuardProApp.getInstance()?.database) }
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val keywordCache = ConcurrentHashMap<Long, KeywordBlacklist>()
	private var lastCacheUpdate = 0L
	private val cacheValidityDuration = 30000L // 30 seconds
	
	companion object {
		private const val BLOCKED_HTML = """
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<title>Content Blocked</title>
				<style>
					body { 
						font-family: Arial, sans-serif; 
						text-align: center; 
						padding: 50px; 
						background-color: #f5f5f5;
					}
					.blocked-message {
						background-color: #ffebee;
						border: 2px solid #f44336;
						border-radius: 8px;
						padding: 20px;
						margin: 20px;
					}
					.blocked-title {
						color: #d32f2f;
						font-size: 24px;
						margin-bottom: 10px;
					}
					.blocked-text {
						color: #666;
						font-size: 16px;
					}
				</style>
			</head>
			<body>
				<div class="blocked-message">
					<div class="blocked-title">🚫 Content Blocked</div>
					<div class="blocked-text">This page contains inappropriate content and has been blocked by InternetGuard Pro.</div>
				</div>
			</body>
			</html>
		"""
	}
	
	override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
		request ?: return null
		val url = request.url.toString()
		
		// Check if URL contains blocked keywords
		if (containsBlockedKeywords(url)) {
			onContentBlocked("URL", url)
			return createBlockedResponse()
		}
		
		return super.shouldInterceptRequest(view, request)
	}
	
	override fun onPageFinished(view: WebView?, url: String?) {
		super.onPageFinished(view, url)
		url ?: return
		
		// Check page content for blocked keywords
		view?.evaluateJavascript("document.documentElement.outerHTML") { html ->
			if (containsBlockedKeywords(html)) {
				onContentBlocked("Page Content", url)
				view.loadDataWithBaseURL(null, BLOCKED_HTML, "text/html", "UTF-8", null)
			}
		}
	}
	
	/**
	 * Checks if text contains any blocked keywords
	 */
	private fun containsBlockedKeywords(text: String): Boolean {
		// Update cache if needed
		if (System.currentTimeMillis() - lastCacheUpdate > cacheValidityDuration) {
			loadKeywordsToCache()
		}
		
		// Check against cached keywords
		for (keyword in keywordCache.values) {
			if (matchesKeyword(text, keyword)) {
				return true
			}
		}
		return false
	}
	
	/**
	 * Checks if text matches a keyword based on case sensitivity
	 */
	private fun matchesKeyword(text: String, keyword: KeywordBlacklist): Boolean {
		return if (keyword.caseSensitive) {
			text.contains(keyword.keyword)
		} else {
			text.contains(keyword.keyword, ignoreCase = true)
		}
	}
	
	/**
	 * Loads keywords from database into cache
	 */
	private fun loadKeywordsToCache() {
		serviceScope.launch {
			try {
				database?.let { db ->
					val keywords = db.keywordBlacklistDao().observeAll()
					keywords.collect { keywordList ->
						keywordCache.clear()
						keywordList.forEach { keyword ->
							keywordCache[keyword.id] = keyword
						}
						lastCacheUpdate = System.currentTimeMillis()
					}
				}
			} catch (e: Exception) {
				// Handle error silently
			}
		}
	}
	
	/**
	 * Creates a blocked response
	 */
	private fun createBlockedResponse(): WebResourceResponse {
		val inputStream = ByteArrayInputStream(BLOCKED_HTML.toByteArray())
		return WebResourceResponse("text/html", "UTF-8", inputStream)
	}
}

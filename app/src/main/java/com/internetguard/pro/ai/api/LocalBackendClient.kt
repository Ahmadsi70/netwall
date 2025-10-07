package com.internetguard.pro.ai.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Client for communicating with local backend server
 * Supports subscription system and rate limiting
 */
class LocalBackendClient(
    private val endpoint: String,
    private val timeoutMs: Long = 5000
) : ModerationClient, SuggestionClient {
    
    companion object {
        private const val TAG = "LocalBackendClient"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .build()
    
    private val json = "application/json; charset=utf-8".toMediaType()

    override suspend fun moderate(text: String, languageHint: String?): ModerationResult {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("input", text)
                    if (!languageHint.isNullOrBlank()) put("language_hint", languageHint)
                }

                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer internetguard-pro-default")
                    .post(payload.toString().toRequestBody(json))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Moderation request failed with code: ${response.code}")
                        return@withContext ModerationResult(false, 0f)
                    }
                    
                    val body = response.body?.string().orEmpty()
                    parseModerationResponse(body)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Moderation error: ${e.message}")
                ModerationResult(false, 0f)
            }
        }
    }

    override suspend fun suggest(keyword: String, language: String?, category: String?): SuggestResult {
        return withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("keyword", keyword)
                    if (!language.isNullOrBlank()) put("language", language)
                    if (!category.isNullOrBlank()) put("category", category)
                }

                val url = endpoint.replace("/moderate", "/suggest")
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer internetguard-pro-default")
                    .post(payload.toString().toRequestBody(json))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Suggestion request failed with code: ${response.code}")
                        return@withContext SuggestResult()
                    }
                    
                    val body = response.body?.string().orEmpty()
                    parseSuggestResponse(body)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Suggestion error: ${e.message}")
                SuggestResult()
            }
        }
    }

    /**
     * Parse moderation response from local backend
     */
    private fun parseModerationResponse(body: String): ModerationResult {
        return try {
            val obj = JSONObject(body)
            val inappropriate = obj.optBoolean("inappropriate", false)
            val confidence = obj.optDouble("confidence", 0.0).toFloat()
            val category = obj.optString("category", null)
            val language = obj.optString("language", null)
            
            // Check for subscription info
            val subscription = obj.optJSONObject("subscription")
            if (subscription != null) {
                val upgradeRequired = subscription.optBoolean("upgradeRequired", false)
                val remaining = subscription.optInt("remaining", 0)
                
                if (upgradeRequired) {
                    Log.w(TAG, "Subscription upgrade required. Remaining requests: $remaining")
                }
            }
            
            ModerationResult(inappropriate, confidence, category, null, language, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse moderation response", e)
            ModerationResult(false, 0f)
        }
    }

    /**
     * Parse suggestion response from local backend
     */
    private fun parseSuggestResponse(body: String): SuggestResult {
        return try {
            val obj = JSONObject(body)
            val synonyms = mutableListOf<String>()
            val variants = mutableListOf<String>()
            val obfuscations = mutableListOf<String>()
            val regex = mutableListOf<String>()
            val categories = mutableListOf<String>()
            
            // Parse arrays
            obj.optJSONArray("synonyms")?.let { array ->
                for (i in 0 until array.length()) {
                    synonyms.add(array.getString(i))
                }
            }
            
            obj.optJSONArray("variants")?.let { array ->
                for (i in 0 until array.length()) {
                    variants.add(array.getString(i))
                }
            }
            
            obj.optJSONArray("obfuscations")?.let { array ->
                for (i in 0 until array.length()) {
                    obfuscations.add(array.getString(i))
                }
            }
            
            obj.optJSONArray("regex")?.let { array ->
                for (i in 0 until array.length()) {
                    regex.add(array.getString(i))
                }
            }
            
            obj.optJSONArray("categories")?.let { array ->
                for (i in 0 until array.length()) {
                    categories.add(array.getString(i))
                }
            }
            
            val notes = obj.optString("notes", "")
            
            SuggestResult(synonyms, variants, obfuscations, regex, categories, notes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse suggestion response", e)
            SuggestResult()
        }
    }
}

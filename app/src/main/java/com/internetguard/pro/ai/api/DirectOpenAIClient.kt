package com.internetguard.pro.ai.api

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Direct OpenAI API client without proxy
 * 
 * Security:
 * - API key is never logged
 * - Stored only in memory during object lifetime
 * - Uses HTTPS for all communications
 * - No key material in error messages
 */
class DirectOpenAIClient(
    private val apiKey: String,
    timeoutMs: Long = 10000L
) : ModerationClient, SuggestionClient {
    
    companion object {
        private const val TAG = "DirectOpenAIClient"
    }
    
    init {
        // Validate API key format on initialization
        require(apiKey.startsWith("sk-") && apiKey.length >= 20) {
            "Invalid OpenAI API key format"
        }
        Log.i(TAG, "DirectOpenAIClient initialized (key length: ${apiKey.length})")
    }

    private val client = OkHttpClient.Builder()
        .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .build()

    private val json = "application/json; charset=utf-8".toMediaType()

    // System prompt for AI suggestions
    private val systemPrompt = """
        You are a strict content moderator for a parental-control app. 
        When given a keyword, generate related terms that should also be blocked to protect minors.
        
        Return ONLY a valid JSON object with these fields:
        {
          "synonyms": ["word1", "word2"],
          "variants": ["variant1", "variant2"], 
          "obfuscations": ["w0rd", "w*rd"],
          "regex": ["pattern1", "pattern2"],
          "categories": ["category1"],
          "notes": "brief explanation"
        }
        
        Focus on harmful content categories: violence, sexual content, drugs, hate speech, self-harm, gambling, scams.
    """.trimIndent()

    override suspend fun moderate(text: String, languageHint: String?): ModerationResult {
        return try {
            val payload = JSONObject().apply {
                put("input", text)
                put("model", "text-moderation-latest")
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/moderations")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(json))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "Moderation request failed with code: ${resp.code}")
                    return ModerationResult(false, 0f)
                }
                val body = resp.body?.string().orEmpty()
                parseModerationResponse(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Moderation error: ${e.message}")
            ModerationResult(false, 0f)
        }
    }

    override suspend fun suggest(keyword: String, languageHint: String?, categoryHint: String?): SuggestResult {
        return try {
            val userMessage = buildString {
                append("Keyword: \"$keyword\"")
                if (!languageHint.isNullOrBlank()) append("\nLanguage: $languageHint")
                if (!categoryHint.isNullOrBlank()) append("\nCategory: $categoryHint")
            }

            val payload = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })
                })
                put("max_tokens", 1000)
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(json))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "Suggestion request failed with code: ${resp.code}")
                    return SuggestResult()
                }
                val body = resp.body?.string().orEmpty()
                parseSuggestionResponse(body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Suggestion error: ${e.message}")
            SuggestResult()
        }
    }

    private fun parseModerationResponse(body: String): ModerationResult {
        return try {
            val obj = JSONObject(body)
            val results = obj.getJSONArray("results")
            val result = results.getJSONObject(0)
            
            val flagged = result.getBoolean("flagged")
            val categories = result.getJSONObject("categories")
            val scores = result.getJSONObject("category_scores")
            
            // Find highest scoring category
            var maxScore = 0.0
            var maxCategory = ""
            val categoryNames = categories.keys()
            while (categoryNames.hasNext()) {
                val category = categoryNames.next()
                val score = scores.getDouble(category)
                if (score > maxScore) {
                    maxScore = score
                    maxCategory = category
                }
            }
            
            ModerationResult(
                isInappropriate = flagged,
                confidence = maxScore.toFloat(),
                category = if (flagged) maxCategory else null,
                rationale = if (flagged) "Flagged by OpenAI moderation" else null,
                language = null,
                action = if (flagged) "block" else "allow"
            )
        } catch (_: Exception) {
            ModerationResult(false, 0f)
        }
    }

    private fun parseSuggestionResponse(body: String): SuggestResult {
        return try {
            val obj = JSONObject(body)
            val choices = obj.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            val content = message.getString("content")
            
            // Parse JSON response from OpenAI
            val suggestions = JSONObject(content)
            
            fun parseArray(name: String): List<String> = buildList {
                val array = suggestions.optJSONArray(name) ?: return@buildList
                for (i in 0 until array.length()) {
                    val item = array.optString(i, null)
                    if (!item.isNullOrBlank()) add(item)
                }
            }
            
            SuggestResult(
                synonyms = parseArray("synonyms"),
                variants = parseArray("variants"),
                obfuscations = parseArray("obfuscations"),
                regex = parseArray("regex"),
                categories = parseArray("categories"),
                notes = suggestions.optString("notes", "")
            )
        } catch (_: Exception) {
            SuggestResult()
        }
    }
}

package com.internetguard.pro.ai.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RemoteModerationClient(
    private val endpoint: String,
    timeoutMs: Long = 2500L
) : ModerationClient, SuggestionClient {

    private val client = OkHttpClient.Builder()
        .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
        .build()

    private val json = "application/json; charset=utf-8".toMediaType()

    override suspend fun moderate(text: String, languageHint: String?): ModerationResult {
        return try {
            val payload = JSONObject().apply {
                put("input", text)
                if (!languageHint.isNullOrBlank()) put("language_hint", languageHint)
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(json))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return ModerationResult(false, 0f)
                val body = resp.body?.string().orEmpty()
                parseResponse(body)
            }
        } catch (_: Exception) {
            ModerationResult(false, 0f)
        }
    }

    /**
     * Parses JSON response from moderation proxy into ModerationResult.
     * Exposed as internal for unit testing.
     */
    internal fun parseResponse(body: String): ModerationResult {
        return try {
            val obj = JSONObject(body)
            val inappropriate = obj.optBoolean("inappropriate", false)
            val confidence = obj.optDouble("confidence", 0.0).toFloat()
            val category = obj.optString("category", null)
            val language = obj.optString("language", null)
            val rationale = obj.optString("rationale", null)
            val action = obj.optString("action", null)
            ModerationResult(inappropriate, confidence, category, rationale, language, action)
        } catch (_: Exception) {
            ModerationResult(false, 0f)
        }
    }

    override suspend fun suggest(keyword: String, language: String?, category: String?): SuggestResult {
        return try {
            val payload = JSONObject().apply {
                put("keyword", keyword)
                if (!language.isNullOrBlank()) put("language", language)
                if (!category.isNullOrBlank()) put("category", category)
            }

            val url = endpoint.replace("/moderate", "/suggest")
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(json))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return SuggestResult()
                val body = resp.body?.string().orEmpty()
                parseSuggest(body)
            }
        } catch (_: Exception) {
            SuggestResult()
        }
    }

    internal fun parseSuggest(body: String): SuggestResult {
        return try {
            val obj = JSONObject(body)
            fun arr(name: String): List<String> = buildList {
                val a = obj.optJSONArray(name) ?: return@buildList
                for (i in 0 until a.length()) {
                    val v = a.optString(i, null)
                    if (!v.isNullOrBlank()) add(v)
                }
            }
            SuggestResult(
                synonyms = arr("synonyms"),
                variants = arr("variants"),
                obfuscations = arr("obfuscations"),
                regex = arr("regex"),
                categories = arr("categories"),
                notes = obj.optString("notes", "")
            )
        } catch (_: Exception) { SuggestResult() }
    }
}



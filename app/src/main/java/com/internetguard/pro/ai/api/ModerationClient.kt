package com.internetguard.pro.ai.api

data class ModerationResult(
    val isInappropriate: Boolean,
    val confidence: Float,
    val category: String? = null,
    val rationale: String? = null,
    val language: String? = null,
    val action: String? = null // allow | review | block
)

interface ModerationClient {
    suspend fun moderate(text: String, languageHint: String? = null): ModerationResult
}

data class SuggestResult(
    val synonyms: List<String> = emptyList(),
    val variants: List<String> = emptyList(),
    val obfuscations: List<String> = emptyList(),
    val regex: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val notes: String = ""
)

interface SuggestionClient {
    suspend fun suggest(keyword: String, language: String? = null, category: String? = null): SuggestResult
}



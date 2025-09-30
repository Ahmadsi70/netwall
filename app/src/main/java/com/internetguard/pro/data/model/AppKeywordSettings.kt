package com.internetguard.pro.data.model

/**
 * Data model representing an app with its keyword filtering settings.
 * 
 * This model combines app information with keyword filtering status
 * for display in the UI.
 */
data class AppKeywordSettings(
    val appInfo: AppInfo,
    val hasKeywordFiltering: Boolean,
    val activeKeywordsCount: Int,
    val previewKeywords: List<String> = emptyList()
) {
    val displayStatus: String
        get() = when {
            !hasKeywordFiltering -> "No filtering"
            activeKeywordsCount == 0 -> "Enabled (no keywords)"
            activeKeywordsCount == 1 -> "1 keyword active"
            else -> "$activeKeywordsCount keywords active"
        }

    val previewText: String
        get() = if (previewKeywords.isEmpty()) "" else run {
            val shown = previewKeywords.take(3)
            val extra = activeKeywordsCount - shown.size
            if (extra > 0) shown.joinToString(", ") + " (+$extra)" else shown.joinToString(", ")
        }
}

package com.internetguard.pro.ai

import android.util.Log
import java.util.*

/**
 * Multi-Language Content Processor
 * 
 * Features:
 * - Language detection (Persian, English, Arabic, Mixed)
 * - Language-specific keyword matching
 * - Cultural context awareness
 * - Script-based detection
 * - Mixed language handling
 */
class MultiLanguageProcessor {
    
    companion object {
        private const val TAG = "MultiLanguageProcessor"
    }
    
    // Language-specific inappropriate content patterns
    private val languagePatterns = mapOf(
        Language.PERSIAN to PersianPatterns(),
        Language.ENGLISH to EnglishPatterns(), 
        Language.ARABIC to ArabicPatterns()
    )
    
    /**
     * Process text with language-aware detection
     */
    fun processText(text: String): DetectionResult {
        val detectedLanguage = detectLanguage(text)
        val cleanText = preprocessText(text, detectedLanguage)
        
        return when (detectedLanguage) {
            Language.PERSIAN -> processPersianText(cleanText)
            Language.ENGLISH -> processEnglishText(cleanText)
            Language.ARABIC -> processArabicText(cleanText)
            Language.MIXED -> processMixedLanguageText(cleanText)
            Language.UNKNOWN -> processUnknownLanguageText(cleanText)
        }
    }
    
    /**
     * Detect primary language of text
     */
    private fun detectLanguage(text: String): Language {
        val persianChars = text.count { it in '\u0600'..'\u06FF' || it in '\uFB50'..'\uFDFF' }
        val arabicChars = text.count { it in '\u0600'..'\u06FF' }
        val englishChars = text.count { it in 'a'..'z' || it in 'A'..'Z' }
        val totalChars = text.count { it.isLetter() }
        
        if (totalChars == 0) return Language.UNKNOWN
        
        val persianRatio = persianChars.toDouble() / totalChars
        val englishRatio = englishChars.toDouble() / totalChars
        val arabicRatio = arabicChars.toDouble() / totalChars
        
        return when {
            persianRatio > 0.6 -> Language.PERSIAN
            englishRatio > 0.6 -> Language.ENGLISH
            arabicRatio > 0.6 -> Language.ARABIC
            persianRatio + englishRatio + arabicRatio > 0.3 -> Language.MIXED
            else -> Language.UNKNOWN
        }
    }
    
    /**
     * Preprocess text based on language
     */
    private fun preprocessText(text: String, language: Language): String {
        var processed = text.lowercase().trim()
        
        when (language) {
            Language.PERSIAN -> {
                // Normalize Persian characters
                processed = normalizePersianText(processed)
            }
            Language.ARABIC -> {
                // Normalize Arabic characters
                processed = normalizeArabicText(processed)
            }
            Language.ENGLISH -> {
                // Standard English preprocessing
                processed = normalizeEnglishText(processed)
            }
            else -> {
                // General normalization
                processed = normalizeGeneralText(processed)
            }
        }
        
        return processed
    }
    
    /**
     * Process Persian text
     */
    private fun processPersianText(text: String): DetectionResult {
        val patterns = languagePatterns[Language.PERSIAN]!!
        val matches = patterns.findMatches(text)
        
        if (matches.isNotEmpty()) {
            val confidence = calculatePersianConfidence(matches, text)
            return DetectionResult(
                isInappropriate = confidence > 0.7f,
                confidence = confidence,
                category = "adult_content_persian",
                reasoning = "Persian inappropriate content detected: ${matches.joinToString(", ")}",
                triggeredPatterns = matches,
                language = "persian"
            )
        }
        
        // Check for disguised Persian content
        val disguisedMatches = patterns.findDisguisedMatches(text)
        if (disguisedMatches.isNotEmpty()) {
            val confidence = calculatePersianConfidence(disguisedMatches, text) * 0.8f
            return DetectionResult(
                isInappropriate = confidence > 0.6f,
                confidence = confidence,
                category = "disguised_persian",
                reasoning = "Disguised Persian content: ${disguisedMatches.joinToString(", ")}",
                triggeredPatterns = disguisedMatches,
                language = "persian"
            )
        }
        
        return DetectionResult.safe("Persian content appears appropriate")
    }
    
    /**
     * Process English text
     */
    private fun processEnglishText(text: String): DetectionResult {
        val patterns = languagePatterns[Language.ENGLISH]!!
        val matches = patterns.findMatches(text)
        
        if (matches.isNotEmpty()) {
            val confidence = calculateEnglishConfidence(matches, text)
            return DetectionResult(
                isInappropriate = confidence > 0.7f,
                confidence = confidence,
                category = "adult_content_english",
                reasoning = "English inappropriate content: ${matches.joinToString(", ")}",
                triggeredPatterns = matches,
                language = "english"
            )
        }
        
        // Check for l33t speak and obfuscation
        val obfuscatedMatches = patterns.findObfuscatedMatches(text)
        if (obfuscatedMatches.isNotEmpty()) {
            val confidence = calculateEnglishConfidence(obfuscatedMatches, text) * 0.9f
            return DetectionResult(
                isInappropriate = confidence > 0.6f,
                confidence = confidence,
                category = "obfuscated_english",
                reasoning = "Obfuscated English content: ${obfuscatedMatches.joinToString(", ")}",
                triggeredPatterns = obfuscatedMatches,
                language = "english"
            )
        }
        
        return DetectionResult.safe("English content appears appropriate")
    }
    
    /**
     * Process Arabic text
     */
    private fun processArabicText(text: String): DetectionResult {
        val patterns = languagePatterns[Language.ARABIC]!!
        val matches = patterns.findMatches(text)
        
        if (matches.isNotEmpty()) {
            val confidence = calculateArabicConfidence(matches, text)
            return DetectionResult(
                isInappropriate = confidence > 0.7f,
                confidence = confidence,
                category = "adult_content_arabic",
                reasoning = "Arabic inappropriate content: ${matches.joinToString(", ")}",
                triggeredPatterns = matches,
                language = "arabic"
            )
        }
        
        return DetectionResult.safe("Arabic content appears appropriate")
    }
    
    /**
     * Process mixed language text
     */
    private fun processMixedLanguageText(text: String): DetectionResult {
        val allMatches = mutableListOf<String>()
        var maxConfidence = 0f
        var dominantLanguage = "mixed"
        
        // Check each language
        for ((language, patterns) in languagePatterns) {
            val matches = patterns.findMatches(text)
            if (matches.isNotEmpty()) {
                allMatches.addAll(matches)
                val confidence = when (language) {
                    Language.PERSIAN -> calculatePersianConfidence(matches, text)
                    Language.ENGLISH -> calculateEnglishConfidence(matches, text)
                    Language.ARABIC -> calculateArabicConfidence(matches, text)
                    else -> 0.5f
                }
                if (confidence > maxConfidence) {
                    maxConfidence = confidence
                    dominantLanguage = language.toString().lowercase()
                }
            }
        }
        
        if (allMatches.isNotEmpty()) {
            return DetectionResult(
                isInappropriate = maxConfidence > 0.6f,
                confidence = maxConfidence,
                category = "mixed_language_inappropriate",
                reasoning = "Mixed language inappropriate content: ${allMatches.joinToString(", ")}",
                triggeredPatterns = allMatches,
                language = dominantLanguage
            )
        }
        
        return DetectionResult.safe("Mixed language content appears appropriate")
    }
    
    /**
     * Process unknown language text
     */
    private fun processUnknownLanguageText(text: String): DetectionResult {
        // Use general pattern matching for unknown languages
        val suspiciousPatterns = listOf(
            Regex("\\d+\\s*\\+"), // Age restrictions
            Regex("xxx+", RegexOption.IGNORE_CASE), // XXX
            Regex("nsfw", RegexOption.IGNORE_CASE) // NSFW
        )
        
        val matches = suspiciousPatterns.mapNotNull { pattern ->
            pattern.find(text)?.value
        }
        
        if (matches.isNotEmpty()) {
            return DetectionResult(
                isInappropriate = true,
                confidence = 0.6f,
                category = "unknown_language_suspicious",
                reasoning = "Suspicious patterns in unknown language: ${matches.joinToString(", ")}",
                triggeredPatterns = matches,
                language = "unknown"
            )
        }
        
        return DetectionResult.safe("Unknown language content appears safe")
    }
    
    /**
     * Calculate confidence for Persian matches
     */
    private fun calculatePersianConfidence(matches: List<String>, text: String): Float {
        var confidence = 0f
        
        // High-confidence Persian keywords
        val highConfidenceKeywords = setOf(
            "محتوای بزرگسالان", "مثبت 18", "محتوای جنسی", "محتوای نامناسب"
        )
        
        for (match in matches) {
            confidence += if (match in highConfidenceKeywords) 0.9f else 0.7f
        }
        
        // Adjust based on context length
        val contextMultiplier = when {
            text.length < 20 -> 1.2f // Short text, higher confidence
            text.length > 100 -> 0.8f // Long text, might be educational
            else -> 1.0f
        }
        
        return (confidence * contextMultiplier).coerceAtMost(1.0f)
    }
    
    /**
     * Calculate confidence for English matches
     */
    private fun calculateEnglishConfidence(matches: List<String>, text: String): Float {
        var confidence = 0f
        
        val highConfidenceKeywords = setOf("porn", "xxx", "nsfw", "explicit")
        val mediumConfidenceKeywords = setOf("adult", "mature", "sexual")
        
        for (match in matches) {
            confidence += when {
                match in highConfidenceKeywords -> 0.9f
                match in mediumConfidenceKeywords -> 0.7f
                else -> 0.5f
            }
        }
        
        return confidence.coerceAtMost(1.0f)
    }
    
    /**
     * Calculate confidence for Arabic matches
     */
    private fun calculateArabicConfidence(matches: List<String>, text: String): Float {
        var confidence = 0f
        
        for (match in matches) {
            confidence += 0.8f // Arabic patterns are generally high confidence
        }
        
        return confidence.coerceAtMost(1.0f)
    }
    
    /**
     * Normalize Persian text
     */
    private fun normalizePersianText(text: String): String {
        return text
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('٠', '۰')
            .replace('١', '۱')
            .replace('٢', '۲')
            .replace('٣', '۳')
            .replace('٤', '۴')
            .replace('٥', '۵')
            .replace('٦', '۶')
            .replace('٧', '۷')
            .replace('٨', '۸')
            .replace('٩', '۹')
    }
    
    /**
     * Normalize Arabic text
     */
    private fun normalizeArabicText(text: String): String {
        return text
            .replace('ي', 'ی')
            .replace('ك', 'ک')
    }
    
    /**
     * Normalize English text
     */
    private fun normalizeEnglishText(text: String): String {
        return text.replace(Regex("\\s+"), " ")
    }
    
    /**
     * General text normalization
     */
    private fun normalizeGeneralText(text: String): String {
        return text
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .trim()
    }
}

/**
 * Language enumeration
 */
enum class Language {
    PERSIAN, ENGLISH, ARABIC, MIXED, UNKNOWN
}

/**
 * Base class for language-specific patterns
 */
abstract class LanguagePatterns {
    abstract fun findMatches(text: String): List<String>
    open fun findDisguisedMatches(text: String): List<String> = emptyList()
    open fun findObfuscatedMatches(text: String): List<String> = emptyList()
}

/**
 * Persian language patterns
 */
class PersianPatterns : LanguagePatterns() {
    private val keywords = setOf(
        "بزرگسال", "محتوای بزرگسالان", "مثبت 18", "18+", "محتوای جنسی",
        "محتوای نامناسب", "محتوای بالغین", "فیلم بزرگسال", "ویدیو بزرگسال",
        "عکس بزرگسال", "تصاویر بزرگسال", "محتوای ممنوع"
    )
    
    override fun findMatches(text: String): List<String> {
        return keywords.filter { text.contains(it, ignoreCase = true) }
    }
    
    override fun findDisguisedMatches(text: String): List<String> {
        val disguisedPatterns = mapOf(
            "ب ز ر گ س ا ل" to "بزرگسال",
            "م ث ب ت ۱ ۸" to "مثبت 18",
            "محتوای_بزرگسالان" to "محتوای بزرگسالان"
        )
        
        return disguisedPatterns.entries.mapNotNull { (pattern, original) ->
            if (text.contains(pattern, ignoreCase = true)) original else null
        }
    }
}

/**
 * English language patterns
 */
class EnglishPatterns : LanguagePatterns() {
    private val keywords = setOf(
        "adult", "porn", "xxx", "nsfw", "explicit", "mature", "sexual", "erotic",
        "nude", "naked", "sex", "adult content", "mature content", "sexual content",
        "adult videos", "porn videos", "xxx videos"
    )
    
    override fun findMatches(text: String): List<String> {
        return keywords.filter { text.contains(it, ignoreCase = true) }
    }
    
    override fun findObfuscatedMatches(text: String): List<String> {
        val obfuscatedPatterns = mapOf(
            "p0rn" to "porn", "s3x" to "sex", "pr0n" to "porn",
            "p o r n" to "porn", "s e x" to "sex", "a d u l t" to "adult",
            "por***n" to "porn", "s*x" to "sex"
        )
        
        return obfuscatedPatterns.entries.mapNotNull { (pattern, original) ->
            if (text.contains(pattern, ignoreCase = true)) original else null
        }
    }
}

/**
 * Arabic language patterns
 */
class ArabicPatterns : LanguagePatterns() {
    private val keywords = setOf(
        "بالغين", "محتوى للبالغين", "محتوى جنسي", "محتوى ناضج",
        "فيديو للبالغين", "صور للبالغين", "محتوى ممنوع"
    )
    
    override fun findMatches(text: String): List<String> {
        return keywords.filter { text.contains(it, ignoreCase = true) }
    }
}

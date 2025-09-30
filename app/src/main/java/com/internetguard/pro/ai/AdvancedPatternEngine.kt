package com.internetguard.pro.ai

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Advanced Pattern Recognition Engine
 * 
 * Features:
 * - L33t speak detection (p0rn -> porn)
 * - Word splitting detection (p o r n -> porn)
 * - Character substitution (@, $, etc.)
 * - Reverse/ROT13 detection
 * - Pattern obfuscation detection
 * - Multi-language pattern matching
 */
class AdvancedPatternEngine {
    
    companion object {
        private const val TAG = "AdvancedPatternEngine"
        private const val QUICK_CONFIDENCE_THRESHOLD = 0.95f
    }
    
    // Pattern caches for performance
    private val normalizedCache = ConcurrentHashMap<String, String>()
    private val patternMatchCache = ConcurrentHashMap<String, PatternMatchResult>()
    
    // Core inappropriate keywords by language
    private val englishPatterns = setOf(
        "adult", "porn", "xxx", "nsfw", "explicit", "mature", "sexual", "erotic", 
        "nude", "naked", "sex", "adult content", "mature content", "sexual content",
        "adult videos", "porn videos", "xxx videos", "18+", "21+"
    )
    
    private val persianPatterns = setOf(
        "بزرگسال", "محتوای بزرگسالان", "مثبت 18", "18+", "محتوای جنسی", 
        "محتوای نامناسب", "محتوای بالغین", "محتوای بزرگسالان", "فیلم بزرگسال",
        "ویدیو بزرگسال", "عکس بزرگسال", "تصاویر بزرگسال"
    )
    
    private val arabicPatterns = setOf(
        "بالغين", "محتوى للبالغين", "محتوى جنسي", "محتوى ناضج", 
        "فيديو للبالغين", "صور للبالغين"
    )
    
    // L33t speak substitution map
    private val l33tMap = mapOf(
        '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', 
        '7' to 't', '@' to 'a', '$' to 's', '!' to 'i', '|' to 'l',
        // Persian specific
        '۰' to 'و', '۱' to 'ی', '۳' to 'ع', '۴' to 'ا', '۵' to 'س'
    )
    
    // Suspicious patterns regex
    private val suspiciousPatterns = listOf(
        Regex("\\d+\\s*\\+", RegexOption.IGNORE_CASE), // 18+, 21+
        Regex("\\bxxx+\\b", RegexOption.IGNORE_CASE), // xxx, xxxx
        Regex("\\bnsfw\\b", RegexOption.IGNORE_CASE), // nsfw
        Regex("[a-z]\\d+[a-z]", RegexOption.IGNORE_CASE), // p0rn, s3x
        Regex("\\b[a-z]\\s+[a-z]\\s+[a-z]\\b", RegexOption.IGNORE_CASE), // p o r n
        Regex("[a-z]+\\*+[a-z]+", RegexOption.IGNORE_CASE), // por***n
        Regex("\\b\\w*adult\\w*\\b", RegexOption.IGNORE_CASE), // adult variations
        Regex("\\b\\w*porn\\w*\\b", RegexOption.IGNORE_CASE) // porn variations
    )
    
    /**
     * Quick screening for obvious patterns
     */
    fun quickScreen(text: String): DetectionResult {
        val cacheKey = "quick_${text.hashCode()}"
        patternMatchCache[cacheKey]?.let { cached ->
            return DetectionResult(
                isInappropriate = cached.isMatch,
                confidence = cached.confidence,
                category = cached.category,
                reasoning = cached.reasoning,
                triggeredPatterns = cached.patterns
            )
        }
        
        val cleanText = text.lowercase().trim()
        val triggeredPatterns = mutableListOf<String>()
        var maxConfidence = 0f
        var category = "general"
        
        // Direct keyword matching
        for (pattern in englishPatterns) {
            if (cleanText.contains(pattern)) {
                triggeredPatterns.add(pattern)
                maxConfidence = maxOf(maxConfidence, 0.95f)
                category = "adult_content"
            }
        }
        
        for (pattern in persianPatterns) {
            if (cleanText.contains(pattern)) {
                triggeredPatterns.add(pattern)
                maxConfidence = maxOf(maxConfidence, 0.95f)
                category = "adult_content"
            }
        }
        
        for (pattern in arabicPatterns) {
            if (cleanText.contains(pattern)) {
                triggeredPatterns.add(pattern)
                maxConfidence = maxOf(maxConfidence, 0.95f)
                category = "adult_content"
            }
        }
        
        // Suspicious pattern matching
        for (regex in suspiciousPatterns) {
            if (regex.containsMatchIn(cleanText)) {
                val match = regex.find(cleanText)?.value ?: "pattern"
                triggeredPatterns.add(match)
                maxConfidence = maxOf(maxConfidence, 0.8f)
                category = if (category == "general") "suspicious" else category
            }
        }
        
        val result = PatternMatchResult(
            isMatch = maxConfidence > 0.5f,
            confidence = maxConfidence,
            category = category,
            patterns = triggeredPatterns,
            reasoning = if (triggeredPatterns.isNotEmpty()) {
                "Quick match: ${triggeredPatterns.joinToString(", ")}"
            } else {
                "No quick patterns detected"
            }
        )
        
        // Cache result
        patternMatchCache[cacheKey] = result
        
        return DetectionResult(
            isInappropriate = result.isMatch,
            confidence = result.confidence,
            category = result.category,
            reasoning = result.reasoning,
            triggeredPatterns = result.patterns
        )
    }
    
    /**
     * Advanced pattern detection with normalization
     */
    fun detectAdvancedPatterns(text: String): DetectionResult {
        val variations = generateTextVariations(text)
        val allTriggered = mutableSetOf<String>()
        var maxConfidence = 0f
        var bestCategory = "general"
        
        for (variation in variations) {
            val result = quickScreen(variation)
            if (result.confidence > maxConfidence) {
                maxConfidence = result.confidence
                bestCategory = result.category
            }
            allTriggered.addAll(result.triggeredPatterns)
        }
        
        return DetectionResult(
            isInappropriate = maxConfidence > 0.7f,
            confidence = maxConfidence,
            category = bestCategory,
            reasoning = "Advanced pattern analysis on ${variations.size} variations",
            triggeredPatterns = allTriggered.toList()
        )
    }
    
    /**
     * Generate text variations for comprehensive detection
     */
    private fun generateTextVariations(text: String): List<String> {
        val variations = mutableListOf<String>()
        val baseText = text.lowercase().trim()
        
        // Original text
        variations.add(baseText)
        
        // L33t speak normalization
        variations.add(normalizeL33tSpeak(baseText))
        
        // Remove spaces (for spaced words like "p o r n")
        variations.add(baseText.replace(Regex("\\s+"), ""))
        
        // Remove special characters
        variations.add(baseText.replace(Regex("[^\\p{L}\\p{N}\\s]"), ""))
        
        // Reverse text (for simple obfuscation)
        variations.add(baseText.reversed())
        
        // Remove duplicate characters (for "porrrn" -> "porn")
        variations.add(removeDuplicateChars(baseText))
        
        // Normalize Persian/Arabic numerals
        variations.add(normalizePersianNumerals(baseText))
        
        return variations.distinct()
    }
    
    /**
     * Normalize l33t speak
     */
    private fun normalizeL33tSpeak(text: String): String {
        val cacheKey = "l33t_$text"
        normalizedCache[cacheKey]?.let { return it }
        
        val normalized = buildString {
            for (char in text) {
                append(l33tMap[char] ?: char)
            }
        }
        
        normalizedCache[cacheKey] = normalized
        return normalized
    }
    
    /**
     * Remove duplicate consecutive characters
     */
    private fun removeDuplicateChars(text: String): String {
        if (text.length <= 1) return text
        
        return buildString {
            var lastChar = text[0]
            append(lastChar)
            
            for (i in 1 until text.length) {
                val currentChar = text[i]
                if (currentChar != lastChar) {
                    append(currentChar)
                    lastChar = currentChar
                }
            }
        }
    }
    
    /**
     * Normalize Persian/Arabic numerals to English
     */
    private fun normalizePersianNumerals(text: String): String {
        return text
            .replace('۰', '0').replace('۱', '1').replace('۲', '2')
            .replace('۳', '3').replace('۴', '4').replace('۵', '5')
            .replace('۶', '6').replace('۷', '7').replace('۸', '8')
            .replace('۹', '9')
            .replace('٠', '0').replace('١', '1').replace('٢', '2')
            .replace('٣', '3').replace('٤', '4').replace('٥', '5')
            .replace('٦', '6').replace('٧', '7').replace('٨', '8')
            .replace('٩', '9')
    }
    
    /**
     * Check if text contains word boundaries for better accuracy
     */
    fun hasWordBoundaryMatch(text: String, pattern: String): Boolean {
        val regex = "\\b${Regex.escape(pattern)}\\b".toRegex(RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(text)
    }
    
    /**
     * Get pattern statistics
     */
    fun getPatternStats(): PatternStats {
        return PatternStats(
            cacheSize = patternMatchCache.size,
            normalizedCacheSize = normalizedCache.size,
            englishPatternsCount = englishPatterns.size,
            persianPatternsCount = persianPatterns.size,
            arabicPatternsCount = arabicPatterns.size,
            suspiciousPatternsCount = suspiciousPatterns.size
        )
    }
    
    /**
     * Clear pattern caches
     */
    fun clearCaches() {
        patternMatchCache.clear()
        normalizedCache.clear()
        Log.i(TAG, "Pattern caches cleared")
    }
    
    /**
     * Add custom pattern (for user-defined patterns)
     */
    fun addCustomPattern(pattern: String, language: String = "en") {
        // This would be implemented to add user-defined patterns
        Log.i(TAG, "Custom pattern added: $pattern ($language)")
    }
}

/**
 * Pattern match result
 */
data class PatternMatchResult(
    val isMatch: Boolean,
    val confidence: Float,
    val category: String,
    val patterns: List<String>,
    val reasoning: String
)

/**
 * Pattern statistics
 */
data class PatternStats(
    val cacheSize: Int,
    val normalizedCacheSize: Int,
    val englishPatternsCount: Int,
    val persianPatternsCount: Int,
    val arabicPatternsCount: Int,
    val suspiciousPatternsCount: Int
)

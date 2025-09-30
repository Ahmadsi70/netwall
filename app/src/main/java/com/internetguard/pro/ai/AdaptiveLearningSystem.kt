package com.internetguard.pro.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Adaptive Learning System
 * 
 * Features:
 * - Learn from user feedback (false positives, missed content)
 * - Dynamic pattern adjustment
 * - User-specific customization
 * - Pattern frequency tracking
 * - Confidence score adaptation
 */
class AdaptiveLearningSystem {
    
    companion object {
        private const val TAG = "AdaptiveLearningSystem"
        private const val PREFS_NAME = "adaptive_learning"
        private const val MAX_WHITELIST_SIZE = 200
        private const val MAX_BLACKLIST_SIZE = 200
        private const val MAX_PATTERN_FREQUENCY = 100
    }
    
    // Learning data structures
    private val userWhitelist = ConcurrentHashMap<String, WhitelistEntry>()
    private val userBlacklist = ConcurrentHashMap<String, BlacklistEntry>()
    private val patternFrequency = ConcurrentHashMap<String, PatternFrequency>()
    private val confidenceAdjustments = ConcurrentHashMap<String, Float>()
    
    // Statistics
    private var totalFeedbackCount = 0L
    private var falsePositiveCount = 0L
    private var missedContentCount = 0L
    private var approvedBlockCount = 0L
    
    /**
     * Process user feedback and adapt the system
     */
    fun processFeedback(text: String, userAction: UserAction) {
        totalFeedbackCount++
        
        when (userAction) {
            UserAction.FALSE_POSITIVE -> {
                handleFalsePositive(text)
                falsePositiveCount++
            }
            UserAction.MISSED_CONTENT -> {
                handleMissedContent(text)
                missedContentCount++
            }
            UserAction.APPROVED_BLOCK -> {
                handleApprovedBlock(text)
                approvedBlockCount++
            }
            UserAction.NEUTRAL -> {
                // No action needed
            }
        }
        
        updatePatternFrequency(text, userAction)
        Log.d(TAG, "Processed feedback: $userAction for text length ${text.length}")
    }
    
    /**
     * Handle false positive feedback
     */
    private fun handleFalsePositive(text: String) {
        val normalizedText = normalizeText(text)
        val textHash = normalizedText.hashCode().toString()
        
        // Add to whitelist
        userWhitelist[textHash] = WhitelistEntry(
            originalText = text,
            normalizedText = normalizedText,
            addedTimestamp = System.currentTimeMillis(),
            confidence = 0.9f,
            source = "user_feedback"
        )
        
        // Reduce confidence for similar patterns
        val patterns = extractPatterns(text)
        patterns.forEach { pattern ->
            val currentAdjustment = confidenceAdjustments.getOrDefault(pattern, 1.0f)
            confidenceAdjustments[pattern] = (currentAdjustment * 0.8f).coerceAtLeast(0.1f)
        }
        
        // Manage whitelist size
        if (userWhitelist.size > MAX_WHITELIST_SIZE) {
            removeOldestWhitelistEntries()
        }
        
        Log.i(TAG, "Added to whitelist: ${normalizedText.take(50)}...")
    }
    
    /**
     * Handle missed content feedback
     */
    private fun handleMissedContent(text: String) {
        val normalizedText = normalizeText(text)
        val textHash = normalizedText.hashCode().toString()
        
        // Add to blacklist
        userBlacklist[textHash] = BlacklistEntry(
            originalText = text,
            normalizedText = normalizedText,
            addedTimestamp = System.currentTimeMillis(),
            confidence = 0.9f,
            source = "user_feedback",
            category = "user_defined"
        )
        
        // Increase confidence for similar patterns
        val patterns = extractPatterns(text)
        patterns.forEach { pattern ->
            val currentAdjustment = confidenceAdjustments.getOrDefault(pattern, 1.0f)
            confidenceAdjustments[pattern] = (currentAdjustment * 1.2f).coerceAtMost(2.0f)
        }
        
        // Manage blacklist size
        if (userBlacklist.size > MAX_BLACKLIST_SIZE) {
            removeOldestBlacklistEntries()
        }
        
        Log.i(TAG, "Added to blacklist: ${normalizedText.take(50)}...")
    }
    
    /**
     * Handle approved block feedback
     */
    private fun handleApprovedBlock(text: String) {
        val patterns = extractPatterns(text)
        
        // Reinforce confidence for these patterns
        patterns.forEach { pattern ->
            val currentAdjustment = confidenceAdjustments.getOrDefault(pattern, 1.0f)
            confidenceAdjustments[pattern] = (currentAdjustment * 1.1f).coerceAtMost(1.5f)
        }
        
        Log.d(TAG, "Reinforced patterns from approved block")
    }
    
    /**
     * Update pattern frequency tracking
     */
    private fun updatePatternFrequency(text: String, action: UserAction) {
        val patterns = extractPatterns(text)
        
        patterns.forEach { pattern ->
            val frequency = patternFrequency.getOrPut(pattern) {
                PatternFrequency(pattern, 0, 0, 0, 0)
            }
            
            when (action) {
                UserAction.FALSE_POSITIVE -> frequency.falsePositiveCount++
                UserAction.MISSED_CONTENT -> frequency.missedCount++
                UserAction.APPROVED_BLOCK -> frequency.approvedCount++
                UserAction.NEUTRAL -> frequency.neutralCount++
            }
            
            frequency.totalCount++
        }
        
        // Manage pattern frequency size
        if (patternFrequency.size > MAX_PATTERN_FREQUENCY) {
            removeInfrequentPatterns()
        }
    }
    
    /**
     * Check if text should be whitelisted based on learning
     */
    fun isWhitelisted(text: String): Boolean {
        val normalizedText = normalizeText(text)
        val textHash = normalizedText.hashCode().toString()
        
        // Direct match
        if (userWhitelist.containsKey(textHash)) {
            return true
        }
        
        // Similarity match
        return userWhitelist.values.any { entry ->
            calculateSimilarity(normalizedText, entry.normalizedText) > 0.8
        }
    }
    
    /**
     * Check if text should be blacklisted based on learning
     */
    fun isBlacklisted(text: String): BlacklistEntry? {
        val normalizedText = normalizeText(text)
        val textHash = normalizedText.hashCode().toString()
        
        // Direct match
        userBlacklist[textHash]?.let { return it }
        
        // Similarity match
        return userBlacklist.values.find { entry ->
            calculateSimilarity(normalizedText, entry.normalizedText) > 0.8
        }
    }
    
    /**
     * Get confidence adjustment for a pattern
     */
    fun getConfidenceAdjustment(pattern: String): Float {
        return confidenceAdjustments.getOrDefault(pattern, 1.0f)
    }
    
    /**
     * Get adjusted confidence based on learning
     */
    fun getAdjustedConfidence(originalConfidence: Float, patterns: List<String>): Float {
        if (patterns.isEmpty()) return originalConfidence
        
        val adjustments = patterns.mapNotNull { pattern ->
            confidenceAdjustments[pattern]
        }
        
        if (adjustments.isEmpty()) return originalConfidence
        
        val averageAdjustment = adjustments.average().toFloat()
        return (originalConfidence * averageAdjustment).coerceIn(0f, 1f)
    }
    
    /**
     * Extract patterns from text for learning
     */
    private fun extractPatterns(text: String): List<String> {
        val patterns = mutableListOf<String>()
        val normalizedText = normalizeText(text)
        val words = normalizedText.split(Regex("\\s+")).filter { it.length > 2 }
        
        // Individual words
        patterns.addAll(words)
        
        // Bigrams
        for (i in 0 until words.size - 1) {
            patterns.add("${words[i]} ${words[i + 1]}")
        }
        
        // Character patterns
        val charPatterns = listOf(
            Regex("\\d+\\+"), // Age restrictions
            Regex("[a-z]\\d+[a-z]"), // L33t speak
            Regex("\\b[a-z]\\s+[a-z]\\b"), // Spaced letters
            Regex("xxx+", RegexOption.IGNORE_CASE) // XXX patterns
        )
        
        charPatterns.forEach { regex ->
            regex.findAll(normalizedText).forEach { match ->
                patterns.add(match.value)
            }
        }
        
        return patterns.distinct()
    }
    
    /**
     * Normalize text for consistent processing
     */
    private fun normalizeText(text: String): String {
        return text
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .trim()
    }
    
    /**
     * Calculate similarity between two texts
     */
    private fun calculateSimilarity(text1: String, text2: String): Double {
        val words1 = text1.split(Regex("\\s+")).toSet()
        val words2 = text2.split(Regex("\\s+")).toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
    
    /**
     * Remove oldest whitelist entries
     */
    private fun removeOldestWhitelistEntries() {
        val sortedEntries = userWhitelist.entries.sortedBy { it.value.addedTimestamp }
        val entriesToRemove = sortedEntries.take(MAX_WHITELIST_SIZE / 5) // Remove 20%
        
        entriesToRemove.forEach { (key, _) ->
            userWhitelist.remove(key)
        }
        
        Log.d(TAG, "Removed ${entriesToRemove.size} old whitelist entries")
    }
    
    /**
     * Remove oldest blacklist entries
     */
    private fun removeOldestBlacklistEntries() {
        val sortedEntries = userBlacklist.entries.sortedBy { it.value.addedTimestamp }
        val entriesToRemove = sortedEntries.take(MAX_BLACKLIST_SIZE / 5) // Remove 20%
        
        entriesToRemove.forEach { (key, _) ->
            userBlacklist.remove(key)
        }
        
        Log.d(TAG, "Removed ${entriesToRemove.size} old blacklist entries")
    }
    
    /**
     * Remove infrequent patterns
     */
    private fun removeInfrequentPatterns() {
        val infrequentPatterns = patternFrequency.entries.filter { (_, frequency) ->
            frequency.totalCount < 3 && 
            System.currentTimeMillis() - frequency.lastUpdated > 7 * 24 * 60 * 60 * 1000 // 7 days
        }
        
        infrequentPatterns.forEach { (pattern, _) ->
            patternFrequency.remove(pattern)
            confidenceAdjustments.remove(pattern)
        }
        
        Log.d(TAG, "Removed ${infrequentPatterns.size} infrequent patterns")
    }
    
    /**
     * Get learning statistics
     */
    fun getLearningStats(): LearningStats {
        return LearningStats(
            totalFeedbackCount = totalFeedbackCount,
            falsePositiveCount = falsePositiveCount,
            missedContentCount = missedContentCount,
            approvedBlockCount = approvedBlockCount,
            whitelistSize = userWhitelist.size,
            blacklistSize = userBlacklist.size,
            patternFrequencySize = patternFrequency.size,
            confidenceAdjustmentSize = confidenceAdjustments.size
        )
    }
    
    /**
     * Export learning data for backup
     */
    fun exportLearningData(): LearningData {
        return LearningData(
            whitelist = userWhitelist.values.toList(),
            blacklist = userBlacklist.values.toList(),
            patternFrequencies = patternFrequency.values.toList(),
            confidenceAdjustments = confidenceAdjustments.toMap(),
            stats = getLearningStats()
        )
    }
    
    /**
     * Import learning data from backup
     */
    fun importLearningData(data: LearningData) {
        userWhitelist.clear()
        userBlacklist.clear()
        patternFrequency.clear()
        confidenceAdjustments.clear()
        
        data.whitelist.forEach { entry ->
            val key = entry.normalizedText.hashCode().toString()
            userWhitelist[key] = entry
        }
        
        data.blacklist.forEach { entry ->
            val key = entry.normalizedText.hashCode().toString()
            userBlacklist[key] = entry
        }
        
        data.patternFrequencies.forEach { frequency ->
            patternFrequency[frequency.pattern] = frequency
        }
        
        confidenceAdjustments.putAll(data.confidenceAdjustments)
        
        Log.i(TAG, "Imported learning data: ${data.whitelist.size} whitelist, ${data.blacklist.size} blacklist")
    }
    
    /**
     * Clear all learning data
     */
    fun clearLearningData() {
        userWhitelist.clear()
        userBlacklist.clear()
        patternFrequency.clear()
        confidenceAdjustments.clear()
        
        totalFeedbackCount = 0
        falsePositiveCount = 0
        missedContentCount = 0
        approvedBlockCount = 0
        
        Log.i(TAG, "Learning data cleared")
    }
    
    /**
     * Release resources
     */
    fun release() {
        clearLearningData()
        Log.i(TAG, "AdaptiveLearningSystem resources released")
    }
}

/**
 * Whitelist entry
 */
data class WhitelistEntry(
    val originalText: String,
    val normalizedText: String,
    val addedTimestamp: Long,
    val confidence: Float,
    val source: String
)

/**
 * Blacklist entry
 */
data class BlacklistEntry(
    val originalText: String,
    val normalizedText: String,
    val addedTimestamp: Long,
    val confidence: Float,
    val source: String,
    val category: String
)

/**
 * Pattern frequency tracking
 */
data class PatternFrequency(
    val pattern: String,
    var totalCount: Int,
    var falsePositiveCount: Int,
    var missedCount: Int,
    var approvedCount: Int,
    var neutralCount: Int = 0,
    var lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Learning statistics
 */
data class LearningStats(
    val totalFeedbackCount: Long,
    val falsePositiveCount: Long,
    val missedContentCount: Long,
    val approvedBlockCount: Long,
    val whitelistSize: Int,
    val blacklistSize: Int,
    val patternFrequencySize: Int,
    val confidenceAdjustmentSize: Int
)

/**
 * Learning data for export/import
 */
data class LearningData(
    val whitelist: List<WhitelistEntry>,
    val blacklist: List<BlacklistEntry>,
    val patternFrequencies: List<PatternFrequency>,
    val confidenceAdjustments: Map<String, Float>,
    val stats: LearningStats
)

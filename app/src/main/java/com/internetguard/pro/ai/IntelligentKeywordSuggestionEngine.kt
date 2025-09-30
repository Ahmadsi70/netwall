package com.internetguard.pro.ai

import android.util.Log
import kotlin.math.*

/**
 * Intelligent Keyword Suggestion Engine
 * 
 * This engine uses advanced pattern recognition, morphological analysis,
 * and semantic processing to generate relevant keyword suggestions
 * without requiring hard-coded word lists.
 */
class IntelligentKeywordSuggestionEngine {
    
    companion object {
        private const val TAG = "IntelligentAI"
        private const val MIN_SIMILARITY_THRESHOLD = 0.6f
        private const val MAX_SUGGESTIONS = 12
    }
    
    private val patternAnalyzer = PatternAnalyzer()
    private val morphologyProcessor = MorphologyProcessor()
    private val semanticAnalyzer = SemanticAnalyzer()
    private val contextDetector = ContextDetector()
    
    /**
     * Main function to generate intelligent suggestions for any keyword
     */
    fun generateSuggestions(keyword: String): List<String> {
        if (keyword.isBlank() || keyword.length < 2) return emptyList()
        
        val startTime = System.currentTimeMillis()
        val normalizedKeyword = keyword.lowercase().trim()
        val suggestions = mutableSetOf<String>()
        
        try {
            // Step 1: Analyze keyword characteristics
            val analysis = analyzeKeyword(normalizedKeyword)
            
            // Step 2: Generate suggestions based on different algorithms
            suggestions.addAll(patternAnalyzer.analyze(normalizedKeyword, analysis))
            suggestions.addAll(morphologyProcessor.process(normalizedKeyword, analysis))
            suggestions.addAll(semanticAnalyzer.generateSemantic(normalizedKeyword, analysis))
            suggestions.addAll(contextDetector.detectAndGenerate(normalizedKeyword, analysis))
            
            // Step 3: Filter and rank suggestions
            val rankedSuggestions = rankSuggestions(
                suggestions.filter { it != normalizedKeyword }.toList(),
                normalizedKeyword,
                analysis
            )
            
            val processingTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Generated ${rankedSuggestions.size} suggestions for '$keyword' in ${processingTime}ms")
            
            return rankedSuggestions.take(MAX_SUGGESTIONS)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating suggestions for '$keyword'", e)
            return emptyList()
        }
    }
    
    /**
     * Analyze keyword to extract characteristics
     */
    private fun analyzeKeyword(keyword: String): KeywordAnalysis {
        return KeywordAnalysis(
            original = keyword,
            length = keyword.length,
            category = contextDetector.detectCategory(keyword),
            riskLevel = assessRiskLevel(keyword),
            complexity = calculateComplexity(keyword),
            phoneticCode = generateSoundex(keyword)
        )
    }
    
    /**
     * Rank suggestions based on relevance and quality
     */
    private fun rankSuggestions(
        suggestions: List<String>,
        originalKeyword: String,
        analysis: KeywordAnalysis
    ): List<String> {
        return suggestions
            .distinctBy { it.lowercase() }
            .filter { it.length > 2 && it.length < 50 }
            .map { suggestion ->
                val score = calculateRelevanceScore(suggestion, originalKeyword, analysis)
                suggestion to score
            }
            .filter { it.second > MIN_SIMILARITY_THRESHOLD }
            .sortedByDescending { it.second }
            .map { it.first }
    }
    
    /**
     * Calculate relevance score between suggestion and original keyword
     */
    private fun calculateRelevanceScore(
        suggestion: String,
        original: String,
        analysis: KeywordAnalysis
    ): Float {
        var score = 0f
        
        // Similarity score (40%)
        score += calculateStringSimilarity(suggestion, original) * 0.4f
        
        // Category relevance (30%)
        if (contextDetector.detectCategory(suggestion) == analysis.category) {
            score += 0.3f
        }
        
        // Length similarity (10%)
        val lengthDiff = abs(suggestion.length - original.length).toFloat()
        val maxLength = max(suggestion.length, original.length).toFloat()
        score += (1f - lengthDiff / maxLength) * 0.1f
        
        // Phonetic similarity (20%)
        if (generateSoundex(suggestion) == analysis.phoneticCode) {
            score += 0.2f
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    /**
     * Calculate string similarity using Levenshtein distance
     */
    private fun calculateStringSimilarity(str1: String, str2: String): Float {
        val longer = if (str1.length > str2.length) str1 else str2
        val shorter = if (str1.length > str2.length) str2 else str1
        
        if (longer.isEmpty()) return 1f
        
        val editDistance = calculateLevenshteinDistance(longer, shorter)
        return (longer.length - editDistance).toFloat() / longer.length
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun calculateLevenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j
        
        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[str1.length][str2.length]
    }
    
    /**
     * Generate Soundex code for phonetic similarity
     */
    private fun generateSoundex(word: String): String {
        if (word.isEmpty()) return "0000"
        
        val soundexMap = mapOf(
            'b' to '1', 'f' to '1', 'p' to '1', 'v' to '1',
            'c' to '2', 'g' to '2', 'j' to '2', 'k' to '2', 'q' to '2', 's' to '2', 'x' to '2', 'z' to '2',
            'd' to '3', 't' to '3',
            'l' to '4',
            'm' to '5', 'n' to '5',
            'r' to '6'
        )
        
        val firstLetter = word.first().uppercaseChar()
        val soundexCode = word.drop(1)
            .mapNotNull { soundexMap[it.lowercaseChar()] }
            .distinct()
            .joinToString("")
            .padEnd(3, '0')
            .take(3)
            
        return "$firstLetter$soundexCode"
    }
    
    /**
     * Assess risk level of keyword
     */
    private fun assessRiskLevel(keyword: String): RiskLevel {
        val riskIndicators = mapOf(
            RiskLevel.HIGH to listOf(
                Regex(".*sex.*"), Regex(".*porn.*"), Regex(".*kill.*"), 
                Regex(".*murder.*"), Regex(".*drug.*"), Regex(".*hate.*")
            ),
            RiskLevel.MEDIUM to listOf(
                Regex(".*adult.*"), Regex(".*mature.*"), Regex(".*fight.*"),
                Regex(".*violence.*"), Regex(".*alcohol.*")
            )
        )
        
        for ((level, patterns) in riskIndicators) {
            if (patterns.any { it.matches(keyword) }) {
                return level
            }
        }
        
        return RiskLevel.LOW
    }
    
    /**
     * Calculate keyword complexity
     */
    private fun calculateComplexity(keyword: String): ComplexityLevel {
        return when {
            keyword.length > 12 || keyword.contains(Regex("[0-9@#$%]")) -> ComplexityLevel.HIGH
            keyword.length > 6 -> ComplexityLevel.MEDIUM
            else -> ComplexityLevel.LOW
        }
    }
}

/**
 * Data class for keyword analysis
 */
data class KeywordAnalysis(
    val original: String,
    val length: Int,
    val category: ContentCategory,
    val riskLevel: RiskLevel,
    val complexity: ComplexityLevel,
    val phoneticCode: String
)

/**
 * Content categories for classification
 */
enum class ContentCategory {
    ADULT, VIOLENCE, SUBSTANCE, HATE, GAMING, SOCIAL, GENERAL
}

/**
 * Risk levels for content assessment
 */
enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

/**
 * Complexity levels for processing
 */
enum class ComplexityLevel {
    LOW, MEDIUM, HIGH
}

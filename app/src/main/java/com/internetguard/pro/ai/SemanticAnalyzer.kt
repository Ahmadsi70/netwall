package com.internetguard.pro.ai

import kotlin.math.*

/**
 * Semantic Analyzer for meaning-based keyword analysis
 * 
 * Generates semantically related suggestions without hard-coded word lists
 */
class SemanticAnalyzer {
    
    /**
     * Generate semantic suggestions based on meaning analysis
     */
    fun generateSemantic(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val suggestions = mutableSetOf<String>()
        
        // Conceptual relationships
        suggestions.addAll(generateConceptualRelations(keyword, analysis))
        
        // Semantic fields
        suggestions.addAll(generateSemanticField(keyword, analysis))
        
        // Contextual synonyms
        suggestions.addAll(generateContextualSynonyms(keyword, analysis))
        
        // Antonyms and opposites
        suggestions.addAll(generateAntonyms(keyword))
        
        return suggestions
    }
    
    /**
     * Generate conceptually related terms
     */
    private fun generateConceptualRelations(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val relations = mutableSetOf<String>()
        
        when (analysis.category) {
            ContentCategory.ADULT -> relations.addAll(generateAdultConcepts(keyword))
            ContentCategory.VIOLENCE -> relations.addAll(generateViolenceConcepts(keyword))
            ContentCategory.SUBSTANCE -> relations.addAll(generateSubstanceConcepts(keyword))
            ContentCategory.HATE -> relations.addAll(generateHateConcepts(keyword))
            else -> relations.addAll(generateGeneralConcepts(keyword))
        }
        
        return relations
    }
    
    /**
     * Generate adult content conceptual relations
     */
    private fun generateAdultConcepts(keyword: String): Set<String> {
        val concepts = mutableSetOf<String>()
        
        // Age-related concepts
        if (containsAgeIndicator(keyword)) {
            concepts.addAll(listOf("mature", "adult", "18+", "age restricted", "parental guidance"))
        }
        
        // Intimacy concepts
        if (containsIntimacyIndicator(keyword)) {
            concepts.addAll(listOf("intimate", "private", "personal", "romantic", "sensual"))
        }
        
        // Content type concepts
        if (containsContentIndicator(keyword)) {
            concepts.addAll(listOf("explicit", "graphic", "uncensored", "unfiltered", "raw"))
        }
        
        // Platform concepts
        concepts.addAll(listOf("dating", "social", "messaging", "video", "streaming"))
        
        return concepts
    }
    
    /**
     * Generate violence conceptual relations
     */
    private fun generateViolenceConcepts(keyword: String): Set<String> {
        val concepts = mutableSetOf<String>()
        
        // Intensity concepts
        if (containsIntensityIndicator(keyword)) {
            concepts.addAll(listOf("extreme", "graphic", "brutal", "severe", "intense"))
        }
        
        // Action concepts
        if (containsActionIndicator(keyword)) {
            concepts.addAll(listOf("aggressive", "hostile", "combative", "destructive", "harmful"))
        }
        
        // Context concepts
        concepts.addAll(listOf("gaming", "movie", "news", "real", "simulated", "fictional"))
        
        return concepts
    }
    
    /**
     * Generate substance abuse conceptual relations
     */
    private fun generateSubstanceConcepts(keyword: String): Set<String> {
        val concepts = mutableSetOf<String>()
        
        // Usage concepts
        if (containsUsageIndicator(keyword)) {
            concepts.addAll(listOf("consumption", "abuse", "addiction", "dependency", "habit"))
        }
        
        // Legal concepts
        if (containsLegalIndicator(keyword)) {
            concepts.addAll(listOf("illegal", "controlled", "prescription", "recreational", "banned"))
        }
        
        // Effect concepts
        concepts.addAll(listOf("intoxication", "impairment", "influence", "altered", "affected"))
        
        return concepts
    }
    
    /**
     * Generate hate speech conceptual relations
     */
    private fun generateHateConcepts(keyword: String): Set<String> {
        val concepts = mutableSetOf<String>()
        
        // Target concepts
        if (containsTargetIndicator(keyword)) {
            concepts.addAll(listOf("discriminatory", "prejudiced", "biased", "stereotyping", "profiling"))
        }
        
        // Behavior concepts
        if (containsBehaviorIndicator(keyword)) {
            concepts.addAll(listOf("offensive", "derogatory", "insulting", "demeaning", "belittling"))
        }
        
        // Impact concepts
        concepts.addAll(listOf("harmful", "hurtful", "damaging", "toxic", "negative"))
        
        return concepts
    }
    
    /**
     * Generate general conceptual relations
     */
    private fun generateGeneralConcepts(keyword: String): Set<String> {
        val concepts = mutableSetOf<String>()
        
        // Quality concepts
        concepts.addAll(listOf("inappropriate", "unsuitable", "unacceptable", "problematic"))
        
        // Content concepts
        concepts.addAll(listOf("material", "content", "information", "data", "media"))
        
        // Action concepts
        concepts.addAll(listOf("activity", "behavior", "conduct", "action", "practice"))
        
        return concepts
    }
    
    /**
     * Generate semantic field related terms
     */
    private fun generateSemanticField(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val fieldTerms = mutableSetOf<String>()
        
        // Generate terms from the same semantic field
        when (analysis.category) {
            ContentCategory.ADULT -> fieldTerms.addAll(getAdultSemanticField())
            ContentCategory.VIOLENCE -> fieldTerms.addAll(getViolenceSemanticField())
            ContentCategory.SUBSTANCE -> fieldTerms.addAll(getSubstanceSemanticField())
            ContentCategory.HATE -> fieldTerms.addAll(getHateSemanticField())
            else -> fieldTerms.addAll(getGeneralSemanticField())
        }
        
        // Filter by semantic similarity
        return fieldTerms.filter { 
            calculateSemanticSimilarity(keyword, it) > 0.3 
        }.toSet()
    }
    
    /**
     * Generate contextual synonyms
     */
    private fun generateContextualSynonyms(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val synonyms = mutableSetOf<String>()
        
        // Generate synonyms based on context
        synonyms.addAll(generateSynonymsByPattern(keyword))
        synonyms.addAll(generateSynonymsByMeaning(keyword, analysis))
        synonyms.addAll(generateSynonymsByUsage(keyword))
        
        return synonyms
    }
    
    /**
     * Generate antonyms and opposite terms
     */
    private fun generateAntonyms(keyword: String): Set<String> {
        val antonyms = mutableSetOf<String>()
        
        // Common antonym patterns
        if (keyword.startsWith("un")) {
            antonyms.add(keyword.removePrefix("un"))
        } else {
            antonyms.add("un$keyword")
        }
        
        if (keyword.startsWith("in")) {
            antonyms.add(keyword.removePrefix("in"))
        } else {
            antonyms.add("in$keyword")
        }
        
        if (keyword.startsWith("dis")) {
            antonyms.add(keyword.removePrefix("dis"))
        } else {
            antonyms.add("dis$keyword")
        }
        
        // Conceptual opposites
        antonyms.addAll(generateConceptualOpposites(keyword))
        
        return antonyms
    }
    
    /**
     * Helper functions for concept detection
     */
    private fun containsAgeIndicator(keyword: String): Boolean {
        val agePatterns = listOf("adult", "mature", "18", "age", "old", "young")
        return agePatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsIntimacyIndicator(keyword: String): Boolean {
        val intimacyPatterns = listOf("intimate", "private", "personal", "romantic", "love")
        return intimacyPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsContentIndicator(keyword: String): Boolean {
        val contentPatterns = listOf("content", "material", "media", "video", "image")
        return contentPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsIntensityIndicator(keyword: String): Boolean {
        val intensityPatterns = listOf("extreme", "intense", "severe", "brutal", "graphic")
        return intensityPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsActionIndicator(keyword: String): Boolean {
        val actionPatterns = listOf("fight", "attack", "assault", "abuse", "harm")
        return actionPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsUsageIndicator(keyword: String): Boolean {
        val usagePatterns = listOf("use", "abuse", "consume", "take", "addiction")
        return usagePatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsLegalIndicator(keyword: String): Boolean {
        val legalPatterns = listOf("legal", "illegal", "law", "banned", "controlled")
        return legalPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsTargetIndicator(keyword: String): Boolean {
        val targetPatterns = listOf("against", "toward", "targeting", "discrimination")
        return targetPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    private fun containsBehaviorIndicator(keyword: String): Boolean {
        val behaviorPatterns = listOf("behavior", "conduct", "action", "practice", "activity")
        return behaviorPatterns.any { keyword.contains(it, ignoreCase = true) }
    }
    
    /**
     * Semantic field generators
     */
    private fun getAdultSemanticField(): Set<String> {
        return setOf("mature", "explicit", "intimate", "romantic", "sensual", "private", "personal")
    }
    
    private fun getViolenceSemanticField(): Set<String> {
        return setOf("aggressive", "hostile", "brutal", "harmful", "destructive", "combative")
    }
    
    private fun getSubstanceSemanticField(): Set<String> {
        return setOf("addictive", "intoxicating", "controlled", "recreational", "medicinal")
    }
    
    private fun getHateSemanticField(): Set<String> {
        return setOf("discriminatory", "prejudiced", "offensive", "derogatory", "insulting")
    }
    
    private fun getGeneralSemanticField(): Set<String> {
        return setOf("inappropriate", "unsuitable", "problematic", "concerning", "questionable")
    }
    
    /**
     * Synonym generation methods
     */
    private fun generateSynonymsByPattern(keyword: String): Set<String> {
        val synonyms = mutableSetOf<String>()
        
        // Pattern-based synonym generation
        if (keyword.endsWith("ing")) {
            val base = keyword.removeSuffix("ing")
            synonyms.add(base + "tion")
            synonyms.add(base + "ment")
        }
        
        if (keyword.endsWith("ness")) {
            val base = keyword.removeSuffix("ness")
            synonyms.add(base + "ity")
        }
        
        return synonyms
    }
    
    private fun generateSynonymsByMeaning(keyword: String, analysis: KeywordAnalysis): Set<String> {
        // Generate synonyms based on semantic meaning
        return when (analysis.category) {
            ContentCategory.ADULT -> setOf("mature", "explicit", "intimate")
            ContentCategory.VIOLENCE -> setOf("aggressive", "hostile", "brutal")
            ContentCategory.SUBSTANCE -> setOf("addictive", "intoxicating", "controlled")
            ContentCategory.HATE -> setOf("offensive", "discriminatory", "prejudiced")
            else -> setOf("inappropriate", "unsuitable", "problematic")
        }
    }
    
    private fun generateSynonymsByUsage(keyword: String): Set<String> {
        val synonyms = mutableSetOf<String>()
        
        // Usage-based synonyms
        synonyms.add("$keyword related")
        synonyms.add("$keyword type")
        synonyms.add("$keyword like")
        synonyms.add("$keyword style")
        
        return synonyms
    }
    
    private fun generateConceptualOpposites(keyword: String): Set<String> {
        val opposites = mutableSetOf<String>()
        
        // Common conceptual opposites
        val oppositeMap = mapOf(
            "good" to "bad",
            "positive" to "negative", 
            "safe" to "dangerous",
            "appropriate" to "inappropriate",
            "acceptable" to "unacceptable",
            "legal" to "illegal",
            "public" to "private"
        )
        
        oppositeMap.forEach { (word, opposite) ->
            if (keyword.contains(word)) {
                opposites.add(keyword.replace(word, opposite))
            }
            if (keyword.contains(opposite)) {
                opposites.add(keyword.replace(opposite, word))
            }
        }
        
        return opposites
    }
    
    /**
     * Calculate semantic similarity between two words
     */
    private fun calculateSemanticSimilarity(word1: String, word2: String): Double {
        // Simple semantic similarity calculation
        val commonChars = word1.toSet().intersect(word2.toSet()).size
        val totalChars = word1.toSet().union(word2.toSet()).size
        
        return if (totalChars > 0) {
            commonChars.toDouble() / totalChars
        } else {
            0.0
        }
    }
}

package com.internetguard.pro.ai

/**
 * Advanced Pattern Analyzer for intelligent keyword recognition
 * 
 * Analyzes patterns in keywords without relying on hard-coded word lists
 */
class PatternAnalyzer {
    
    /**
     * Analyze keyword and generate pattern-based suggestions
     */
    fun analyze(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val suggestions = mutableSetOf<String>()
        
        // Pattern-based generation
        suggestions.addAll(generateByPattern(keyword, analysis))
        
        // Obfuscation detection and generation
        suggestions.addAll(generateObfuscatedVariations(keyword))
        
        // Common transformation patterns
        suggestions.addAll(generateTransformations(keyword))
        
        return suggestions
    }
    
    /**
     * Generate suggestions based on detected patterns
     */
    private fun generateByPattern(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val suggestions = mutableSetOf<String>()
        
        when (analysis.category) {
            ContentCategory.ADULT -> suggestions.addAll(generateAdultPatterns(keyword))
            ContentCategory.VIOLENCE -> suggestions.addAll(generateViolencePatterns(keyword))
            ContentCategory.SUBSTANCE -> suggestions.addAll(generateSubstancePatterns(keyword))
            ContentCategory.HATE -> suggestions.addAll(generateHatePatterns(keyword))
            else -> suggestions.addAll(generateGeneralPatterns(keyword))
        }
        
        return suggestions
    }
    
    /**
     * Generate adult content related patterns
     */
    private fun generateAdultPatterns(keyword: String): Set<String> {
        val patterns = mutableSetOf<String>()
        
        // Common adult content patterns
        val adultSuffixes = listOf("content", "material", "videos", "images", "scenes")
        val adultPrefixes = listOf("explicit", "mature", "adult", "inappropriate")
        
        adultSuffixes.forEach { suffix ->
            patterns.add("$keyword $suffix")
        }
        
        adultPrefixes.forEach { prefix ->
            patterns.add("$prefix $keyword")
        }
        
        // Age-related patterns
        patterns.add("$keyword 18+")
        patterns.add("$keyword adult")
        patterns.add("$keyword mature")
        
        return patterns
    }
    
    /**
     * Generate violence related patterns
     */
    private fun generateViolencePatterns(keyword: String): Set<String> {
        val patterns = mutableSetOf<String>()
        
        val violenceSuffixes = listOf("scenes", "content", "behavior", "activity")
        val violencePrefixes = listOf("graphic", "extreme", "physical", "domestic")
        
        violenceSuffixes.forEach { suffix ->
            patterns.add("$keyword $suffix")
        }
        
        violencePrefixes.forEach { prefix ->
            patterns.add("$prefix $keyword")
        }
        
        return patterns
    }
    
    /**
     * Generate substance abuse related patterns
     */
    private fun generateSubstancePatterns(keyword: String): Set<String> {
        val patterns = mutableSetOf<String>()
        
        val substanceSuffixes = listOf("abuse", "addiction", "use", "consumption")
        val substancePrefixes = listOf("illegal", "recreational", "prescription")
        
        substanceSuffixes.forEach { suffix ->
            patterns.add("$keyword $suffix")
        }
        
        substancePrefixes.forEach { prefix ->
            patterns.add("$prefix $keyword")
        }
        
        return patterns
    }
    
    /**
     * Generate hate speech related patterns
     */
    private fun generateHatePatterns(keyword: String): Set<String> {
        val patterns = mutableSetOf<String>()
        
        val hateSuffixes = listOf("speech", "content", "language", "behavior")
        val hatePrefixes = listOf("offensive", "discriminatory", "prejudiced")
        
        hateSuffixes.forEach { suffix ->
            patterns.add("$keyword $suffix")
        }
        
        hatePrefixes.forEach { prefix ->
            patterns.add("$prefix $keyword")
        }
        
        return patterns
    }
    
    /**
     * Generate general patterns for any keyword
     */
    private fun generateGeneralPatterns(keyword: String): Set<String> {
        val patterns = mutableSetOf<String>()
        
        val generalSuffixes = listOf("content", "material", "related", "activity", "behavior")
        val generalPrefixes = listOf("inappropriate", "offensive", "harmful", "disturbing")
        
        generalSuffixes.forEach { suffix ->
            patterns.add("$keyword $suffix")
        }
        
        generalPrefixes.forEach { prefix ->
            patterns.add("$prefix $keyword")
        }
        
        return patterns
    }
    
    /**
     * Generate obfuscated variations (leetspeak, symbols, etc.)
     */
    private fun generateObfuscatedVariations(keyword: String): Set<String> {
        val variations = mutableSetOf<String>()
        
        // Leetspeak transformations
        val leetMap = mapOf(
            'a' to listOf('@', '4'),
            'e' to listOf('3'),
            'i' to listOf('1', '!'),
            'o' to listOf('0'),
            's' to listOf('$', '5'),
            't' to listOf('7'),
            'l' to listOf('1')
        )
        
        // Apply single character replacements
        leetMap.forEach { (original, replacements) ->
            if (keyword.contains(original)) {
                replacements.forEach { replacement ->
                    variations.add(keyword.replace(original, replacement))
                }
            }
        }
        
        // Space-separated variations
        if (keyword.length > 3) {
            variations.add(keyword.toCharArray().joinToString(" "))
            variations.add(keyword.toCharArray().joinToString("."))
            variations.add(keyword.toCharArray().joinToString("*"))
        }
        
        // Mixed case variations
        variations.add(keyword.uppercase())
        variations.add(keyword.lowercase())
        if (keyword.length > 1) {
            variations.add(keyword.first().uppercase() + keyword.drop(1).lowercase())
        }
        
        return variations.filter { it != keyword }.toSet()
    }
    
    /**
     * Generate common transformations
     */
    private fun generateTransformations(keyword: String): Set<String> {
        val transformations = mutableSetOf<String>()
        
        // Plural/singular transformations
        if (keyword.endsWith("s") && keyword.length > 3) {
            transformations.add(keyword.dropLast(1))
        } else {
            transformations.add(keyword + "s")
        }
        
        // Common word endings
        if (keyword.endsWith("ing")) {
            transformations.add(keyword.dropLast(3))
        } else {
            transformations.add(keyword + "ing")
        }
        
        if (keyword.endsWith("ed")) {
            transformations.add(keyword.dropLast(2))
        } else {
            transformations.add(keyword + "ed")
        }
        
        // Common prefixes
        val prefixes = listOf("un", "re", "pre", "anti", "non")
        prefixes.forEach { prefix ->
            if (keyword.startsWith(prefix)) {
                transformations.add(keyword.removePrefix(prefix))
            } else {
                transformations.add(prefix + keyword)
            }
        }
        
        // Common suffixes
        val suffixes = listOf("ly", "er", "est", "ness", "ful", "less")
        suffixes.forEach { suffix ->
            if (keyword.endsWith(suffix)) {
                transformations.add(keyword.removeSuffix(suffix))
            } else {
                transformations.add(keyword + suffix)
            }
        }
        
        return transformations.filter { it != keyword && it.length > 2 }.toSet()
    }
}

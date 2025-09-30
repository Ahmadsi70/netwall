package com.internetguard.pro.ai

/**
 * Morphology Processor for word structure analysis and generation
 * 
 * Processes word morphology to generate related terms without hard-coded lists
 */
class MorphologyProcessor {
    
    /**
     * Process keyword morphology and generate variations
     */
    fun process(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val suggestions = mutableSetOf<String>()
        
        // Basic morphological transformations
        suggestions.addAll(generateBasicForms(keyword))
        
        // Advanced morphological analysis
        suggestions.addAll(analyzeWordStructure(keyword))
        
        // Derivational morphology
        suggestions.addAll(generateDerivations(keyword))
        
        // Inflectional morphology
        suggestions.addAll(generateInflections(keyword))
        
        return suggestions.filter { it != keyword && it.length > 2 }.toSet()
    }
    
    /**
     * Generate basic word forms
     */
    private fun generateBasicForms(keyword: String): Set<String> {
        val forms = mutableSetOf<String>()
        
        // Present participle
        forms.add(keyword + "ing")
        
        // Past tense (simple rule)
        if (keyword.endsWith("e")) {
            forms.add(keyword + "d")
        } else {
            forms.add(keyword + "ed")
        }
        
        // Plural forms
        when {
            keyword.endsWith("y") && keyword.length > 1 && !isVowel(keyword[keyword.length - 2]) -> {
                forms.add(keyword.dropLast(1) + "ies")
            }
            keyword.endsWith("s") || keyword.endsWith("sh") || keyword.endsWith("ch") || 
            keyword.endsWith("x") || keyword.endsWith("z") -> {
                forms.add(keyword + "es")
            }
            else -> {
                forms.add(keyword + "s")
            }
        }
        
        // Comparative and superlative
        if (keyword.length <= 6) { // Short adjectives
            if (keyword.endsWith("y")) {
                val base = keyword.dropLast(1)
                forms.add(base + "ier")
                forms.add(base + "iest")
            } else {
                forms.add(keyword + "er")
                forms.add(keyword + "est")
            }
        }
        
        return forms
    }
    
    /**
     * Analyze word structure and generate related terms
     */
    private fun analyzeWordStructure(keyword: String): Set<String> {
        val suggestions = mutableSetOf<String>()
        
        // Analyze prefixes
        suggestions.addAll(analyzePrefixes(keyword))
        
        // Analyze suffixes
        suggestions.addAll(analyzeSuffixes(keyword))
        
        // Analyze root words
        suggestions.addAll(analyzeRoots(keyword))
        
        return suggestions
    }
    
    /**
     * Analyze and generate prefix variations
     */
    private fun analyzePrefixes(keyword: String): Set<String> {
        val variations = mutableSetOf<String>()
        
        val commonPrefixes = mapOf(
            "un" to "not",
            "re" to "again",
            "pre" to "before", 
            "mis" to "wrong",
            "dis" to "not",
            "anti" to "against",
            "non" to "not",
            "over" to "too much",
            "under" to "too little",
            "super" to "above",
            "sub" to "below",
            "inter" to "between",
            "trans" to "across"
        )
        
        // Remove existing prefixes
        commonPrefixes.keys.forEach { prefix ->
            if (keyword.startsWith(prefix) && keyword.length > prefix.length + 2) {
                val root = keyword.removePrefix(prefix)
                variations.add(root)
                
                // Add other prefixes to the root
                commonPrefixes.keys.filter { it != prefix }.forEach { otherPrefix ->
                    variations.add(otherPrefix + root)
                }
            }
        }
        
        // Add prefixes to root word
        if (!commonPrefixes.keys.any { keyword.startsWith(it) }) {
            commonPrefixes.keys.forEach { prefix ->
                variations.add(prefix + keyword)
            }
        }
        
        return variations
    }
    
    /**
     * Analyze and generate suffix variations
     */
    private fun analyzeSuffixes(keyword: String): Set<String> {
        val variations = mutableSetOf<String>()
        
        val commonSuffixes = mapOf(
            "ing" to "action",
            "ed" to "past",
            "er" to "person/comparative",
            "est" to "superlative",
            "ly" to "adverb",
            "ness" to "quality",
            "ful" to "full of",
            "less" to "without",
            "able" to "capable of",
            "ible" to "capable of",
            "tion" to "action/state",
            "sion" to "action/state",
            "ment" to "result/state",
            "ity" to "quality",
            "ous" to "having quality",
            "ive" to "having tendency"
        )
        
        // Remove existing suffixes
        commonSuffixes.keys.forEach { suffix ->
            if (keyword.endsWith(suffix) && keyword.length > suffix.length + 2) {
                val root = keyword.removeSuffix(suffix)
                variations.add(root)
                
                // Add other suffixes to the root
                commonSuffixes.keys.filter { it != suffix }.forEach { otherSuffix ->
                    variations.add(root + otherSuffix)
                }
            }
        }
        
        // Add suffixes to root word
        if (!commonSuffixes.keys.any { keyword.endsWith(it) }) {
            commonSuffixes.keys.forEach { suffix ->
                variations.add(keyword + suffix)
            }
        }
        
        return variations
    }
    
    /**
     * Analyze root words and generate variations
     */
    private fun analyzeRoots(keyword: String): Set<String> {
        val variations = mutableSetOf<String>()
        
        // Try to find the root by removing common patterns
        val possibleRoot = findRoot(keyword)
        if (possibleRoot != keyword) {
            variations.add(possibleRoot)
            
            // Generate new forms from the root
            variations.addAll(generateBasicForms(possibleRoot))
        }
        
        return variations
    }
    
    /**
     * Generate derivational morphology variations
     */
    private fun generateDerivations(keyword: String): Set<String> {
        val derivations = mutableSetOf<String>()
        
        // Noun to adjective conversions
        if (isNoun(keyword)) {
            derivations.add(keyword + "al")    // nation -> national
            derivations.add(keyword + "ic")    // atom -> atomic  
            derivations.add(keyword + "ous")   // fame -> famous
            derivations.add(keyword + "y")     // dirt -> dirty
        }
        
        // Verb to noun conversions
        if (isVerb(keyword)) {
            derivations.add(keyword + "tion")  // act -> action
            derivations.add(keyword + "ment")  // move -> movement
            derivations.add(keyword + "er")    // teach -> teacher
            derivations.add(keyword + "ing")   // build -> building
        }
        
        // Adjective to adverb conversions
        if (isAdjective(keyword)) {
            derivations.add(keyword + "ly")    // quick -> quickly
            derivations.add(keyword + "ness")  // happy -> happiness
        }
        
        return derivations
    }
    
    /**
     * Generate inflectional morphology variations
     */
    private fun generateInflections(keyword: String): Set<String> {
        val inflections = mutableSetOf<String>()
        
        // Verb inflections
        if (isVerb(keyword)) {
            inflections.addAll(generateVerbInflections(keyword))
        }
        
        // Noun inflections  
        if (isNoun(keyword)) {
            inflections.addAll(generateNounInflections(keyword))
        }
        
        // Adjective inflections
        if (isAdjective(keyword)) {
            inflections.addAll(generateAdjectiveInflections(keyword))
        }
        
        return inflections
    }
    
    /**
     * Generate verb inflections
     */
    private fun generateVerbInflections(verb: String): Set<String> {
        val inflections = mutableSetOf<String>()
        
        // Third person singular
        inflections.add(verb + "s")
        
        // Present participle
        if (verb.endsWith("e")) {
            inflections.add(verb.dropLast(1) + "ing")
        } else {
            inflections.add(verb + "ing")
        }
        
        // Past tense and past participle (regular)
        if (verb.endsWith("e")) {
            inflections.add(verb + "d")
        } else if (verb.endsWith("y") && !isVowel(verb[verb.length - 2])) {
            inflections.add(verb.dropLast(1) + "ied")
        } else {
            inflections.add(verb + "ed")
        }
        
        return inflections
    }
    
    /**
     * Generate noun inflections
     */
    private fun generateNounInflections(noun: String): Set<String> {
        val inflections = mutableSetOf<String>()
        
        // Plural forms
        when {
            noun.endsWith("y") && !isVowel(noun[noun.length - 2]) -> {
                inflections.add(noun.dropLast(1) + "ies")
            }
            noun.endsWith("f") -> {
                inflections.add(noun.dropLast(1) + "ves")
            }
            noun.endsWith("fe") -> {
                inflections.add(noun.dropLast(2) + "ves")
            }
            noun.endsWith("s") || noun.endsWith("sh") || noun.endsWith("ch") || 
            noun.endsWith("x") || noun.endsWith("z") -> {
                inflections.add(noun + "es")
            }
            else -> {
                inflections.add(noun + "s")
            }
        }
        
        // Possessive forms
        inflections.add(noun + "'s")
        inflections.add(noun + "s'")
        
        return inflections
    }
    
    /**
     * Generate adjective inflections
     */
    private fun generateAdjectiveInflections(adjective: String): Set<String> {
        val inflections = mutableSetOf<String>()
        
        if (adjective.length <= 6) { // Short adjectives
            // Comparative
            if (adjective.endsWith("y")) {
                inflections.add(adjective.dropLast(1) + "ier")
            } else {
                inflections.add(adjective + "er")
            }
            
            // Superlative
            if (adjective.endsWith("y")) {
                inflections.add(adjective.dropLast(1) + "iest")
            } else {
                inflections.add(adjective + "est")
            }
        }
        
        return inflections
    }
    
    /**
     * Helper functions for word type detection
     */
    private fun isVowel(char: Char): Boolean {
        return char.lowercase() in setOf("a", "e", "i", "o", "u")
    }
    
    private fun isNoun(word: String): Boolean {
        // Simple heuristic for noun detection
        return word.endsWith("tion") || word.endsWith("ness") || word.endsWith("ment") ||
               word.endsWith("er") || word.endsWith("or") || word.endsWith("ist")
    }
    
    private fun isVerb(word: String): Boolean {
        // Simple heuristic for verb detection
        return word.endsWith("ize") || word.endsWith("ify") || word.endsWith("ate") ||
               !word.endsWith("tion") && !word.endsWith("ness") && !word.endsWith("ly")
    }
    
    private fun isAdjective(word: String): Boolean {
        // Simple heuristic for adjective detection
        return word.endsWith("ful") || word.endsWith("less") || word.endsWith("able") ||
               word.endsWith("ible") || word.endsWith("ous") || word.endsWith("ive")
    }
    
    private fun findRoot(word: String): String {
        // Simple root finding algorithm
        val suffixesToRemove = listOf("ing", "ed", "er", "est", "ly", "ness", "ful", "less")
        
        for (suffix in suffixesToRemove) {
            if (word.endsWith(suffix) && word.length > suffix.length + 2) {
                return word.removeSuffix(suffix)
            }
        }
        
        return word
    }
}

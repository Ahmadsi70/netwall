package com.internetguard.pro.ai

/**
 * Context Detector for intelligent content categorization
 * 
 * Detects content category without relying on hard-coded word lists
 */
class ContextDetector {
    
    /**
     * Detect content category based on keyword characteristics
     */
    fun detectCategory(keyword: String): ContentCategory {
        val normalized = keyword.lowercase()
        
        return when {
            isAdultContent(normalized) -> ContentCategory.ADULT
            isViolentContent(normalized) -> ContentCategory.VIOLENCE
            isSubstanceContent(normalized) -> ContentCategory.SUBSTANCE
            isHateContent(normalized) -> ContentCategory.HATE
            isGamingContent(normalized) -> ContentCategory.GAMING
            isSocialContent(normalized) -> ContentCategory.SOCIAL
            else -> ContentCategory.GENERAL
        }
    }
    
    /**
     * Detect and generate context-specific suggestions
     */
    fun detectAndGenerate(keyword: String, analysis: KeywordAnalysis): Set<String> {
        val suggestions = mutableSetOf<String>()
        
        when (analysis.category) {
            ContentCategory.ADULT -> suggestions.addAll(generateAdultContext(keyword))
            ContentCategory.VIOLENCE -> suggestions.addAll(generateViolenceContext(keyword))
            ContentCategory.SUBSTANCE -> suggestions.addAll(generateSubstanceContext(keyword))
            ContentCategory.HATE -> suggestions.addAll(generateHateContext(keyword))
            ContentCategory.GAMING -> suggestions.addAll(generateGamingContext(keyword))
            ContentCategory.SOCIAL -> suggestions.addAll(generateSocialContext(keyword))
            else -> suggestions.addAll(generateGeneralContext(keyword))
        }
        
        return suggestions
    }
    
    /**
     * Detect adult content based on patterns and characteristics
     */
    private fun isAdultContent(keyword: String): Boolean {
        val adultPatterns = listOf(
            // Direct patterns
            Regex(".*sex.*"), Regex(".*porn.*"), Regex(".*adult.*"), 
            Regex(".*explicit.*"), Regex(".*mature.*"), Regex(".*nude.*"),
            Regex(".*intimate.*"), Regex(".*erotic.*"),
            
            // Coded patterns
            Regex(".*18\\+.*"), Regex(".*xxx.*"), Regex(".*nsfw.*"),
            Regex(".*s3x.*"), Regex(".*p0rn.*"),
            
            // Body-related
            Regex(".*breast.*"), Regex(".*genital.*"), Regex(".*private.*"),
            
            // Activity-related
            Regex(".*dating.*"), Regex(".*romance.*"), Regex(".*seductive.*")
        )
        
        return adultPatterns.any { it.matches(keyword) }
    }
    
    /**
     * Detect violent content
     */
    private fun isViolentContent(keyword: String): Boolean {
        val violencePatterns = listOf(
            Regex(".*violence.*"), Regex(".*fight.*"), Regex(".*kill.*"),
            Regex(".*murder.*"), Regex(".*attack.*"), Regex(".*assault.*"),
            Regex(".*abuse.*"), Regex(".*harm.*"), Regex(".*weapon.*"),
            Regex(".*blood.*"), Regex(".*death.*"), Regex(".*brutal.*")
        )
        
        return violencePatterns.any { it.matches(keyword) }
    }
    
    /**
     * Detect substance-related content
     */
    private fun isSubstanceContent(keyword: String): Boolean {
        val substancePatterns = listOf(
            Regex(".*drug.*"), Regex(".*alcohol.*"), Regex(".*smoke.*"),
            Regex(".*cocaine.*"), Regex(".*heroin.*"), Regex(".*marijuana.*"),
            Regex(".*weed.*"), Regex(".*addiction.*"), Regex(".*substance.*"),
            Regex(".*drunk.*"), Regex(".*high.*"), Regex(".*intoxicated.*")
        )
        
        return substancePatterns.any { it.matches(keyword) }
    }
    
    /**
     * Detect hate speech content
     */
    private fun isHateContent(keyword: String): Boolean {
        val hatePatterns = listOf(
            Regex(".*hate.*"), Regex(".*racist.*"), Regex(".*discrimination.*"),
            Regex(".*prejudice.*"), Regex(".*bigotry.*"), Regex(".*stereotype.*"),
            Regex(".*bias.*"), Regex(".*intolerance.*"), Regex(".*offensive.*")
        )
        
        return hatePatterns.any { it.matches(keyword) }
    }
    
    /**
     * Detect gaming-related content
     */
    private fun isGamingContent(keyword: String): Boolean {
        val gamingPatterns = listOf(
            Regex(".*game.*"), Regex(".*gaming.*"), Regex(".*player.*"),
            Regex(".*level.*"), Regex(".*score.*"), Regex(".*achievement.*"),
            Regex(".*multiplayer.*"), Regex(".*online.*"), Regex(".*virtual.*")
        )
        
        return gamingPatterns.any { it.matches(keyword) }
    }
    
    /**
     * Detect social media content
     */
    private fun isSocialContent(keyword: String): Boolean {
        val socialPatterns = listOf(
            Regex(".*social.*"), Regex(".*media.*"), Regex(".*chat.*"),
            Regex(".*message.*"), Regex(".*post.*"), Regex(".*comment.*"),
            Regex(".*share.*"), Regex(".*like.*"), Regex(".*follow.*")
        )
        
        return socialPatterns.any { it.matches(keyword) }
    }
    
    /**
     * Generate adult content context suggestions
     */
    private fun generateAdultContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        // Platform-specific contexts
        contexts.add("$keyword on social media")
        contexts.add("$keyword in messaging apps")
        contexts.add("$keyword in dating apps")
        contexts.add("$keyword in video content")
        contexts.add("$keyword in image sharing")
        
        // Age-related contexts
        contexts.add("$keyword for minors")
        contexts.add("$keyword underage")
        contexts.add("$keyword age verification")
        
        // Content type contexts
        contexts.add("$keyword streaming")
        contexts.add("$keyword download")
        contexts.add("$keyword subscription")
        
        return contexts
    }
    
    /**
     * Generate violence context suggestions
     */
    private fun generateViolenceContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        // Media contexts
        contexts.add("$keyword in games")
        contexts.add("$keyword in movies")
        contexts.add("$keyword in news")
        contexts.add("$keyword in videos")
        
        // Type contexts
        contexts.add("graphic $keyword")
        contexts.add("realistic $keyword")
        contexts.add("simulated $keyword")
        contexts.add("cartoon $keyword")
        
        // Impact contexts
        contexts.add("$keyword against children")
        contexts.add("$keyword against animals")
        contexts.add("domestic $keyword")
        
        return contexts
    }
    
    /**
     * Generate substance context suggestions
     */
    private fun generateSubstanceContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        // Usage contexts
        contexts.add("$keyword consumption")
        contexts.add("$keyword abuse")
        contexts.add("$keyword addiction")
        contexts.add("$keyword dependency")
        
        // Legal contexts
        contexts.add("illegal $keyword")
        contexts.add("prescription $keyword")
        contexts.add("recreational $keyword")
        
        // Age contexts
        contexts.add("underage $keyword")
        contexts.add("minor $keyword")
        
        return contexts
    }
    
    /**
     * Generate hate speech context suggestions
     */
    private fun generateHateContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        // Target contexts
        contexts.add("$keyword against minorities")
        contexts.add("$keyword against religion")
        contexts.add("$keyword against gender")
        contexts.add("$keyword against race")
        
        // Platform contexts
        contexts.add("$keyword in comments")
        contexts.add("$keyword in posts")
        contexts.add("$keyword in messages")
        
        return contexts
    }
    
    /**
     * Generate gaming context suggestions
     */
    private fun generateGamingContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        contexts.add("$keyword in multiplayer")
        contexts.add("$keyword in chat")
        contexts.add("$keyword in voice")
        contexts.add("online $keyword")
        contexts.add("competitive $keyword")
        
        return contexts
    }
    
    /**
     * Generate social media context suggestions
     */
    private fun generateSocialContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        contexts.add("$keyword in posts")
        contexts.add("$keyword in comments")
        contexts.add("$keyword in messages")
        contexts.add("$keyword in stories")
        contexts.add("$keyword sharing")
        
        return contexts
    }
    
    /**
     * Generate general context suggestions
     */
    private fun generateGeneralContext(keyword: String): Set<String> {
        val contexts = mutableSetOf<String>()
        
        contexts.add("$keyword content")
        contexts.add("$keyword material")
        contexts.add("$keyword related")
        contexts.add("$keyword activity")
        contexts.add("inappropriate $keyword")
        
        return contexts
    }
}

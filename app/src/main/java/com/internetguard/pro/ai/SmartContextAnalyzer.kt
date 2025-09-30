package com.internetguard.pro.ai

import android.util.Log

/**
 * Smart Context Analyzer
 * 
 * Features:
 * - Educational vs inappropriate context detection
 * - Sentence structure analysis
 * - Intent analysis (informational vs explicit)
 * - False positive reduction
 * - Context-aware confidence scoring
 */
class SmartContextAnalyzer {
    
    companion object {
        private const val TAG = "SmartContextAnalyzer"
    }
    
    // Educational/Medical context indicators
    private val educationalKeywords = setOf(
        // English
        "education", "educational", "medical", "health", "healthcare", "science", 
        "scientific", "research", "study", "academic", "university", "school",
        "learning", "teaching", "textbook", "curriculum", "course", "lesson",
        "therapy", "treatment", "diagnosis", "clinical", "patient", "doctor",
        
        // Persian
        "آموزش", "آموزشی", "تحصیلی", "علمی", "پژوهش", "تحقیق", "دانشگاه", 
        "مدرسه", "درس", "کتاب درسی", "پزشکی", "سلامت", "درمان", "بهداشت",
        "دکتر", "پزشک", "بیمار", "تشخیص", "مطالعه", "یادگیری",
        
        // Arabic
        "تعليم", "تعليمي", "طبي", "صحة", "علمي", "بحث", "دراسة", "جامعة",
        "مدرسة", "درس", "طبيب", "مريض", "علاج", "تشخيص"
    )
    
    // Inappropriate context indicators
    private val inappropriateKeywords = setOf(
        // English
        "watch", "download", "free", "click here", "hot", "sexy", "streaming",
        "live", "cam", "chat", "meet", "hookup", "dating", "tonight", "now",
        "gallery", "photos", "videos", "pics", "images", "collection",
        
        // Persian
        "تماشا", "دانلود", "رایگان", "کلیک کنید", "داغ", "جذاب", "زنده",
        "چت", "ملاقات", "امشب", "الان", "گالری", "عکس", "ویدیو", "فیلم",
        "مجموعه", "کلکسیون", "آنلاین", "پخش زنده",
        
        // Arabic
        "مشاهدة", "تحميل", "مجاني", "اضغط هنا", "ساخن", "جذاب", "مباشر",
        "دردشة", "لقاء", "الليلة", "الآن", "معرض", "صور", "فيديو"
    )
    
    // Professional/Clinical context phrases
    private val professionalPhrases = setOf(
        "sexual health", "reproductive health", "sex education", "human sexuality",
        "sexual dysfunction", "sexual therapy", "sexual development", "sexual behavior",
        "adult development", "mature behavior", "adult psychology", "behavioral therapy",
        
        "سلامت جنسی", "آموزش جنسی", "رشد جنسی", "رفتار جنسی", "روانشناسی بالغین",
        "توسعه بالغین", "درمان رفتاری", "بهداشت باروری", "سلامت زنان",
        
        "الصحة الجنسية", "التربية الجنسية", "النمو الجنسي", "السلوك الجنسي",
        "علم النفس للبالغين", "التطوير للبالغين", "العلاج السلوكي"
    )
    
    /**
     * Analyze context of detected content
     */
    fun analyze(
        text: String,
        patternResult: DetectionResult,
        languageResult: DetectionResult
    ): DetectionResult {
        
        if (!patternResult.isInappropriate && !languageResult.isInappropriate) {
            return DetectionResult.safe("No inappropriate patterns detected")
        }
        
        val contextAnalysis = performContextAnalysis(text)
        val adjustedConfidence = adjustConfidenceBasedOnContext(
            maxOf(patternResult.confidence, languageResult.confidence),
            contextAnalysis
        )
        
        return DetectionResult(
            isInappropriate = adjustedConfidence > 0.7f,
            confidence = adjustedConfidence,
            category = determineCategoryWithContext(
                patternResult.category,
                languageResult.category,
                contextAnalysis
            ),
            reasoning = buildContextReasoning(contextAnalysis, patternResult, languageResult),
            triggeredPatterns = (patternResult.triggeredPatterns + languageResult.triggeredPatterns).distinct(),
            language = languageResult.language
        )
    }
    
    /**
     * Perform comprehensive context analysis
     */
    private fun performContextAnalysis(text: String): ContextAnalysis {
        val cleanText = text.lowercase()
        
        return ContextAnalysis(
            isEducational = analyzeEducationalContext(cleanText),
            isProfessional = analyzeProfessionalContext(cleanText),
            isInformational = analyzeInformationalContext(cleanText),
            isExplicit = analyzeExplicitContext(cleanText),
            sentenceStructure = analyzeSentenceStructure(cleanText),
            intentScore = analyzeIntent(cleanText),
            contextLength = text.length,
            wordCount = cleanText.split(Regex("\\s+")).size
        )
    }
    
    /**
     * Analyze educational context
     */
    private fun analyzeEducationalContext(text: String): Boolean {
        val educationalScore = educationalKeywords.count { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
        
        // Check for educational phrases
        val educationalPhrases = professionalPhrases.count { phrase ->
            text.contains(phrase, ignoreCase = true)
        }
        
        // Educational indicators
        val hasEducationalStructure = text.contains(Regex("(definition|meaning|explanation|study|research)"))
        val hasAcademicLanguage = text.contains(Regex("(according to|studies show|research indicates)"))
        
        return educationalScore >= 2 || educationalPhrases >= 1 || hasEducationalStructure || hasAcademicLanguage
    }
    
    /**
     * Analyze professional/clinical context
     */
    private fun analyzeProfessionalContext(text: String): Boolean {
        val professionalScore = professionalPhrases.count { phrase ->
            text.contains(phrase, ignoreCase = true)
        }
        
        val clinicalTerms = listOf(
            "diagnosis", "treatment", "therapy", "clinical", "patient", "medical",
            "تشخیص", "درمان", "پزشکی", "بالینی", "بیمار", "پزشک",
            "تشخيص", "علاج", "طبي", "سريري", "مريض", "طبيب"
        )
        
        val clinicalScore = clinicalTerms.count { term ->
            text.contains(term, ignoreCase = true)
        }
        
        return professionalScore >= 1 || clinicalScore >= 2
    }
    
    /**
     * Analyze informational context
     */
    private fun analyzeInformationalContext(text: String): Boolean {
        val informationalIndicators = listOf(
            "what is", "how to", "information about", "facts about", "guide to",
            "چیست", "چگونه", "اطلاعات درباره", "راهنمای", "حقایق درباره",
            "ما هو", "كيفية", "معلومات عن", "دليل", "حقائق عن"
        )
        
        return informationalIndicators.any { indicator ->
            text.contains(indicator, ignoreCase = true)
        }
    }
    
    /**
     * Analyze explicit context
     */
    private fun analyzeExplicitContext(text: String): Boolean {
        val explicitScore = inappropriateKeywords.count { keyword ->
            text.contains(keyword, ignoreCase = true)
        }
        
        val explicitPatterns = listOf(
            Regex("(watch|download|stream).*(free|now|live)", RegexOption.IGNORE_CASE),
            Regex("(hot|sexy|adult).*(photos|videos|pics)", RegexOption.IGNORE_CASE),
            Regex("(click here|visit now).*(adult|mature)", RegexOption.IGNORE_CASE)
        )
        
        val patternMatches = explicitPatterns.count { pattern ->
            pattern.containsMatchIn(text)
        }
        
        return explicitScore >= 3 || patternMatches >= 1
    }
    
    /**
     * Analyze sentence structure
     */
    private fun analyzeSentenceStructure(text: String): SentenceStructure {
        val sentences = text.split(Regex("[.!?]+")).filter { it.trim().isNotEmpty() }
        val avgSentenceLength = if (sentences.isNotEmpty()) {
            sentences.map { it.split(Regex("\\s+")).size }.average()
        } else 0.0
        
        val hasQuestions = text.contains(Regex("[?؟]"))
        val hasExclamations = text.contains(Regex("[!]"))
        val hasImperatives = text.contains(Regex("\\b(click|watch|download|visit|see)\\b", RegexOption.IGNORE_CASE))
        
        return SentenceStructure(
            sentenceCount = sentences.size,
            averageLength = avgSentenceLength,
            hasQuestions = hasQuestions,
            hasExclamations = hasExclamations,
            hasImperatives = hasImperatives
        )
    }
    
    /**
     * Analyze intent of the text
     */
    private fun analyzeIntent(text: String): Double {
        var intentScore = 0.0
        
        // Educational intent indicators (+)
        if (text.contains(Regex("(learn|understand|know|education)", RegexOption.IGNORE_CASE))) {
            intentScore += 0.3
        }
        
        // Commercial intent indicators (-)
        if (text.contains(Regex("(buy|purchase|order|sale)", RegexOption.IGNORE_CASE))) {
            intentScore -= 0.2
        }
        
        // Explicit intent indicators (--)
        if (text.contains(Regex("(watch now|download free|click here)", RegexOption.IGNORE_CASE))) {
            intentScore -= 0.5
        }
        
        // Professional intent indicators (+)
        if (text.contains(Regex("(therapy|treatment|medical|clinical)", RegexOption.IGNORE_CASE))) {
            intentScore += 0.4
        }
        
        return intentScore.coerceIn(-1.0, 1.0)
    }
    
    /**
     * Adjust confidence based on context analysis
     */
    private fun adjustConfidenceBasedOnContext(
        originalConfidence: Float,
        context: ContextAnalysis
    ): Float {
        var adjustedConfidence = originalConfidence
        
        // Reduce confidence for educational context
        if (context.isEducational) {
            adjustedConfidence *= 0.3f
        }
        
        // Reduce confidence for professional context
        if (context.isProfessional) {
            adjustedConfidence *= 0.2f
        }
        
        // Reduce confidence for informational context
        if (context.isInformational) {
            adjustedConfidence *= 0.4f
        }
        
        // Increase confidence for explicit context
        if (context.isExplicit) {
            adjustedConfidence *= 1.3f
        }
        
        // Adjust based on intent
        when {
            context.intentScore > 0.3 -> adjustedConfidence *= 0.5f // Positive intent
            context.intentScore < -0.3 -> adjustedConfidence *= 1.2f // Negative intent
        }
        
        // Adjust based on text length and structure
        if (context.contextLength > 200 && context.wordCount > 30) {
            // Longer texts are more likely to be informational
            adjustedConfidence *= 0.7f
        }
        
        return adjustedConfidence.coerceIn(0f, 1f)
    }
    
    /**
     * Determine category with context consideration
     */
    private fun determineCategoryWithContext(
        patternCategory: String,
        languageCategory: String,
        context: ContextAnalysis
    ): String {
        return when {
            context.isEducational -> "educational_content"
            context.isProfessional -> "professional_content"
            context.isInformational -> "informational_content"
            context.isExplicit -> "explicit_content"
            else -> maxOf(patternCategory, languageCategory, compareBy { it.length })
        }
    }
    
    /**
     * Build reasoning based on context analysis
     */
    private fun buildContextReasoning(
        context: ContextAnalysis,
        patternResult: DetectionResult,
        languageResult: DetectionResult
    ): String {
        return buildString {
            append("Context analysis: ")
            
            if (context.isEducational) append("Educational context detected. ")
            if (context.isProfessional) append("Professional/clinical context. ")
            if (context.isInformational) append("Informational content. ")
            if (context.isExplicit) append("Explicit commercial context. ")
            
            append("Intent score: ${String.format("%.2f", context.intentScore)}. ")
            
            if (patternResult.triggeredPatterns.isNotEmpty()) {
                append("Patterns: ${patternResult.triggeredPatterns.joinToString(", ")}. ")
            }
            
            if (languageResult.triggeredPatterns.isNotEmpty()) {
                append("Language patterns: ${languageResult.triggeredPatterns.joinToString(", ")}.")
            }
        }
    }
}

/**
 * Context analysis result
 */
data class ContextAnalysis(
    val isEducational: Boolean,
    val isProfessional: Boolean,
    val isInformational: Boolean,
    val isExplicit: Boolean,
    val sentenceStructure: SentenceStructure,
    val intentScore: Double,
    val contextLength: Int,
    val wordCount: Int
)

/**
 * Sentence structure analysis
 */
data class SentenceStructure(
    val sentenceCount: Int,
    val averageLength: Double,
    val hasQuestions: Boolean,
    val hasExclamations: Boolean,
    val hasImperatives: Boolean
)

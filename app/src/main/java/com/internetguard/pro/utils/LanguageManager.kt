package com.internetguard.pro.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

/**
 * Language Manager for handling multi-language support.
 * 
 * Manages language switching, locale detection, and RTL support
 * for the InternetGuard Pro application.
 */
object LanguageManager {
    
    // Supported languages
    private val supportedLanguages = mapOf(
        "en" to "English",
        "fa" to "Persian",
        "ar" to "Arabic",
        "zh" to "Chinese",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "ru" to "Russian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "hi" to "Hindi",
        "pt" to "Portuguese"
    )
    
    // RTL languages
    private val rtlLanguages = setOf("ar", "fa", "he", "ur")
    
    /**
     * Gets all supported languages.
     * 
     * @return Map of language codes to display names
     */
    fun getSupportedLanguages(): Map<String, String> = supportedLanguages
    
    /**
     * Gets the display name for a language code.
     * 
     * @param languageCode The language code (e.g., "en", "fa")
     * @return The display name of the language
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return supportedLanguages[languageCode] ?: languageCode
    }
    
    /**
     * Checks if a language is supported.
     * 
     * @param languageCode The language code to check
     * @return True if the language is supported
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return supportedLanguages.containsKey(languageCode)
    }
    
    /**
     * Checks if a language is RTL (Right-to-Left).
     * 
     * @param languageCode The language code to check
     * @return True if the language is RTL
     */
    fun isRTLanguage(languageCode: String): Boolean {
        return rtlLanguages.contains(languageCode)
    }
    
    /**
     * Gets the current system language.
     * 
     * @param context The application context
     * @return The current system language code
     */
    fun getCurrentSystemLanguage(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return locale.language
    }
    
    /**
     * Gets the best matching language for the current system.
     * 
     * @param context The application context
     * @return The best matching language code
     */
    fun getBestMatchingLanguage(context: Context): String {
        val systemLanguage = getCurrentSystemLanguage(context)
        return if (isLanguageSupported(systemLanguage)) {
            systemLanguage
        } else {
            "en" // Default to English
        }
    }
    
    /**
     * Applies language to the context.
     * 
     * @param context The application context
     * @param languageCode The language code to apply
     */
    fun applyLanguage(context: Context, languageCode: String) {
        if (!isLanguageSupported(languageCode)) {
            return
        }
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    /**
     * Creates a new context with the specified language.
     * This is useful for Activities that need to attach a context with a specific language.
     * 
     * @param baseContext The base context
     * @param languageCode The language code to apply
     * @return A new context with the specified language
     */
    fun createContextWithLanguage(baseContext: Context, languageCode: String): Context {
        if (!isLanguageSupported(languageCode)) {
            return baseContext
        }
        
        val locale = Locale(languageCode)
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        
        return baseContext.createConfigurationContext(config)
    }
    
    /**
     * Gets the language direction for a given language.
     * 
     * @param languageCode The language code
     * @return The language direction (LTR or RTL)
     */
    fun getLanguageDirection(languageCode: String): String {
        return if (isRTLanguage(languageCode)) "rtl" else "ltr"
    }
    
    /**
     * Gets the text alignment for a given language.
     * 
     * @param languageCode The language code
     * @return The text alignment (start, end, left, right)
     */
    fun getTextAlignment(languageCode: String): String {
        return if (isRTLanguage(languageCode)) "end" else "start"
    }
    
    /**
     * Gets the gravity for a given language.
     * 
     * @param languageCode The language code
     * @return The gravity value
     */
    fun getGravity(languageCode: String): Int {
        return if (isRTLanguage(languageCode)) {
            android.view.Gravity.END
        } else {
            android.view.Gravity.START
        }
    }
    
    /**
     * Formats a number according to the language locale.
     * 
     * @param number The number to format
     * @param languageCode The language code
     * @return The formatted number string
     */
    fun formatNumber(number: Number, languageCode: String): String {
        val locale = Locale(languageCode)
        return when (number) {
            is Int -> String.format(locale, "%d", number)
            is Long -> String.format(locale, "%d", number)
            is Float -> String.format(locale, "%.2f", number)
            is Double -> String.format(locale, "%.2f", number)
            else -> number.toString()
        }
    }
    
    /**
     * Formats a date according to the language locale.
     * 
     * @param date The date to format
     * @param languageCode The language code
     * @return The formatted date string
     */
    fun formatDate(date: Date, languageCode: String): String {
        val locale = Locale(languageCode)
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", locale)
        return formatter.format(date)
    }
    
    /**
     * Formats a time according to the language locale.
     * 
     * @param time The time to format
     * @param languageCode The language code
     * @return The formatted time string
     */
    fun formatTime(time: Date, languageCode: String): String {
        val locale = Locale(languageCode)
        val formatter = java.text.SimpleDateFormat("HH:mm", locale)
        return formatter.format(time)
    }
    
    /**
     * Gets the language-specific keyword patterns for filtering.
     * 
     * @param languageCode The language code
     * @return List of common inappropriate keywords for the language
     */
    fun getLanguageSpecificKeywords(languageCode: String): List<String> {
        return when (languageCode) {
            "en" -> listOf("violence", "gambling", "adult", "hate", "drugs")
            "fa" -> listOf("خشونت", "قمار", "بزرگسال", "نفرت", "مواد")
            "ar" -> listOf("عنف", "مقامرة", "بالغين", "كراهية", "مخدرات")
            "zh" -> listOf("暴力", "赌博", "成人", "仇恨", "毒品")
            "es" -> listOf("violencia", "apuestas", "adulto", "odio", "drogas")
            "fr" -> listOf("violence", "jeu", "adulte", "haine", "drogues")
            "de" -> listOf("gewalt", "glücksspiel", "erwachsene", "hass", "drogen")
            "ru" -> listOf("насилие", "азартные игры", "взрослые", "ненависть", "наркотики")
            "ja" -> listOf("暴力", "ギャンブル", "成人", "憎悪", "薬物")
            "ko" -> listOf("폭력", "도박", "성인", "증오", "마약")
            "hi" -> listOf("हिंसा", "जुआ", "वयस्क", "घृणा", "ड्रग्स")
            "pt" -> listOf("violência", "jogos de azar", "adulto", "ódio", "drogas")
            else -> emptyList()
        }
    }
    
    /**
     * Gets the language-specific categories for organizing keywords.
     * 
     * @param languageCode The language code
     * @return Map of category codes to display names
     */
    fun getLanguageSpecificCategories(languageCode: String): Map<String, String> {
        return when (languageCode) {
            "en" -> mapOf(
                "content" to "Content",
                "gambling" to "Gambling",
                "violence" to "Violence",
                "adult" to "Adult",
                "hate_speech" to "Hate Speech",
                "custom" to "Custom"
            )
            "fa" -> mapOf(
                "content" to "محتوا",
                "gambling" to "قمار",
                "violence" to "خشونت",
                "adult" to "بزرگسال",
                "hate_speech" to "سخن نفرت",
                "custom" to "سفارشی"
            )
            "ar" -> mapOf(
                "content" to "محتوى",
                "gambling" to "مقامرة",
                "violence" to "عنف",
                "adult" to "بالغين",
                "hate_speech" to "خطاب كراهية",
                "custom" to "مخصص"
            )
            "zh" -> mapOf(
                "content" to "内容",
                "gambling" to "赌博",
                "violence" to "暴力",
                "adult" to "成人",
                "hate_speech" to "仇恨言论",
                "custom" to "自定义"
            )
            "es" -> mapOf(
                "content" to "Contenido",
                "gambling" to "Apuestas",
                "violence" to "Violencia",
                "adult" to "Adulto",
                "hate_speech" to "Discurso de Odio",
                "custom" to "Personalizado"
            )
            "fr" -> mapOf(
                "content" to "Contenu",
                "gambling" to "Jeu d'argent",
                "violence" to "Violence",
                "adult" to "Adulte",
                "hate_speech" to "Discours de haine",
                "custom" to "Personnalisé"
            )
            "de" -> mapOf(
                "content" to "Inhalt",
                "gambling" to "Glücksspiel",
                "violence" to "Gewalt",
                "adult" to "Erwachsene",
                "hate_speech" to "Hassrede",
                "custom" to "Benutzerdefiniert"
            )
            "ru" -> mapOf(
                "content" to "Контент",
                "gambling" to "Азартные игры",
                "violence" to "Насилие",
                "adult" to "Взрослые",
                "hate_speech" to "Речь ненависти",
                "custom" to "Пользовательский"
            )
            "ja" -> mapOf(
                "content" to "コンテンツ",
                "gambling" to "ギャンブル",
                "violence" to "暴力",
                "adult" to "成人向け",
                "hate_speech" to "ヘイトスピーチ",
                "custom" to "カスタム"
            )
            "ko" -> mapOf(
                "content" to "콘텐츠",
                "gambling" to "도박",
                "violence" to "폭력",
                "adult" to "성인",
                "hate_speech" to "혐오 발언",
                "custom" to "사용자 정의"
            )
            "hi" -> mapOf(
                "content" to "सामग्री",
                "gambling" to "जुआ",
                "violence" to "हिंसा",
                "adult" to "वयस्क",
                "hate_speech" to "घृणा भाषण",
                "custom" to "कस्टम"
            )
            "pt" -> mapOf(
                "content" to "Conteúdo",
                "gambling" to "Jogos de Azar",
                "violence" to "Violência",
                "adult" to "Adulto",
                "hate_speech" to "Discurso de Ódio",
                "custom" to "Personalizado"
            )
            else -> emptyMap()
        }
    }
}

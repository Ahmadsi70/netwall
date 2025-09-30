package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.utils.LanguageManager
import kotlinx.coroutines.launch

/**
 * ViewModel for language settings fragment.
 * 
 * Manages language selection, application, and system language detection.
 * Handles language-specific features and RTL support.
 */
class LanguageSettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    
    // LiveData for supported languages
    private val _supportedLanguages = MutableLiveData<List<LanguageItem>>()
    val supportedLanguages: LiveData<List<LanguageItem>> = _supportedLanguages
    
    // LiveData for selected language
    private val _selectedLanguage = MutableLiveData<String>()
    val selectedLanguage: LiveData<String> = _selectedLanguage
    
    // LiveData for current language
    private val _currentLanguage = MutableLiveData<String>()
    val currentLanguage: LiveData<String> = _currentLanguage
    
    // LiveData for RTL status
    private val _isRTL = MutableLiveData<Boolean>()
    val isRTL: LiveData<Boolean> = _isRTL
    
    // LiveData for language change status
    private val _languageChanged = MutableLiveData<Boolean>()
    val languageChanged: LiveData<Boolean> = _languageChanged
    
    init {
        loadSupportedLanguages()
        loadCurrentLanguage()
    }
    
    /**
     * Loads all supported languages
     */
    private fun loadSupportedLanguages() {
        val languages = LanguageManager.getSupportedLanguages().map { (code, name) ->
            LanguageItem(
                code = code,
                name = name,
                isRTL = LanguageManager.isRTLanguage(code),
                isSelected = false
            )
        }
        _supportedLanguages.value = languages
    }
    
    /**
     * Loads current system language
     */
    private fun loadCurrentLanguage() {
        val systemLanguage = LanguageManager.getCurrentSystemLanguage(context)
        val bestMatch = LanguageManager.getBestMatchingLanguage(context)
        
        _currentLanguage.value = bestMatch
        _isRTL.value = LanguageManager.isRTLanguage(bestMatch)
        _selectedLanguage.value = bestMatch
    }
    
    /**
     * Selects a language
     */
    fun selectLanguage(languageCode: String) {
        if (LanguageManager.isLanguageSupported(languageCode)) {
            _selectedLanguage.value = languageCode
            _isRTL.value = LanguageManager.isRTLanguage(languageCode)
            
            // Update language list selection
            updateLanguageSelection(languageCode)
        }
    }
    
    /**
     * Updates language selection in the list
     */
    private fun updateLanguageSelection(selectedCode: String) {
        val currentList = _supportedLanguages.value ?: return
        val updatedList = currentList.map { language ->
            language.copy(isSelected = language.code == selectedCode)
        }
        _supportedLanguages.value = updatedList
    }
    
    /**
     * Applies the selected language
     */
    fun applyLanguage() {
        val selectedLang = _selectedLanguage.value ?: return
        
        viewModelScope.launch {
            try {
                // Apply language to context
                LanguageManager.applyLanguage(context, selectedLang)
                
                // Update current language
                _currentLanguage.value = selectedLang
                _isRTL.value = LanguageManager.isRTLanguage(selectedLang)
                
                // Notify language change
                _languageChanged.value = true
                
                // Reset notification
                _languageChanged.value = false
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Resets to system language
     */
    fun resetToSystemLanguage() {
        val systemLanguage = LanguageManager.getBestMatchingLanguage(context)
        selectLanguage(systemLanguage)
    }
    
    /**
     * Gets language-specific keywords for the selected language
     */
    fun getLanguageSpecificKeywords(): List<String> {
        val selectedLang = _selectedLanguage.value ?: "en"
        return LanguageManager.getLanguageSpecificKeywords(selectedLang)
    }
    
    /**
     * Gets language-specific categories for the selected language
     */
    fun getLanguageSpecificCategories(): Map<String, String> {
        val selectedLang = _selectedLanguage.value ?: "en"
        return LanguageManager.getLanguageSpecificCategories(selectedLang)
    }
    
    /**
     * Checks if a language change requires app restart
     */
    fun requiresRestart(): Boolean {
        val currentLang = _currentLanguage.value ?: "en"
        val selectedLang = _selectedLanguage.value ?: "en"
        return currentLang != selectedLang
    }
    
    /**
     * Gets the language direction for the selected language
     */
    fun getLanguageDirection(): String {
        val selectedLang = _selectedLanguage.value ?: "en"
        return LanguageManager.getLanguageDirection(selectedLang)
    }
    
    /**
     * Gets the text alignment for the selected language
     */
    fun getTextAlignment(): String {
        val selectedLang = _selectedLanguage.value ?: "en"
        return LanguageManager.getTextAlignment(selectedLang)
    }
    
    /**
     * Data class for language items
     */
    data class LanguageItem(
        val code: String,
        val name: String,
        val isRTL: Boolean,
        val isSelected: Boolean
    )
}

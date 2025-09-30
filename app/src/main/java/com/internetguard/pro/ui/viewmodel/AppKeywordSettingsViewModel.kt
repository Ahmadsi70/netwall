package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.entities.AppKeywordRules
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.data.model.AppKeywordSettings
import com.internetguard.pro.data.repository.AppRepository
import com.internetguard.pro.data.repository.KeywordRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app-specific keyword blocking settings.
 * 
 * Handles the logic for enabling/disabling keyword filtering for specific apps
 * and managing which keywords apply to which apps.
 */
class AppKeywordSettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val appRepository: AppRepository
    private val keywordRepository: KeywordRepository
    private val database = (application as InternetGuardProApp).database
    
    // LiveData for app list with keyword settings
    private val _appList = MutableLiveData<List<AppKeywordSettings>>()
    val appList: LiveData<List<AppKeywordSettings>> = _appList
    
    // LiveData for available keywords
    private val _availableKeywords = MutableLiveData<List<KeywordBlacklist>>()
    val availableKeywords: LiveData<List<KeywordBlacklist>> = _availableKeywords
    
    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        appRepository = AppRepository(database.appBlockRulesDao())
        keywordRepository = KeywordRepository(database.keywordBlacklistDao())
        loadData()
    }
    
    /**
     * Loads app list and keyword settings
     */
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadAppsWithKeywordSettings()
                loadAvailableKeywords()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Loads apps with their keyword filtering status
     */
    private suspend fun loadAppsWithKeywordSettings() {
        try {
            val installedApps = appRepository.getInstalledApps()
            val appsWithRules = database.appKeywordRulesDao().getAppsWithKeywordRules()
            
            val appKeywordSettings = installedApps.map { appInfo ->
                val hasKeywordFiltering = appsWithRules.contains(appInfo.packageName)
                val activeRules = if (hasKeywordFiltering) {
                    database.appKeywordRulesDao().getActiveRulesForApp(appInfo.packageName)
                } else emptyList()
                val activeRulesCount = activeRules.size

                // Preview: نام 3 کلمه اول
                val preview = if (activeRules.isNotEmpty()) {
                    activeRules.take(3).mapNotNull { rule ->
                        keywordRepository.getKeywordById(rule.keywordId)?.keyword
                    }
                } else emptyList()
                
                AppKeywordSettings(
                    appInfo = appInfo,
                    hasKeywordFiltering = hasKeywordFiltering,
                    activeKeywordsCount = activeRulesCount,
                    previewKeywords = preview
                )
            }
            
            _appList.value = appKeywordSettings
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load apps: ${e.message}"
        }
    }
    
    /**
     * Loads available keywords
     */
    private suspend fun loadAvailableKeywords() {
        try {
            keywordRepository.getAllKeywords().collect { keywords ->
                _availableKeywords.value = keywords
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load keywords: ${e.message}"
        }
    }
    
    /**
     * Toggles keyword filtering for a specific app
     */
    fun toggleAppKeywordFiltering(packageName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                if (isEnabled) {
                    // Enable keyword filtering by adding all keywords to this app
                    val keywords = _availableKeywords.value ?: emptyList()
                    keywords.forEach { keyword ->
                        val rule = AppKeywordRules(
                            appPackageName = packageName,
                            keywordId = keyword.id,
                            isEnabled = true
                        )
                        database.appKeywordRulesDao().insert(rule)
                    }
                } else {
                    // Disable keyword filtering by removing all rules for this app
                    database.appKeywordRulesDao().deleteAllRulesForApp(packageName)
                }
                
                // Refresh the app list
                loadAppsWithKeywordSettings()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update app settings: ${e.message}"
            }
        }
    }
    
    /**
     * Updates keyword rules for a specific app
     */
    fun updateAppKeywordRules(packageName: String, selectedKeywords: List<String>) {
        viewModelScope.launch {
            try {
                // Remove existing rules for this app
                database.appKeywordRulesDao().deleteAllRulesForApp(packageName)
                
                // Add new keywords and rules
                selectedKeywords.forEach { keywordText ->
                    // First, add the keyword to the blacklist if it doesn't exist
                    val existingKeyword = keywordRepository.getKeywordByText(keywordText)
                    val keywordId = if (existingKeyword != null) {
                        existingKeyword.id
                    } else {
                        val newKeyword = KeywordBlacklist(
                            keyword = keywordText,
                            category = "Custom",
                            caseSensitive = false,
                            language = "en",
                            createdAt = System.currentTimeMillis()
                        )
                        keywordRepository.addKeyword(newKeyword)
                        newKeyword.id
                    }
                    
                    // Create rule for this app
                    val rule = AppKeywordRules(
                        appPackageName = packageName,
                        keywordId = keywordId,
                        isEnabled = true
                    )
                    database.appKeywordRulesDao().insert(rule)
                }
                
                // Refresh the app list
                loadAppsWithKeywordSettings()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update keyword rules: ${e.message}"
            }
        }
    }
    
    /**
     * Enables keyword filtering for all apps
     */
    fun enableKeywordFilteringForAllApps() {
        viewModelScope.launch {
            try {
                val apps = _appList.value ?: emptyList()
                val keywords = _availableKeywords.value ?: emptyList()
                
                apps.forEach { appSetting ->
                    if (!appSetting.hasKeywordFiltering) {
                        keywords.forEach { keyword ->
                            val rule = AppKeywordRules(
                                appPackageName = appSetting.appInfo.packageName,
                                keywordId = keyword.id,
                                isEnabled = true
                            )
                            database.appKeywordRulesDao().insert(rule)
                        }
                    }
                }
                
                loadAppsWithKeywordSettings()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to enable filtering for all apps: ${e.message}"
            }
        }
    }
    
    /**
     * Disables keyword filtering for all apps
     */
    fun disableKeywordFilteringForAllApps() {
        viewModelScope.launch {
            try {
                val apps = _appList.value ?: emptyList()
                
                apps.forEach { appSetting ->
                    if (appSetting.hasKeywordFiltering) {
                        database.appKeywordRulesDao().deleteAllRulesForApp(appSetting.appInfo.packageName)
                    }
                }
                
                loadAppsWithKeywordSettings()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to disable filtering for all apps: ${e.message}"
            }
        }
    }
    
    /**
     * Gets active keywords for a specific app
     */
    fun getActiveKeywordsForApp(packageName: String): LiveData<List<KeywordBlacklist>> {
        val result = MutableLiveData<List<KeywordBlacklist>>()
        
        viewModelScope.launch {
            try {
                val rules = database.appKeywordRulesDao().getActiveRulesForApp(packageName)
                val keywords = mutableListOf<KeywordBlacklist>()
                
                rules.forEach { rule ->
                    val keyword = keywordRepository.getKeywordById(rule.keywordId)
                    keyword?.let { keywords.add(it) }
                }
                
                result.value = keywords
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load keywords for app: ${e.message}"
            }
        }
        
        return result
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Refreshes all data
     */
    fun refresh() {
        loadData()
    }
}

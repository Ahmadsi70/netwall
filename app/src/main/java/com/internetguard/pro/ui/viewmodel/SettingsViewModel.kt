package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import kotlinx.coroutines.launch

/**
 * ViewModel for settings fragment.
 * 
 * Manages app settings including theme, language,
 * notifications, and data management.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
	
	private val database by lazy { (application as InternetGuardProApp).database }
	private val prefs = application.getSharedPreferences("app_settings", 0)
	
	// LiveData for settings
	private val _isDarkTheme = MutableLiveData<Boolean>()
	val isDarkTheme: LiveData<Boolean> = _isDarkTheme
	
	private val _isNotificationsEnabled = MutableLiveData<Boolean>()
	val isNotificationsEnabled: LiveData<Boolean> = _isNotificationsEnabled
	
	private val _currentLanguage = MutableLiveData<String>()
	val currentLanguage: LiveData<String> = _currentLanguage
	
	private val _exportSuccess = MutableLiveData<Boolean>()
	val exportSuccess: LiveData<Boolean> = _exportSuccess
	
	private val _importSuccess = MutableLiveData<Boolean>()
	val importSuccess: LiveData<Boolean> = _importSuccess
	
	init {
		loadSettings()
	}
	
	/**
	 * Loads current settings
	 */
	private fun loadSettings() {
		_isDarkTheme.value = prefs.getBoolean("dark_theme", false)
		_isNotificationsEnabled.value = prefs.getBoolean("notifications_enabled", true)
		_currentLanguage.value = prefs.getString("language", "English") ?: "English"
	}
	
	/**
	 * Sets theme preference
	 */
	fun setTheme(themeIndex: Int) {
		val isDark = when (themeIndex) {
			0 -> false // Light
			1 -> true  // Dark
			else -> false // System (default to light)
		}
		
		prefs.edit().putBoolean("dark_theme", isDark).apply()
		_isDarkTheme.value = isDark
	}
	
	/**
	 * Gets current theme index
	 */
	fun getCurrentTheme(): Int {
		return if (_isDarkTheme.value == true) 1 else 0
	}
	
	/**
	 * Sets language preference
	 */
	fun setLanguage(languageIndex: Int) {
		val language = when (languageIndex) {
			0 -> "English"
			1 -> "Persian"
			else -> "English"
		}
		
		prefs.edit().putString("language", language).apply()
		_currentLanguage.value = language
	}
	
	/**
	 * Gets current language index
	 */
	fun getCurrentLanguage(): Int {
		return when (_currentLanguage.value) {
			"English" -> 0
			"Persian" -> 1
			else -> 0
		}
	}
	
	/**
	 * Sets notifications enabled
	 */
	fun setNotificationsEnabled(enabled: Boolean) {
		prefs.edit().putBoolean("notifications_enabled", enabled).apply()
		_isNotificationsEnabled.value = enabled
	}
	
	/**
	 * Exports app data
	 */
	fun exportData() {
		viewModelScope.launch {
			try {
				// Implementation would export data to file
				// For now, simulate success
				_exportSuccess.value = true
			} catch (e: Exception) {
				_exportSuccess.value = false
			}
		}
	}
	
	/**
	 * Imports app data
	 */
	fun importData() {
		viewModelScope.launch {
			try {
				// Implementation would import data from file
				// For now, simulate success
				_importSuccess.value = true
			} catch (e: Exception) {
				_importSuccess.value = false
			}
		}
	}
	
	/**
	 * Clears all app data
	 */
	fun clearAllData() {
		viewModelScope.launch {
			try {
				// Clear all database tables
				database.clearAllTables()
				
				// Clear preferences
				prefs.edit().clear().apply()
				
				// Reload settings
				loadSettings()
			} catch (e: Exception) {
				// Handle error
			}
		}
	}
}

package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.entities.KeywordLogs
import com.internetguard.pro.data.repository.KeywordLogsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing keyword blocking logs.
 * 
 * Handles loading and displaying logs of blocked content attempts.
 */
class KeywordLogsViewModel(application: Application) : AndroidViewModel(application) {
	
	private val repository: KeywordLogsRepository
	
	// LiveData for logs list
	private val _logsList = MutableLiveData<List<KeywordLogs>>()
	val logsList: LiveData<List<KeywordLogs>> = _logsList
	
	// LiveData for error messages
	private val _errorMessage = MutableLiveData<String?>()
	val errorMessage: LiveData<String?> = _errorMessage
	
	init {
		val database = (application as InternetGuardProApp).database
		repository = KeywordLogsRepository(database.keywordLogsDao())
		loadLogs()
	}
	
	/**
	 * Loads all logs from database
	 */
	private fun loadLogs() {
		viewModelScope.launch {
			try {
				val logs = repository.getLatestLogs(100) // Get latest 100 logs
				_logsList.value = logs
			} catch (e: Exception) {
				_errorMessage.value = "Failed to load logs: ${e.message}"
			}
		}
	}
	
	/**
	 * Clears all logs
	 */
	fun clearAllLogs() {
		viewModelScope.launch {
			try {
				repository.clearAllLogs()
				loadLogs() // Refresh the list
			} catch (e: Exception) {
				_errorMessage.value = "Failed to clear logs: ${e.message}"
			}
		}
	}
	
	/**
	 * Clears error message
	 */
	fun clearError() {
		_errorMessage.value = null
	}
}

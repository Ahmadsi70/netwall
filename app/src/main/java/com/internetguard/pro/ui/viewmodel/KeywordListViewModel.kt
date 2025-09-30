package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.repository.KeywordRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing keyword blacklist.
 * 
 * Handles CRUD operations for keywords and provides
 * LiveData for UI updates.
 */
class KeywordListViewModel(application: Application) : AndroidViewModel(application) {
	
	private val repository: KeywordRepository
	
	// LiveData for keyword list
	private val _keywordList = MutableLiveData<List<KeywordBlacklist>>()
	val keywordList: LiveData<List<KeywordBlacklist>> = _keywordList
	
	// LiveData for error messages
	private val _errorMessage = MutableLiveData<String?>()
	val errorMessage: LiveData<String?> = _errorMessage
	
	init {
		val database = (application as InternetGuardProApp).database
		repository = KeywordRepository(database.keywordBlacklistDao())
		loadKeywords()
	}
	
	/**
	 * Loads all keywords from database
	 */
	private fun loadKeywords() {
		viewModelScope.launch {
			try {
				repository.getAllKeywords().collect { keywords ->
					_keywordList.value = keywords
				}
			} catch (e: Exception) {
				_errorMessage.value = "Failed to load keywords: ${e.message}"
			}
		}
	}
	
	/**
	 * Adds a new keyword to blacklist
	 */
	fun addKeyword(keyword: KeywordBlacklist) {
		viewModelScope.launch {
			try {
				repository.addKeyword(keyword)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to add keyword: ${e.message}"
			}
		}
	}
	
	/**
	 * Updates an existing keyword
	 */
	fun updateKeyword(keyword: KeywordBlacklist) {
		viewModelScope.launch {
			try {
				repository.updateKeyword(keyword)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to update keyword: ${e.message}"
			}
		}
	}
	
	/**
	 * Deletes a keyword from blacklist
	 */
	fun deleteKeyword(keyword: KeywordBlacklist) {
		viewModelScope.launch {
			try {
				repository.deleteKeyword(keyword)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to delete keyword: ${e.message}"
			}
		}
	}
	
	/**
	 * Clears all keywords from blacklist
	 */
	fun clearAllKeywords() {
		viewModelScope.launch {
			try {
				repository.clearAllKeywords()
			} catch (e: Exception) {
				_errorMessage.value = "Failed to clear keywords: ${e.message}"
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

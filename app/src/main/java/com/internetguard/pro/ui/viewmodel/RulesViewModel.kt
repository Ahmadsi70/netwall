package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.entities.CustomRules
import com.internetguard.pro.data.repository.RulesRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing custom rules.
 * 
 * Handles CRUD operations for rules and provides
 * LiveData for UI updates.
 */
class RulesViewModel(application: Application) : AndroidViewModel(application) {
	
	private val repository: RulesRepository
	
	// LiveData for rules list
	private val _rulesList = MutableLiveData<List<CustomRules>>()
	val rulesList: LiveData<List<CustomRules>> = _rulesList
	
	// LiveData for error messages
	private val _errorMessage = MutableLiveData<String?>()
	val errorMessage: LiveData<String?> = _errorMessage
	
	init {
		val database = (application as InternetGuardProApp).database
		repository = RulesRepository(database.customRulesDao())
		loadRules()
	}
	
	/**
	 * Loads all rules from database
	 */
	private fun loadRules() {
		viewModelScope.launch {
			try {
				repository.getAllRules().collect { rules ->
					_rulesList.value = rules
				}
			} catch (e: Exception) {
				_errorMessage.value = "Failed to load rules: ${e.message}"
			}
		}
	}
	
	/**
	 * Adds a new rule
	 */
	fun addRule(rule: CustomRules) {
		viewModelScope.launch {
			try {
				repository.addRule(rule)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to add rule: ${e.message}"
			}
		}
	}
	
	/**
	 * Updates an existing rule
	 */
	fun updateRule(rule: CustomRules) {
		viewModelScope.launch {
			try {
				repository.updateRule(rule)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to update rule: ${e.message}"
			}
		}
	}
	
	/**
	 * Deletes a rule
	 */
	fun deleteRule(rule: CustomRules) {
		viewModelScope.launch {
			try {
				repository.deleteRule(rule)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to delete rule: ${e.message}"
			}
		}
	}
	
	/**
	 * Toggles rule enabled state
	 */
	fun toggleRule(rule: CustomRules, isEnabled: Boolean) {
		viewModelScope.launch {
			try {
				val updatedRule = rule.copy(isEnabled = isEnabled)
				repository.updateRule(updatedRule)
			} catch (e: Exception) {
				_errorMessage.value = "Failed to toggle rule: ${e.message}"
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

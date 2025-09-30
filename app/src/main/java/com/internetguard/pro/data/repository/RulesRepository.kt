package com.internetguard.pro.data.repository

import com.internetguard.pro.data.daos.CustomRulesDao
import com.internetguard.pro.data.entities.CustomRules
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing custom rules.
 * 
 * Provides a clean interface between UI and data layer,
 * handling all database operations for rules.
 */
class RulesRepository(
	private val customRulesDao: CustomRulesDao
) {
	
	/**
	 * Gets all rules as a Flow
	 */
	fun getAllRules(): Flow<List<CustomRules>> = customRulesDao.getAllRules()
	
	/**
	 * Gets active rules as a Flow
	 */
	fun getActiveRules(): Flow<List<CustomRules>> = customRulesDao.getActiveRules()
	
	/**
	 * Gets rules by type
	 */
	suspend fun getRulesByType(type: String): List<CustomRules> {
		return customRulesDao.getRulesByType(type)
	}
	
	/**
	 * Adds a new rule
	 */
	suspend fun addRule(rule: CustomRules): Long {
		return customRulesDao.insert(rule)
	}
	
	/**
	 * Updates an existing rule
	 */
	suspend fun updateRule(rule: CustomRules) {
		customRulesDao.update(rule)
	}
	
	/**
	 * Deletes a rule
	 */
	suspend fun deleteRule(rule: CustomRules) {
		customRulesDao.delete(rule)
	}
	
	/**
	 * Updates last triggered time
	 */
	suspend fun updateLastTriggered(id: Long, timestamp: Long) {
		customRulesDao.updateLastTriggered(id, timestamp)
	}
}

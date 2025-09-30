package com.internetguard.pro.data.repository

import com.internetguard.pro.data.daos.KeywordLogsDao
import com.internetguard.pro.data.entities.KeywordLogs

/**
 * Repository for managing keyword blocking logs.
 * 
 * Provides a clean interface between UI and data layer,
 * handling all database operations for logs.
 */
class KeywordLogsRepository(
	private val keywordLogsDao: KeywordLogsDao
) {
	
	/**
	 * Gets latest logs with limit
	 */
	suspend fun getLatestLogs(limit: Int): List<KeywordLogs> {
		return keywordLogsDao.latest(limit)
	}
	
	/**
	 * Gets logs for a specific keyword
	 */
	fun getLogsByKeyword(keywordId: Long) = keywordLogsDao.observeByKeyword(keywordId)
	
	/**
	 * Inserts a new log entry
	 */
	suspend fun insertLog(log: KeywordLogs): Long {
		return keywordLogsDao.insert(log)
	}
	
	/**
	 * Clears all logs
	 */
	suspend fun clearAllLogs() {
		keywordLogsDao.clear()
	}
}

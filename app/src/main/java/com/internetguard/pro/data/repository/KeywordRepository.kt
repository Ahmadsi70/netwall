package com.internetguard.pro.data.repository

import com.internetguard.pro.data.daos.KeywordBlacklistDao
import com.internetguard.pro.data.entities.KeywordBlacklist
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing keyword blacklist.
 * 
 * Provides a clean interface between UI and data layer,
 * handling all database operations for keywords.
 */
class KeywordRepository(
	private val keywordBlacklistDao: KeywordBlacklistDao
) {
	
	/**
	 * Gets all keywords as a Flow
	 */
	fun getAllKeywords(): Flow<List<KeywordBlacklist>> = keywordBlacklistDao.observeAll()
	
	/**
	 * Adds a new keyword to blacklist
	 */
	suspend fun addKeyword(keyword: KeywordBlacklist): Long {
		return keywordBlacklistDao.upsert(keyword)
	}
	
	/**
	 * Updates an existing keyword
	 */
	suspend fun updateKeyword(keyword: KeywordBlacklist) {
		keywordBlacklistDao.update(keyword)
	}
	
	/**
	 * Deletes a keyword from blacklist
	 */
	suspend fun deleteKeyword(keyword: KeywordBlacklist) {
		keywordBlacklistDao.delete(keyword)
	}
	
	/**
	 * Clears all keywords from blacklist
	 */
	suspend fun clearAllKeywords() {
		keywordBlacklistDao.clear()
	}
	
	/**
	 * Searches keywords by pattern
	 */
	suspend fun searchKeywords(pattern: String): List<KeywordBlacklist> {
		return keywordBlacklistDao.search("%$pattern%")
	}
	
	/**
	 * Gets a keyword by its ID
	 */
	suspend fun getKeywordById(id: Long): KeywordBlacklist? {
		return keywordBlacklistDao.getById(id)
	}
	
	/**
	 * Gets a keyword by its text
	 */
	suspend fun getKeywordByText(text: String): KeywordBlacklist? {
		return keywordBlacklistDao.getByText(text)
	}
}

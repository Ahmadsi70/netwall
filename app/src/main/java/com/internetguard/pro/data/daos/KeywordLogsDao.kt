package com.internetguard.pro.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.internetguard.pro.data.entities.KeywordLogs

/**
 * DAO for accessing [KeywordLogs].
 */
@Dao
interface KeywordLogsDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(log: KeywordLogs): Long

	@Query("SELECT * FROM keyword_logs WHERE keywordId = :keywordId ORDER BY attemptTime DESC")
	fun observeByKeyword(keywordId: Long): LiveData<List<KeywordLogs>>

	@Query("SELECT * FROM keyword_logs ORDER BY attemptTime DESC LIMIT :limit")
	suspend fun latest(limit: Int): List<KeywordLogs>

	@Query("DELETE FROM keyword_logs")
	suspend fun clear()
}

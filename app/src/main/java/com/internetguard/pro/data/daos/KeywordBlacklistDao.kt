package com.internetguard.pro.data.daos

import kotlinx.coroutines.flow.Flow
import androidx.room.*
import com.internetguard.pro.data.entities.KeywordBlacklist

/**
 * DAO for accessing [KeywordBlacklist].
 */
@Dao
interface KeywordBlacklistDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(item: KeywordBlacklist): Long

	@Update
	suspend fun update(item: KeywordBlacklist)

	@Delete
	suspend fun delete(item: KeywordBlacklist)

	@Query("SELECT * FROM keyword_blacklist ORDER BY createdAt DESC")
	fun observeAll(): Flow<List<KeywordBlacklist>>

	@Query("SELECT * FROM keyword_blacklist WHERE keyword LIKE :pattern")
	suspend fun search(pattern: String): List<KeywordBlacklist>

	@Query("SELECT * FROM keyword_blacklist WHERE id = :id")
	suspend fun getById(id: Long): KeywordBlacklist?

	@Query("SELECT * FROM keyword_blacklist WHERE keyword = :text LIMIT 1")
	suspend fun getByText(text: String): KeywordBlacklist?

	@Query("SELECT * FROM keyword_blacklist ORDER BY createdAt DESC")
	suspend fun getAll(): List<KeywordBlacklist>

	@Query("DELETE FROM keyword_blacklist")
	suspend fun clear()
}

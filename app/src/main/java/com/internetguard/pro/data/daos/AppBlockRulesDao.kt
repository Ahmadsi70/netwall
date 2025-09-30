package com.internetguard.pro.data.daos

import kotlinx.coroutines.flow.Flow
import androidx.room.*
import com.internetguard.pro.data.entities.AppBlockRules

/**
 * DAO for accessing [AppBlockRules].
 */
@Dao
interface AppBlockRulesDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(rule: AppBlockRules): Long

	@Update
	suspend fun update(rule: AppBlockRules)

	@Delete
	suspend fun delete(rule: AppBlockRules)

	@Query("SELECT * FROM app_block_rules ORDER BY createdAt DESC")
	fun observeAll(): Flow<List<AppBlockRules>>

	@Query("SELECT * FROM app_block_rules")
	suspend fun getAll(): List<AppBlockRules>

	@Query("SELECT * FROM app_block_rules WHERE appPackageName = :packageName LIMIT 1")
	suspend fun getByPackage(packageName: String): AppBlockRules?

	@Query("SELECT * FROM app_block_rules WHERE appUid = :uid LIMIT 1")
	suspend fun getByUid(uid: Int): AppBlockRules?

	@Query("DELETE FROM app_block_rules")
	suspend fun clear()
}

package com.internetguard.pro.data.daos

import kotlinx.coroutines.flow.Flow
import androidx.room.*
import com.internetguard.pro.data.entities.CustomRules

/**
 * DAO for accessing custom rules.
 */
@Dao
interface CustomRulesDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(rule: CustomRules): Long

	@Update
	suspend fun update(rule: CustomRules)

	@Delete
	suspend fun delete(rule: CustomRules)

	@Query("SELECT * FROM custom_rules WHERE isEnabled = 1 ORDER BY priority DESC, createdAt DESC")
	fun getActiveRules(): Flow<List<CustomRules>>

	@Query("SELECT * FROM custom_rules ORDER BY priority DESC, createdAt DESC")
	fun getAllRules(): Flow<List<CustomRules>>

	@Query("SELECT * FROM custom_rules WHERE ruleType = :type AND isEnabled = 1 ORDER BY priority DESC")
	suspend fun getRulesByType(type: String): List<CustomRules>

	@Query("UPDATE custom_rules SET lastTriggered = :timestamp WHERE id = :id")
	suspend fun updateLastTriggered(id: Long, timestamp: Long)

	@Query("DELETE FROM custom_rules WHERE id = :id")
	suspend fun deleteById(id: Long)
}

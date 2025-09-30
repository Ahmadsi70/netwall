package com.internetguard.pro.data.daos

import androidx.room.*
import com.internetguard.pro.data.entities.AppKeywordRules
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing [AppKeywordRules].
 */
@Dao
interface AppKeywordRulesDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AppKeywordRules)
    
    @Delete
    suspend fun delete(rule: AppKeywordRules)
    
    @Update
    suspend fun update(rule: AppKeywordRules)
    
    @Query("SELECT * FROM app_keyword_rules WHERE appPackageName = :packageName AND isEnabled = 1")
    suspend fun getActiveRulesForApp(packageName: String): List<AppKeywordRules>
    
    @Query("SELECT * FROM app_keyword_rules WHERE keywordId = :keywordId")
    fun getRulesForKeyword(keywordId: Long): Flow<List<AppKeywordRules>>
    
    // Synchronous snapshot for UI filtering
    @Query("SELECT * FROM app_keyword_rules WHERE keywordId = :keywordId")
    suspend fun getRulesForKeywordSync(keywordId: Long): List<AppKeywordRules>
    
    @Query("SELECT DISTINCT appPackageName FROM app_keyword_rules WHERE isEnabled = 1")
    suspend fun getAppsWithKeywordRules(): List<String>
    
    @Query("DELETE FROM app_keyword_rules WHERE appPackageName = :packageName")
    suspend fun deleteAllRulesForApp(packageName: String)
    
    @Query("DELETE FROM app_keyword_rules WHERE keywordId = :keywordId")
    suspend fun deleteAllRulesForKeyword(keywordId: Long)
    
    @Query("SELECT DISTINCT appPackageName FROM app_keyword_rules")
    suspend fun getAllPackageNames(): List<String>
    
    @Query("SELECT * FROM app_keyword_rules WHERE appPackageName = :packageName")
    suspend fun getRulesForApp(packageName: String): List<AppKeywordRules>
}

package com.internetguard.pro.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.internetguard.pro.data.daos.AppBlockRulesDao
import com.internetguard.pro.data.entities.AppBlockRules
import com.internetguard.pro.data.model.AppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing app blocking rules.
 * 
 * Provides a clean interface between UI and data layer,
 * handling all database operations for app rules.
 */
class AppRepository(
	private val appBlockRulesDao: AppBlockRulesDao,
	private val context: Context? = null
) {
	
	/**
	 * Gets all app blocking rules as a Flow
	 */
	fun getAllRules(): Flow<List<AppBlockRules>> = appBlockRulesDao.observeAll()
	
	/**
	 * Gets all app blocking rules as a List (for one-time read)
	 */
	suspend fun getAllRulesList(): List<AppBlockRules> = appBlockRulesDao.getAll()
	
	/**
	 * Gets a specific rule by package name
	 */
	suspend fun getRuleByPackage(packageName: String): AppBlockRules? {
		return appBlockRulesDao.getByPackage(packageName)
	}
	
	/**
	 * Gets a specific rule by UID
	 */
	suspend fun getRuleByUid(uid: Int): AppBlockRules? {
		return appBlockRulesDao.getByUid(uid)
	}
	
	/**
	 * Saves or updates an app blocking rule
	 */
	suspend fun saveRule(rule: AppBlockRules): Long {
		return appBlockRulesDao.upsert(rule)
	}
	
	/**
	 * Updates an existing rule
	 */
	suspend fun updateRule(rule: AppBlockRules) {
		appBlockRulesDao.update(rule)
	}
	
	/**
	 * Deletes a rule
	 */
	suspend fun deleteRule(rule: AppBlockRules) {
		appBlockRulesDao.delete(rule)
	}
	
	/**
	 * Clears all rules
	 */
	suspend fun clearAllRules() {
		appBlockRulesDao.clear()
	}
	
	/**
	 * Gets list of installed apps
	 */
	suspend fun getInstalledApps(): List<AppInfo> {
		if (context == null) return emptyList()
		
		val packageManager = context.packageManager
		val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
		
		return installedApps.mapNotNull { app ->
			// Skip system apps and this app itself
			if (app.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
				return@mapNotNull null
			}
			
			try {
				val appName = packageManager.getApplicationLabel(app).toString()
				val icon = try {
					packageManager.getApplicationIcon(app.packageName)
				} catch (e: Exception) {
					null
				}
				
				AppInfo(
					packageName = app.packageName,
					appName = appName,
					icon = icon,
					uid = app.uid
				)
			} catch (e: Exception) {
				null
			}
		}
	}
	
	/**
	 * Gets all blocked apps (apps with blockWifi or blockCellular = true)
	 */
	suspend fun getBlockedApps(): List<AppInfo> {
		val rules = appBlockRulesDao.getAll()
		return rules.filter { it.blockWifi || it.blockCellular }
			.map { rule ->
				AppInfo(
					uid = rule.appUid,
					packageName = rule.appPackageName,
					appName = rule.appName,
					icon = null, // Will be loaded separately if needed
					blockWifi = rule.blockWifi,
					blockCellular = rule.blockCellular,
					blockMode = rule.blockMode
				)
			}
	}
	
	/**
	 * Unblocks an app (sets both blockWifi and blockCellular to false)
	 */
	suspend fun unblockApp(packageName: String) {
		val rule = appBlockRulesDao.getByPackage(packageName)
		if (rule != null) {
			val updatedRule = rule.copy(blockWifi = false, blockCellular = false)
			appBlockRulesDao.update(updatedRule)
		}
	}
}

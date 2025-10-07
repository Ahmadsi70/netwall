package com.internetguard.pro.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.data.repository.AppRepository
import com.internetguard.pro.network.PerAppNetworkController
import com.internetguard.pro.services.NetworkGuardVpnService
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app list and blocking rules.
 * 
 * Handles fetching installed apps, managing blocking rules,
 * and updating the UI state.
 */
class AppListViewModel(application: Application) : AndroidViewModel(application) {
	
	private val repository: AppRepository
	private val packageManager: PackageManager = application.packageManager
	private val perAppNetworkController: PerAppNetworkController
	private var showBlockedOnly: Boolean = false
	
	// Optimization: Cache for app information
	private val appInfoCache = mutableMapOf<String, AppInfo>()
	private var lastCacheUpdate = 0L
	private val cacheValidityDuration = 300000L // 5 minutes
	
	// Performance optimization: Batch processing
	private val batchSize = 50 // Process apps in batches
	private var isProcessingBatch = false
	
	// LiveData for app list
	private val _appList = MutableLiveData<List<AppInfo>>()
	val appList: LiveData<List<AppInfo>> = _appList
	
	// LiveData for loading state
	private val _isLoading = MutableLiveData<Boolean>()
	val isLoading: LiveData<Boolean> = _isLoading
	
	// LiveData for error messages
	private val _errorMessage = MutableLiveData<String?>()
	val errorMessage: LiveData<String?> = _errorMessage

	// Keep full list for fast filtering without reloading
	private var fullList: List<AppInfo> = emptyList()
	
	init {
		val database = (application as InternetGuardProApp).database
		repository = AppRepository(database.appBlockRulesDao())
		perAppNetworkController = PerAppNetworkController(application)
		loadInstalledApps()
	}
	
	/**
	 * Loads all installed apps and their blocking status
	 */
	fun loadInstalledApps() {
		// Prevent multiple simultaneous loads
		if (isProcessingBatch) {
			Log.d("AppListViewModel", "Already processing apps, skipping duplicate request")
			return
		}
		
		viewModelScope.launch {
			_isLoading.value = true
			isProcessingBatch = true
			try {
				val apps = getInstalledApps().let { list ->
					if (showBlockedOnly) list.filter { it.blockWifi || it.blockCellular } else list
				}
				fullList = apps
				_appList.value = fullList
				// Load icons asynchronously after initial publish for faster first paint
				loadIconsAsync()
			} catch (e: Exception) {
				Log.e("AppListViewModel", "Error loading apps: ${e.message}", e)
				_errorMessage.value = "Failed to load apps: ${e.message}"
			} finally {
				_isLoading.value = false
				isProcessingBatch = false
			}
		}
	}

	/**
	 * Filter app list by name or package name (case-insensitive)
	 */
	fun filterApps(query: String) {
		val trimmed = query.trim()
		if (trimmed.isEmpty()) {
			_appList.value = fullList
			return
		}
		val q = trimmed.lowercase()
		_appList.value = fullList.filter { app ->
			app.appName.lowercase().contains(q) || app.packageName.lowercase().contains(q)
		}
	}

	fun setShowBlockedOnly(enabled: Boolean) {
		showBlockedOnly = enabled
	}
	
	/**
	 * Gets list of installed apps with their blocking status - HIGHLY OPTIMIZED
	 */
	private suspend fun getInstalledApps(): List<AppInfo> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
		// Check cache validity
		val currentTime = System.currentTimeMillis()
		if (appInfoCache.isNotEmpty() && (currentTime - lastCacheUpdate) < cacheValidityDuration) {
			Log.d("AppListViewModel", "Using cached app list (${appInfoCache.size} apps)")
			return@withContext appInfoCache.values.toList()
		}
		
		Log.d("AppListViewModel", "Loading fresh app list...")
		
		// 🚀 OPTIMIZATION 1: Read all rules once (1 query instead of N queries)
		val allRules = repository.getAllRulesList()
		val rulesMap = allRules.associateBy { it.appPackageName }
		
		// 🚀 OPTIMIZATION 2: Filter system apps and only launchable apps
		val installedApps = packageManager.getInstalledApplications(0)
			.filter { app ->
				// Only user apps (not system)
				(app.flags and ApplicationInfo.FLAG_SYSTEM == 0) &&
				// Not this app itself
				app.packageName != getApplication<Application>().packageName &&
				// Only launchable apps
				packageManager.getLaunchIntentForPackage(app.packageName) != null
			}
		
		Log.d("AppListViewModel", "Found ${installedApps.size} user apps")
		
		val appInfoList = mutableListOf<AppInfo>()
		
		// Clear old cache
		appInfoCache.clear()
		
		// 🚀 OPTIMIZATION 3: Process in batches for better performance
		val batches = installedApps.chunked(batchSize)
		var processedCount = 0
		
		for (batch in batches) {
			// Process batch
			for (app in batch) {
				val appName = try {
					packageManager.getApplicationLabel(app).toString()
				} catch (e: Exception) {
					app.packageName
				}
				
				// 🚀 OPTIMIZATION 4: Skip icon loading initially for faster first paint
				val icon = null // Load icons asynchronously later
				
				// 🚀 Use Map instead of database query
				val existingRule = rulesMap[app.packageName]
				
				val appInfo = AppInfo(
					uid = app.uid,
					packageName = app.packageName,
					appName = appName,
					icon = icon,
					blockWifi = existingRule?.blockWifi ?: false,
					blockCellular = existingRule?.blockCellular ?: false,
					blockMode = existingRule?.blockMode ?: "blacklist"
				)
				
				// Add to cache
				appInfoCache[app.packageName] = appInfo
				appInfoList.add(appInfo)
				processedCount++
			}
			
			// 🚀 OPTIMIZATION 5: Yield control between batches to prevent ANR
			kotlinx.coroutines.yield()
		}
		
		// Update cache time
		lastCacheUpdate = currentTime
		
		Log.d("AppListViewModel", "Processed $processedCount apps in ${batches.size} batches")
		
		return@withContext appInfoList.sortedBy { it.appName }
	}
	
	/**
	 * Updates Wi-Fi blocking status for an app
	 */
	fun updateWifiBlocking(packageName: String, blockWifi: Boolean) {
		viewModelScope.launch {
			try {
				val existingRule = repository.getRuleByPackage(packageName)
				val appInfo = _appList.value?.find { it.packageName == packageName }
				
				if (existingRule != null) {
					// Update existing rule
					val updatedRule = existingRule.copy(blockWifi = blockWifi)
					repository.updateRule(updatedRule)
				} else if (appInfo != null) {
					// Create new rule
					val newRule = com.internetguard.pro.data.entities.AppBlockRules(
						appUid = appInfo.uid,
						appPackageName = packageName,
						appName = appInfo.appName,
						blockWifi = blockWifi,
						blockCellular = appInfo.blockCellular,
						blockMode = appInfo.blockMode,
						createdAt = System.currentTimeMillis()
					)
					repository.saveRule(newRule)
				}
				
				// Update UI
				updateAppInList(packageName) { it.copy(blockWifi = blockWifi) }

                // Block/Allow internet for selected app using VPN Service
                val intent = Intent(getApplication(), NetworkGuardVpnService::class.java)
                // Send network type for precise blocking
                intent.putExtra("network_type", "wifi")
                if (blockWifi) {
                    // Block internet for selected app
                    intent.action = NetworkGuardVpnService.ACTION_BLOCK_APP
                    intent.putExtra("package_name", packageName)
                    getApplication<Application>().startForegroundService(intent)
                    Log.d("AppListViewModel", "Blocking app: $packageName")
                } else {
                    // Allow internet for selected app
                    intent.action = NetworkGuardVpnService.ACTION_UNBLOCK_APP
                    intent.putExtra("package_name", packageName)
                    getApplication<Application>().startForegroundService(intent)
                    Log.d("AppListViewModel", "Unblocking app: $packageName")
                }
			} catch (e: Exception) {
				_errorMessage.value = "Failed to update Wi-Fi blocking: ${e.message}"
			}
		}
	}
	
	/**
	 * Updates cellular blocking status for an app
	 */
	fun updateCellularBlocking(packageName: String, blockCellular: Boolean) {
		viewModelScope.launch {
			try {
				val existingRule = repository.getRuleByPackage(packageName)
				val appInfo = _appList.value?.find { it.packageName == packageName }
				
				if (existingRule != null) {
					// Update existing rule
					val updatedRule = existingRule.copy(blockCellular = blockCellular)
					repository.updateRule(updatedRule)
				} else if (appInfo != null) {
					// Create new rule
					val newRule = com.internetguard.pro.data.entities.AppBlockRules(
						appUid = appInfo.uid,
						appPackageName = packageName,
						appName = appInfo.appName,
						blockWifi = appInfo.blockWifi,
						blockCellular = blockCellular,
						blockMode = appInfo.blockMode,
						createdAt = System.currentTimeMillis()
					)
					repository.saveRule(newRule)
				}
				
				// Update UI
				updateAppInList(packageName) { it.copy(blockCellular = blockCellular) }

                // Block/Allow internet for selected app using VPN Service
                val intent = Intent(getApplication(), NetworkGuardVpnService::class.java)
                // Send network type for precise blocking
                intent.putExtra("network_type", "cellular")
                if (blockCellular) {
                    // Block internet for selected app
                    intent.action = NetworkGuardVpnService.ACTION_BLOCK_APP
                    intent.putExtra("package_name", packageName)
                    getApplication<Application>().startForegroundService(intent)
                    Log.d("AppListViewModel", "Blocking app: $packageName")
                } else {
                    // Allow internet for selected app
                    intent.action = NetworkGuardVpnService.ACTION_UNBLOCK_APP
                    intent.putExtra("package_name", packageName)
                    getApplication<Application>().startForegroundService(intent)
                    Log.d("AppListViewModel", "Unblocking app: $packageName")
                }
			} catch (e: Exception) {
				_errorMessage.value = "Failed to update cellular blocking: ${e.message}"
			}
		}
	}
	
	/**
	 * Updates an app in the current list
	 */
	private fun updateAppInList(packageName: String, update: (AppInfo) -> AppInfo) {
		val currentList = _appList.value?.toMutableList() ?: return
		val index = currentList.indexOfFirst { it.packageName == packageName }
		if (index != -1) {
			currentList[index] = update(currentList[index])
			_appList.value = currentList
		}
	}
	
	/**
	 * Clears error message
	 */
	fun clearError() {
		_errorMessage.value = null
	}

	/**
	 * Asynchronously loads app icons and updates list items progressively
	 */
	private fun loadIconsAsync() {
		viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
			val pm = packageManager
			val current = _appList.value ?: return@launch
			
			Log.d("AppListViewModel", "Loading icons for ${current.size} apps")
			
			// 🚀 OPTIMIZATION: Load icons in batches to prevent UI blocking
			val appsWithoutIcons = current.filter { it.icon == null }
			val batches = appsWithoutIcons.chunked(20) // Smaller batches for icons
			
			for (batch in batches) {
				for (item in batch) {
					try {
						val icon = pm.getApplicationIcon(item.packageName)
						updateAppInList(item.packageName) { it.copy(icon = icon) }
					} catch (e: Exception) {
						Log.w("AppListViewModel", "Failed to load icon for ${item.packageName}: ${e.message}")
					}
				}
				
				// 🚀 Yield control between batches to keep UI responsive
				kotlinx.coroutines.yield()
			}
			
			Log.d("AppListViewModel", "Finished loading icons")
		}
	}

}

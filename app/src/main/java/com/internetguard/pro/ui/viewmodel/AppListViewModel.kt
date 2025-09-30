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
	
	// بهینه‌سازی: Cache برای اطلاعات اپ‌ها
	private val appInfoCache = mutableMapOf<String, AppInfo>()
	private var lastCacheUpdate = 0L
	private val cacheValidityDuration = 300000L // 5 minutes
	
	// LiveData for app list
	private val _appList = MutableLiveData<List<AppInfo>>()
	val appList: LiveData<List<AppInfo>> = _appList
	
	// LiveData for loading state
	private val _isLoading = MutableLiveData<Boolean>()
	val isLoading: LiveData<Boolean> = _isLoading
	
	// LiveData for error messages
	private val _errorMessage = MutableLiveData<String?>()
	val errorMessage: LiveData<String?> = _errorMessage

	// نگه‌داری لیست کامل برای فیلتر سریع بدون لود مجدد
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
		viewModelScope.launch {
			_isLoading.value = true
			try {
				val apps = getInstalledApps().let { list ->
					if (showBlockedOnly) list.filter { it.blockWifi || it.blockCellular } else list
				}
				fullList = apps
				_appList.value = fullList
			} catch (e: Exception) {
				_errorMessage.value = "Failed to load apps: ${e.message}"
			} finally {
				_isLoading.value = false
			}
		}
	}

	/**
	 * فیلتر لیست اپ‌ها بر اساس نام یا نام پکیج (case-insensitive)
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
		// بررسی cache validity
		val currentTime = System.currentTimeMillis()
		if (appInfoCache.isNotEmpty() && (currentTime - lastCacheUpdate) < cacheValidityDuration) {
			return@withContext appInfoCache.values.toList()
		}
		
		// 🚀 OPTIMIZATION 1: خواندن همه rules یک‌بار (1 query به جای N query)
		val allRules = repository.getAllRulesList()
		val rulesMap = allRules.associateBy { it.appPackageName }
		
		// 🚀 OPTIMIZATION 2: فیلتر اپ‌های سیستمی و فقط اپ‌های قابل launch
		val installedApps = packageManager.getInstalledApplications(0)
			.filter { app ->
				// فقط اپ‌های user (نه سیستمی)
				(app.flags and ApplicationInfo.FLAG_SYSTEM == 0) &&
				// نه خود این اپ
				app.packageName != getApplication<Application>().packageName &&
				// فقط اپ‌هایی که قابل launch هستند
				packageManager.getLaunchIntentForPackage(app.packageName) != null
			}
		
		val appInfoList = mutableListOf<AppInfo>()
		
		// پاک کردن cache قدیمی
		appInfoCache.clear()
		
		// 🚀 OPTIMIZATION 3: پردازش موازی با chunked
		for (app in installedApps) {
			val appName = try {
				packageManager.getApplicationLabel(app).toString()
			} catch (e: Exception) {
				app.packageName
			}
			
			val icon = try {
				packageManager.getApplicationIcon(app.packageName)
			} catch (e: Exception) {
				null
			}
			
			// 🚀 از Map استفاده می‌کنیم به جای database query
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
			
			// اضافه کردن به cache
			appInfoCache[app.packageName] = appInfo
			appInfoList.add(appInfo)
		}
		
		// به‌روزرسانی زمان cache
		lastCacheUpdate = currentTime
		
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
                // ارسال نوع شبکه برای دقیق‌سازی بلاک
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
                // ارسال نوع شبکه برای دقیق‌سازی بلاک
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

}

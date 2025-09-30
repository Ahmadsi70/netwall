package com.internetguard.pro.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.data.repository.AppRepository
import com.internetguard.pro.services.NetworkGuardVpnService
import kotlinx.coroutines.launch

/**
 * ViewModel for dashboard fragment.
 * 
 * Manages blocked apps list and statistics.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
	
	private val repository: AppRepository
    private var vpnStatusReceiver: BroadcastReceiver? = null
	
	// LiveData for blocked apps list
	private val _blockedAppsList = MutableLiveData<List<AppInfo>>()
	val blockedAppsList: LiveData<List<AppInfo>> = _blockedAppsList
	
	// LiveData for blocked apps count
	private val _blockedAppsCount = MutableLiveData<Int>()
	val blockedAppsCount: LiveData<Int> = _blockedAppsCount

	// LiveData for VPN connection status
	private val _vpnConnected = MutableLiveData<Boolean>()
	val vpnConnected: LiveData<Boolean> = _vpnConnected
	
	// LiveData for statistics
	private val _blockedCount = MutableLiveData<Long>()
	val blockedCount: LiveData<Long> = _blockedCount
	
	private val _timeSaved = MutableLiveData<Long>()
	val timeSaved: LiveData<Long> = _timeSaved
	
	// LiveData for loading state
	private val _isLoading = MutableLiveData<Boolean>()
	val isLoading: LiveData<Boolean> = _isLoading
	
	init {
		val database = (application as InternetGuardProApp).database
		repository = AppRepository(database.appBlockRulesDao())
		loadBlockedApps()
        registerVpnStatusReceiver()
	}
	
	/**
	 * Loads blocked apps from repository
	 */
	private fun loadBlockedApps() {
		_isLoading.value = true
		
		viewModelScope.launch {
			try {
				val blockedApps = repository.getBlockedApps()
				_blockedAppsList.value = blockedApps
				_blockedAppsCount.value = blockedApps.size
				_blockedCount.value = blockedApps.size.toLong()
				_timeSaved.value = calculateTimeSaved(blockedApps.size)
			} catch (e: Exception) {
				// Handle error
			} finally {
				_isLoading.value = false
			}
		}
	}
	
	/**
	 * Calculate time saved based on blocked apps count
	 */
	private fun calculateTimeSaved(blockedAppsCount: Int): Long {
		// Simple calculation: assume 30 minutes saved per blocked app per day
		return blockedAppsCount * 30L
	}
	
	/**
	 * Unblock an app
	 */
	fun unblockApp(packageName: String) {
		viewModelScope.launch {
			try {
				repository.unblockApp(packageName)
				
				// Send unblock intent to VPN service
				val intent = Intent(getApplication(), NetworkGuardVpnService::class.java)
				// پیش‌فرض: آزادسازی روی هر دو شبکه
				intent.putExtra("network_type", "all")
				intent.action = NetworkGuardVpnService.ACTION_UNBLOCK_APP
				intent.putExtra("package_name", packageName)
				getApplication<Application>().startForegroundService(intent)
				
				// Reload blocked apps
				loadBlockedApps()
			} catch (e: Exception) {
				// Handle error
			}
		}
	}
	
	/**
	 * Updates blocked content count
	 */
	fun updateBlockedCount(count: Long) {
		_blockedCount.value = count
	}
	
	/**
	 * Updates time saved statistic
	 */
	fun updateTimeSaved(timeInMinutes: Long) {
		_timeSaved.value = timeInMinutes
	}
	
	/**
	 * Refreshes all dashboard data
	 */
	fun refresh() {
		loadBlockedApps()
	}

    private fun registerVpnStatusReceiver() {
        val filter = IntentFilter("com.internetguard.pro.VPN_STATUS_UPDATE")
        vpnStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
				val count = intent.getIntExtra("blocked_apps_count", 0)
				val connected = intent.getBooleanExtra("is_connected", false)
				_blockedAppsCount.postValue(count)
				_vpnConnected.postValue(connected)
            }
        }
        getApplication<Application>().registerReceiver(vpnStatusReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(vpnStatusReceiver)
        } catch (_: Exception) {}
    }
}
package com.internetguard.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.security.BiometricAuthManager
// import com.internetguard.pro.security.DatabaseEncryptionManager // Removed for size optimization
import com.internetguard.pro.data.model.EncryptionStatus
import com.internetguard.pro.data.model.PrivacyStatus
import kotlinx.coroutines.launch

/**
 * ViewModel for security settings fragment.
 * 
 * Manages security and privacy settings including biometric authentication,
 * database encryption, and advanced privacy features.
 */
class SecuritySettingsViewModel(application: Application) : AndroidViewModel(application) {
	
	private val biometricManager: BiometricAuthManager
    // private val encryptionManager: DatabaseEncryptionManager // Removed for size optimization
	// private val privacyManager: AdvancedPrivacyManager // Removed for cleanup
	
	// LiveData for biometric authentication
	private val _isBiometricEnabled = MutableLiveData<Boolean>()
	val isBiometricEnabled: LiveData<Boolean> = _isBiometricEnabled
	
	private val _isBiometricAvailable = MutableLiveData<Boolean>()
	val isBiometricAvailable: LiveData<Boolean> = _isBiometricAvailable
	
	// LiveData for encryption status
	private val _encryptionStatus = MutableLiveData<com.internetguard.pro.data.model.EncryptionStatus>()
	val encryptionStatus: LiveData<com.internetguard.pro.data.model.EncryptionStatus> = _encryptionStatus
	
	// LiveData for privacy status
	private val _privacyStatus = MutableLiveData<com.internetguard.pro.data.model.PrivacyStatus>()
	val privacyStatus: LiveData<com.internetguard.pro.data.model.PrivacyStatus> = _privacyStatus
	
	// LiveData for security score
	private val _securityScore = MutableLiveData<Int>()
	val securityScore: LiveData<Int> = _securityScore
	
	init {
		val app = application as InternetGuardProApp
		biometricManager = BiometricAuthManager(application)
		// encryptionManager = app.encryptionManager // Removed for size optimization
		// privacyManager = AdvancedPrivacyManager(application) // Removed for cleanup
		
		loadSecuritySettings()
	}
	
	/**
	 * Loads current security settings
	 */
	private fun loadSecuritySettings() {
		// Load biometric settings
		_isBiometricEnabled.value = biometricManager.isBiometricEnabled()
		_isBiometricAvailable.value = biometricManager.isBiometricAvailable()
		
		// Load encryption status (simplified without SQLCipher)
		// val encryptionStatus = encryptionManager.getEncryptionStatus() // Removed
		_encryptionStatus.value = EncryptionStatus(false, 70) // Simplified without encryption
		
		// Load privacy status (simplified)
		_privacyStatus.value = PrivacyStatus(false, false, false, false, false) // Default values
		
		// Calculate security score
		calculateSecurityScore()
	}
	
	/**
	 * Enables biometric authentication
	 */
	fun enableBiometric() {
		biometricManager.enableBiometric()
		_isBiometricEnabled.value = true
		calculateSecurityScore()
	}
	
	/**
	 * Disables biometric authentication
	 */
	fun disableBiometric() {
		biometricManager.disableBiometric()
		_isBiometricEnabled.value = false
		calculateSecurityScore()
	}
	
	/**
	 * Calculates overall security score
	 */
	private fun calculateSecurityScore() {
		viewModelScope.launch {
			var score = 0
			
			// Biometric authentication (30 points)
			if (biometricManager.isBiometricEnabled()) {
				score += 30
			}
			
			// Database encryption (25 points)
			val encryptionStatus = _encryptionStatus.value
			if (encryptionStatus?.isEncrypted == true) {
				score += 25
				// Additional points for password strength
				score += (encryptionStatus.passwordStrength / 10)
			}
			
			// Privacy features (20 points)
			val privacyStatus = _privacyStatus.value
			var privacyScore = 0
			if (privacyStatus?.privateDNSEnabled == true) privacyScore += 5
			if (privacyStatus?.vpnPassthroughEnabled == true) privacyScore += 5
			if (privacyStatus?.networkMonitoringEnabled == true) privacyScore += 5
			if (privacyStatus?.dnsOverHTTPSEnabled == true || privacyStatus?.dnsOverTLSEnabled == true) privacyScore += 5
			score += privacyScore
			
			// Permission grants (15 points)
			// This would be implemented based on actual permission status
			score += 15
			
			// Recent security updates (10 points)
			// This would be implemented based on app update status
			score += 10
			
			_securityScore.value = minOf(score, 100)
		}
	}
	
	/**
	 * Clears all app data
	 */
	fun clearAllData() {
		viewModelScope.launch {
			try {
				// Clear database
				val database = (getApplication() as InternetGuardProApp).database
				database.clearAllTables()
				
				// Clear encryption settings (removed for size optimization)
				// encryptionManager.clearStoredPassword()
				
				// Reset privacy settings (simplified)
				// privacyManager.resetPrivacySettings() // Removed for cleanup
				
				// Disable biometric
				biometricManager.disableBiometric()
				
				// Reload settings
				loadSecuritySettings()
			} catch (e: Exception) {
				// Handle error
			}
		}
	}
	
	/**
	 * Gets security recommendations
	 */
	fun getSecurityRecommendations(): List<SecurityRecommendation> {
		// Simplified recommendations without AdvancedPrivacyManager
		return listOf(
			SecurityRecommendation(
				type = "biometric",
				title = "Enable Biometric Authentication",
				description = "Use fingerprint or face recognition for app security",
				priority = "high"
			)
		)
	}
	
	/**
	 * Refreshes all security settings
	 */
	fun refreshSettings() {
		loadSecuritySettings()
	}
}

/**
 * Data class for security recommendations
 */
data class SecurityRecommendation(
	val type: String,
	val title: String,
	val description: String,
	val priority: String
)

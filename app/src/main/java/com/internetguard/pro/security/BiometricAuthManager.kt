package com.internetguard.pro.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.internetguard.pro.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manager for biometric authentication throughout the app.
 * 
 * Provides secure biometric authentication for sensitive operations
 * like accessing settings, modifying keyword lists, and disabling blocks.
 * All authentication is handled locally with no external dependencies.
 */
class BiometricAuthManager(private val context: Context) {
	
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val prefs: SharedPreferences = context.getSharedPreferences("biometric_auth", Context.MODE_PRIVATE)
	
	companion object {
		private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
		private const val BIOMETRIC_LAST_AUTH_KEY = "biometric_last_auth"
		private const val AUTH_TIMEOUT = 300000L // 5 minutes
		private const val MAX_AUTH_ATTEMPTS = 3
		private const val AUTH_ATTEMPTS_KEY = "auth_attempts"
		private const val AUTH_LOCKOUT_KEY = "auth_lockout"
		private const val LOCKOUT_DURATION = 300000L // 5 minutes
	}
	
	/**
	 * Checks if biometric authentication is available on the device
	 */
	fun isBiometricAvailable(): Boolean {
		val biometricManager = BiometricManager.from(context)
		return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
	}
	
	/**
	 * Checks if biometric authentication is enabled by user
	 */
	fun isBiometricEnabled(): Boolean {
		return prefs.getBoolean(BIOMETRIC_ENABLED_KEY, false)
	}
	
	/**
	 * Enables biometric authentication
	 */
	fun enableBiometric() {
		prefs.edit().putBoolean(BIOMETRIC_ENABLED_KEY, true).apply()
	}
	
	/**
	 * Disables biometric authentication
	 */
	fun disableBiometric() {
		prefs.edit().putBoolean(BIOMETRIC_ENABLED_KEY, false).apply()
	}
	
	/**
	 * Checks if authentication is required for sensitive operations
	 */
	fun isAuthenticationRequired(): Boolean {
		if (!isBiometricEnabled()) return false
		if (isLockedOut()) return true
		
		val lastAuth = prefs.getLong(BIOMETRIC_LAST_AUTH_KEY, 0)
		val currentTime = System.currentTimeMillis()
		
		return (currentTime - lastAuth) > AUTH_TIMEOUT
	}
	
	/**
	 * Checks if user is currently locked out due to failed attempts
	 */
	private fun isLockedOut(): Boolean {
		val lockoutTime = prefs.getLong(AUTH_LOCKOUT_KEY, 0)
		val currentTime = System.currentTimeMillis()
		return (currentTime - lockoutTime) < LOCKOUT_DURATION
	}
	
	/**
	 * Shows biometric prompt for general authentication
	 */
	fun showBiometricPrompt(
		activity: FragmentActivity,
		title: String = "Biometric Authentication",
		subtitle: String = "Use your biometric to continue",
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		if (!isBiometricAvailable()) {
			onError("Biometric authentication is not available on this device")
			return
		}
		
		if (isLockedOut()) {
			onError("Too many failed attempts. Please try again later.")
			return
		}
		
		val executor = ContextCompat.getMainExecutor(context)
		val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				updateLastAuthTime()
				resetAuthAttempts()
				onSuccess()
			}
			
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				handleAuthError(errorCode, errString.toString(), onError)
			}
			
			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				handleAuthFailure(onError)
			}
		})
		
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle(title)
			.setSubtitle(subtitle)
			.setNegativeButtonText("Cancel")
			.build()
		
		biometricPrompt.authenticate(promptInfo)
	}
	
	/**
	 * Shows biometric prompt for sensitive operations
	 */
	fun showBiometricPromptForSensitiveOperation(
		activity: FragmentActivity,
		operation: String,
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		showBiometricPrompt(
			activity = activity,
			title = "Confirm $operation",
			subtitle = "Use your biometric to confirm this sensitive action",
			onSuccess = onSuccess,
			onError = onError
		)
	}
	
	/**
	 * Shows biometric prompt for settings access
	 */
	fun showBiometricPromptForSettings(
		activity: FragmentActivity,
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		showBiometricPrompt(
			activity = activity,
			title = "Access Settings",
			subtitle = "Use your biometric to access app settings",
			onSuccess = onSuccess,
			onError = onError
		)
	}
	
	/**
	 * Shows biometric prompt for keyword list modification
	 */
	fun showBiometricPromptForKeywordModification(
		activity: FragmentActivity,
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		showBiometricPrompt(
			activity = activity,
			title = "Modify Keyword List",
			subtitle = "Use your biometric to modify the keyword blacklist",
			onSuccess = onSuccess,
			onError = onError
		)
	}
	
	/**
	 * Shows biometric prompt for disabling blocks
	 */
	fun showBiometricPromptForDisablingBlocks(
		activity: FragmentActivity,
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		showBiometricPrompt(
			activity = activity,
			title = "Disable Blocks",
			subtitle = "Use your biometric to disable content blocking",
			onSuccess = onSuccess,
			onError = onError
		)
	}
	
	/**
	 * Handles authentication errors
	 */
	private fun handleAuthError(errorCode: Int, errorMessage: String, onError: (String) -> Unit) {
		when (errorCode) {
			BiometricPrompt.ERROR_NO_BIOMETRICS -> {
				onError("No biometric data enrolled. Please set up biometric authentication in device settings.")
			}
			BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
				onError("Biometric hardware is currently unavailable.")
			}
			BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> {
				onError("Unable to process biometric data. Please try again.")
			}
			BiometricPrompt.ERROR_TIMEOUT -> {
				onError("Authentication timed out. Please try again.")
			}
			BiometricPrompt.ERROR_CANCELED -> {
				onError("Authentication was canceled.")
			}
			BiometricPrompt.ERROR_LOCKOUT -> {
				onError("Too many failed attempts. Biometric authentication is temporarily disabled.")
			}
			BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
				onError("Too many failed attempts. Biometric authentication is permanently disabled.")
			}
			else -> {
				onError("Authentication failed: $errorMessage")
			}
		}
	}
	
	/**
	 * Handles authentication failures
	 */
	private fun handleAuthFailure(onError: (String) -> Unit) {
		val attempts = prefs.getInt(AUTH_ATTEMPTS_KEY, 0) + 1
		prefs.edit().putInt(AUTH_ATTEMPTS_KEY, attempts).apply()
		
		if (attempts >= MAX_AUTH_ATTEMPTS) {
			lockoutUser()
			onError("Too many failed attempts. Please try again in 5 minutes.")
		} else {
			onError("Authentication failed. ${MAX_AUTH_ATTEMPTS - attempts} attempts remaining.")
		}
	}
	
	/**
	 * Locks out user after too many failed attempts
	 */
	private fun lockoutUser() {
		val currentTime = System.currentTimeMillis()
		prefs.edit()
			.putLong(AUTH_LOCKOUT_KEY, currentTime)
			.putInt(AUTH_ATTEMPTS_KEY, 0)
			.apply()
	}
	
	/**
	 * Resets authentication attempts counter
	 */
	private fun resetAuthAttempts() {
		prefs.edit().putInt(AUTH_ATTEMPTS_KEY, 0).apply()
	}
	
	/**
	 * Updates last successful authentication time
	 */
	private fun updateLastAuthTime() {
		prefs.edit().putLong(BIOMETRIC_LAST_AUTH_KEY, System.currentTimeMillis()).apply()
	}
	
	/**
	 * Gets remaining lockout time in milliseconds
	 */
	fun getRemainingLockoutTime(): Long {
		val lockoutTime = prefs.getLong(AUTH_LOCKOUT_KEY, 0)
		val currentTime = System.currentTimeMillis()
		val elapsed = currentTime - lockoutTime
		return if (elapsed < LOCKOUT_DURATION) {
			LOCKOUT_DURATION - elapsed
		} else {
			0
		}
	}
	
	/**
	 * Gets remaining authentication attempts
	 */
	fun getRemainingAttempts(): Int {
		val attempts = prefs.getInt(AUTH_ATTEMPTS_KEY, 0)
		return MAX_AUTH_ATTEMPTS - attempts
	}
}

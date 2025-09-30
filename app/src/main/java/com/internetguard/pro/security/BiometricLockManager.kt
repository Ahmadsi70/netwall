package com.internetguard.pro.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.entities.CustomRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Manager for biometric authentication and security features.
 * 
 * Provides biometric lock functionality to secure app access
 * and sensitive operations.
 */
class BiometricLockManager(private val context: Context) {
	
	private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private val database by lazy { (context.applicationContext as InternetGuardProApp).database }
	private val prefs: SharedPreferences = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
	
	companion object {
		private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
		private const val BIOMETRIC_LAST_AUTH_KEY = "biometric_last_auth"
		private const val AUTH_TIMEOUT = 300000L // 5 minutes
	}
	
	/**
	 * Checks if biometric authentication is available
	 */
	fun isBiometricAvailable(): Boolean {
		val biometricManager = BiometricManager.from(context)
		return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
	}
	
	/**
	 * Checks if biometric authentication is enabled
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
	 * Checks if authentication is required
	 */
	fun isAuthenticationRequired(): Boolean {
		if (!isBiometricEnabled()) return false
		
		val lastAuth = prefs.getLong(BIOMETRIC_LAST_AUTH_KEY, 0)
		val currentTime = System.currentTimeMillis()
		
		return (currentTime - lastAuth) > AUTH_TIMEOUT
	}
	
	/**
	 * Shows biometric prompt for authentication
	 */
	fun showBiometricPrompt(
		activity: FragmentActivity,
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		val executor = ContextCompat.getMainExecutor(context)
		val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				updateLastAuthTime()
				onSuccess()
			}
			
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				onError(errString.toString())
			}
			
			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				onError("Authentication failed")
			}
		})
		
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle("Biometric Authentication")
			.setSubtitle("Use your biometric to access InternetGuard Pro")
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
		val executor = ContextCompat.getMainExecutor(context)
		val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				updateLastAuthTime()
				onSuccess()
			}
			
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				onError(errString.toString())
			}
			
			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				onError("Authentication failed")
			}
		})
		
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle("Confirm $operation")
			.setSubtitle("Use your biometric to confirm this action")
			.setNegativeButtonText("Cancel")
			.build()
		
		biometricPrompt.authenticate(promptInfo)
	}
	
	/**
	 * Updates last authentication time
	 */
	private fun updateLastAuthTime() {
		prefs.edit().putLong(BIOMETRIC_LAST_AUTH_KEY, System.currentTimeMillis()).apply()
	}
	
	/**
	 * Creates biometric-based blocking rules
	 */
	fun createBiometricBlockingRule() {
		serviceScope.launch {
			try {
				val conditions = JSONObject().apply {
					put("biometricRequired", true)
					put("sensitiveOperation", true)
				}
				
				val actions = JSONObject().apply {
					put("type", "require_biometric")
					put("message", "Biometric authentication required for this operation")
				}
				
				val rule = CustomRules(
					ruleName = "Biometric Security Rule",
					ruleType = "biometric",
					isEnabled = true,
					conditions = conditions.toString(),
					actions = actions.toString(),
					priority = 15,
					createdAt = System.currentTimeMillis()
				)
				
				database.customRulesDao().insert(rule)
			} catch (e: Exception) {
				// Handle error
			}
		}
	}
	
	/**
	 * Validates biometric authentication for rule execution
	 */
	fun validateBiometricForRule(rule: CustomRules): Boolean {
		val conditions = JSONObject(rule.conditions)
		val requiresBiometric = conditions.optBoolean("biometricRequired", false)
		
		return if (requiresBiometric) {
			!isAuthenticationRequired()
		} else {
			true
		}
	}
}

package com.internetguard.pro.security

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.internetguard.pro.R

/**
 * Manager for handling runtime permissions with clear explanations.
 * 
 * Provides user-friendly permission requests with detailed explanations
 * for why each permission is needed. All permissions are for local processing only.
 */
class PermissionManager(
	private val activity: FragmentActivity,
	private val fragment: Fragment? = null
) {
	
	private val context: Context = activity.applicationContext
	private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
	private var settingsLauncher: ActivityResultLauncher<Intent>? = null
	
	companion object {
		// Core permissions
		const val INTERNET = Manifest.permission.INTERNET
		const val FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE
		const val QUERY_ALL_PACKAGES = Manifest.permission.QUERY_ALL_PACKAGES
		const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
		const val PACKAGE_USAGE_STATS = Manifest.permission.PACKAGE_USAGE_STATS
		const val BIND_VPN_SERVICE = Manifest.permission.BIND_VPN_SERVICE
		const val BIND_ACCESSIBILITY_SERVICE = Manifest.permission.BIND_ACCESSIBILITY_SERVICE
		const val RECEIVE_BOOT_COMPLETED = Manifest.permission.RECEIVE_BOOT_COMPLETED
		
		// Permission groups
		val CORE_PERMISSIONS = arrayOf(
			INTERNET,
			FOREGROUND_SERVICE,
			RECEIVE_BOOT_COMPLETED
		)
		
		val APP_MONITORING_PERMISSIONS = arrayOf(
			QUERY_ALL_PACKAGES,
			PACKAGE_USAGE_STATS
		)
		
		val LOCATION_PERMISSIONS = arrayOf(
			ACCESS_FINE_LOCATION
		)
		
		val SERVICE_PERMISSIONS = arrayOf(
			BIND_VPN_SERVICE,
			BIND_ACCESSIBILITY_SERVICE
		)
	}
	
	/**
	 * Initializes permission launchers
	 */
	fun initialize() {
		permissionLauncher = (fragment ?: activity).registerForActivityResult(
			ActivityResultContracts.RequestMultiplePermissions()
		) { permissions ->
			handlePermissionResults(permissions)
		}
		
		settingsLauncher = (fragment ?: activity).registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()
		) { result ->
			handleSettingsResult(result.resultCode)
		}
	}
	
	/**
	 * Checks if all core permissions are granted
	 */
	fun areCorePermissionsGranted(): Boolean {
		return CORE_PERMISSIONS.all { permission ->
			ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
		}
	}
	
	/**
	 * Checks if app monitoring permissions are granted
	 */
	fun areAppMonitoringPermissionsGranted(): Boolean {
		return APP_MONITORING_PERMISSIONS.all { permission ->
			ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
		}
	}
	
	/**
	 * Checks if location permissions are granted
	 */
	fun areLocationPermissionsGranted(): Boolean {
		return LOCATION_PERMISSIONS.all { permission ->
			ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
		}
	}
	
	/**
	 * Checks if service permissions are granted
	 */
	fun areServicePermissionsGranted(): Boolean {
		return SERVICE_PERMISSIONS.all { permission ->
			ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
		}
	}
	
	/**
	 * Requests core permissions with explanation
	 */
	fun requestCorePermissions() {
		requestPermissionsWithExplanation(
			permissions = CORE_PERMISSIONS,
			title = "Core Permissions",
			message = "InternetGuard Pro needs these basic permissions to function:\n\n" +
					"• Internet: To check for app updates and security definitions\n" +
					"• Foreground Service: To run background protection continuously\n" +
					"• Boot Completed: To restart protection after device reboot\n\n" +
					"All data processing happens locally on your device."
		)
	}
	
	/**
	 * Requests app monitoring permissions with explanation
	 */
	fun requestAppMonitoringPermissions() {
		requestPermissionsWithExplanation(
			permissions = APP_MONITORING_PERMISSIONS,
			title = "App Monitoring Permissions",
			message = "To monitor and control app usage, InternetGuard Pro needs:\n\n" +
					"• Query All Packages: To see installed apps and their network usage\n" +
					"• Usage Stats: To track which apps you use and for how long\n\n" +
					"This data stays on your device and is never shared."
		)
	}
	
	/**
	 * Requests location permissions with explanation
	 */
	fun requestLocationPermissions() {
		requestPermissionsWithExplanation(
			permissions = LOCATION_PERMISSIONS,
			title = "Location Permission",
			message = "InternetGuard Pro uses location for:\n\n" +
					"• Location-based blocking rules (e.g., block social media at work)\n" +
					"• Geofencing features for automatic protection\n\n" +
					"Location data is processed locally and never transmitted."
		)
	}
	
	/**
	 * Requests service permissions with explanation
	 */
	fun requestServicePermissions() {
		requestPermissionsWithExplanation(
			permissions = SERVICE_PERMISSIONS,
			title = "Service Permissions",
			message = "InternetGuard Pro needs these permissions to provide protection:\n\n" +
					"• VPN Service: To intercept and filter network traffic\n" +
					"• Accessibility Service: To monitor app content for keyword blocking\n\n" +
					"These services run locally and don't access external servers."
		)
	}
	
	/**
	 * Requests permissions with detailed explanation dialog
	 */
	private fun requestPermissionsWithExplanation(
		permissions: Array<String>,
		title: String,
		message: String
	) {
		val deniedPermissions = permissions.filter { permission ->
			ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
		}
		
		if (deniedPermissions.isEmpty()) {
			// All permissions already granted
			return
		}
		
		// Check if we should show rationale
		val shouldShowRationale: Boolean = deniedPermissions.any { permission: String ->
			if (fragment != null) {
				fragment.shouldShowRequestPermissionRationale(permission)
			} else {
				ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
			}
		}
		
		if (shouldShowRationale) {
			showPermissionExplanationDialog(permissions, title, message)
		} else {
			// Request permissions directly
			permissionLauncher?.launch(permissions)
		}
	}
	
	/**
	 * Shows explanation dialog before requesting permissions
	 */
	private fun showPermissionExplanationDialog(
		permissions: Array<String>,
		title: String,
		message: String
	) {
		MaterialAlertDialogBuilder(activity)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton("Grant Permissions") { _, _ ->
				permissionLauncher?.launch(permissions)
			}
			.setNegativeButton("Cancel") { dialog, _ ->
				dialog.dismiss()
			}
			.setCancelable(false)
			.show()
	}
	
	/**
	 * Handles permission request results
	 */
	private fun handlePermissionResults(permissions: Map<String, Boolean>) {
		val deniedPermissions = permissions.filter { !it.value }.keys
		
		if (deniedPermissions.isEmpty()) {
			// All permissions granted
			onAllPermissionsGranted()
		} else {
			// Some permissions denied
			val permanentlyDenied = deniedPermissions.filter { permission ->
				!activity.shouldShowRequestPermissionRationale(permission)
			}
			
			if (permanentlyDenied.isNotEmpty()) {
				showSettingsDialog(permanentlyDenied)
			} else {
				showPermissionDeniedDialog(deniedPermissions.toList())
			}
		}
	}
	
	/**
	 * Shows dialog to go to settings for permanently denied permissions
	 */
	private fun showSettingsDialog(permanentlyDenied: List<String>) {
		val message = "Some permissions were permanently denied. " +
				"Please enable them in app settings to use all features:\n\n" +
				permanentlyDenied.joinToString("\n") { getPermissionDescription(it) }
		
		MaterialAlertDialogBuilder(activity)
			.setTitle("Permissions Required")
			.setMessage(message)
			.setPositiveButton("Open Settings") { _, _ ->
				openAppSettings()
			}
			.setNegativeButton("Cancel") { dialog, _ ->
				dialog.dismiss()
			}
			.setCancelable(false)
			.show()
	}
	
	/**
	 * Shows dialog for temporarily denied permissions
	 */
	private fun showPermissionDeniedDialog(deniedPermissions: List<String>) {
		val message = "Some permissions were denied. " +
				"These features may not work properly:\n\n" +
				deniedPermissions.joinToString("\n") { getPermissionDescription(it) }
		
		MaterialAlertDialogBuilder(activity)
			.setTitle("Permissions Denied")
			.setMessage(message)
			.setPositiveButton("Try Again") { _, _ ->
				permissionLauncher?.launch(deniedPermissions.toTypedArray())
			}
			.setNegativeButton("Continue") { dialog, _ ->
				dialog.dismiss()
			}
			.show()
	}
	
	/**
	 * Opens app settings
	 */
	private fun openAppSettings() {
		val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
			data = Uri.fromParts("package", context.packageName, null)
		}
		settingsLauncher?.launch(intent)
	}
	
	/**
	 * Handles settings result
	 */
	private fun handleSettingsResult(resultCode: Int) {
		if (resultCode == Activity.RESULT_OK) {
			// Check permissions again after returning from settings
			checkAllPermissions()
		}
	}
	
	/**
	 * Gets user-friendly description for permission
	 */
	private fun getPermissionDescription(permission: String): String {
		return when (permission) {
			INTERNET -> "• Internet: Required for app functionality"
			FOREGROUND_SERVICE -> "• Foreground Service: Required for background protection"
			QUERY_ALL_PACKAGES -> "• Query All Packages: Required to monitor installed apps"
			ACCESS_FINE_LOCATION -> "• Location: Required for location-based rules"
			PACKAGE_USAGE_STATS -> "• Usage Stats: Required to track app usage"
			BIND_VPN_SERVICE -> "• VPN Service: Required for network filtering"
			BIND_ACCESSIBILITY_SERVICE -> "• Accessibility Service: Required for content monitoring"
			RECEIVE_BOOT_COMPLETED -> "• Boot Completed: Required to restart after reboot"
			else -> "• $permission"
		}
	}
	
	/**
	 * Checks all permissions and requests missing ones
	 */
	fun checkAllPermissions() {
		val allPermissions = CORE_PERMISSIONS + APP_MONITORING_PERMISSIONS + 
				LOCATION_PERMISSIONS + SERVICE_PERMISSIONS
		
		val missingPermissions = allPermissions.filter { permission ->
			ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
		}
		
		if (missingPermissions.isNotEmpty()) {
			requestPermissionsWithExplanation(
				permissions = missingPermissions.toTypedArray(),
				title = "Required Permissions",
				message = "InternetGuard Pro needs these permissions to provide full protection. " +
						"All data processing happens locally on your device."
			)
		} else {
			onAllPermissionsGranted()
		}
	}
	
	/**
	 * Callback for when all permissions are granted
	 */
	private fun onAllPermissionsGranted() {
		// This can be overridden by the calling activity/fragment
		// to perform actions after permissions are granted
	}
}

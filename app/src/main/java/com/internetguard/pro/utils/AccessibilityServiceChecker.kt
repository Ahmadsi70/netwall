package com.internetguard.pro.utils

import android.content.ComponentName
import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.internetguard.pro.services.KeywordAccessibilityService

/**
 * Utility class for checking accessibility service status
 */
object AccessibilityServiceChecker {
	
	/**
	 * Checks if the keyword accessibility service is enabled
	 */
	fun isServiceEnabled(context: Context): Boolean {
		val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
		val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
			0x000000FF
		)
		
		val serviceName = ComponentName(context, KeywordAccessibilityService::class.java)
		
		return enabledServices.any { serviceInfo ->
			serviceInfo.resolveInfo.serviceInfo.packageName == serviceName.packageName &&
			serviceInfo.resolveInfo.serviceInfo.name == serviceName.className
		}
	}
	
	/**
	 * Gets the service status as a human-readable string
	 */
	fun getServiceStatus(context: Context): String {
		return if (isServiceEnabled(context)) {
			"Enabled"
		} else {
			"Disabled"
		}
	}
	
	/**
	 * Gets detailed service information
	 */
	fun getServiceInfo(context: Context): ServiceInfo {
		val isEnabled = isServiceEnabled(context)
		val status = getServiceStatus(context)
		
		return ServiceInfo(
			isEnabled = isEnabled,
			status = status,
			description = if (isEnabled) {
				"Accessibility service is enabled and keywords are being blocked"
			} else {
				"Accessibility service is disabled. Keywords are not being blocked"
			}
		)
	}
	
	/**
	 * Data class for service information
	 */
	data class ServiceInfo(
		val isEnabled: Boolean,
		val status: String,
		val description: String
	)
}

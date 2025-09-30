package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.internetguard.pro.R
import com.internetguard.pro.databinding.FragmentPrivacyPolicyBinding

/**
 * Privacy Policy fragment explaining data handling and privacy practices.
 * 
 * Provides detailed information about on-device processing, data storage,
 * and privacy commitments. No data collection or sharing is performed.
 */
class PrivacyPolicyFragment : Fragment() {
	
	private var _binding: FragmentPrivacyPolicyBinding? = null
	private val binding get() = _binding!!
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
		return binding.root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setupContent()
	}
	
	/**
	 * Sets up privacy policy content
	 */
	private fun setupContent() {
		// Set up scrollable content with privacy information
		binding.privacyContent.text = getPrivacyPolicyContent()
	}
	
	/**
	 * Gets the complete privacy policy content
	 */
	private fun getPrivacyPolicyContent(): String {
		return """
			PRIVACY POLICY
			InternetGuard Pro
			Last Updated: ${getCurrentDate()}
			
			OVERVIEW
			InternetGuard Pro is designed with privacy as a core principle. This app processes all data locally on your device and does not collect, store, or transmit any personal information to external servers.
			
			DATA PROCESSING
			
			1. LOCAL PROCESSING ONLY
			• All AI analysis, keyword filtering, and app monitoring happens on your device
			• No data is sent to external servers or third parties
			• No internet connection required for core functionality
			
			2. DATA STORAGE
			• All data is stored locally using encrypted SQLCipher database
			• Database is encrypted with a device-specific key
			• No cloud storage or external data synchronization
			
			3. DATA TYPES PROCESSED
			• App usage statistics (stored locally)
			• Keyword blacklist (stored locally)
			• Blocking rules and preferences (stored locally)
			• VPN connection logs (stored locally)
			
			PERMISSIONS EXPLAINED
			
			• INTERNET: Used only for checking app updates and security definitions
			• FOREGROUND_SERVICE: Required for continuous background protection
			• QUERY_ALL_PACKAGES: Needed to monitor installed applications
			• ACCESS_FINE_LOCATION: Used for location-based blocking rules
			• PACKAGE_USAGE_STATS: Required to track app usage patterns
			• BIND_VPN_SERVICE: Essential for network traffic filtering
			• BIND_ACCESSIBILITY_SERVICE: Needed for content monitoring
			• RECEIVE_BOOT_COMPLETED: Allows protection to restart after reboot
			
			SECURITY MEASURES
			
			1. ENCRYPTION
			• All local data is encrypted using SQLCipher
			• Encryption keys are stored in Android Keystore
			• Biometric authentication for sensitive operations
			
			2. ACCESS CONTROLS
			• Biometric authentication required for settings access
			• Biometric authentication required for keyword list modification
			• Biometric authentication required for disabling blocks
			
			3. DATA RETENTION
			• Data is retained only as long as the app is installed
			• Users can clear all data at any time
			• No data is retained after app uninstallation
			
			THIRD-PARTY SERVICES
			
			• No third-party analytics or tracking services
			• No advertising networks or data brokers
			• No social media integrations that share data
			• No external AI services or cloud processing
			
			USER RIGHTS
			
			1. DATA ACCESS
			• Users can view all stored data within the app
			• Data export functionality available
			• Data import functionality available
			
			2. DATA CONTROL
			• Users can modify or delete any stored data
			• Complete data deletion option available
			• No data recovery after deletion
			
			3. TRANSPARENCY
			• Open source code available for review
			• Clear explanation of all permissions
			• Regular security audits and updates
			
			CHILDREN'S PRIVACY
			
			• App is suitable for all ages
			• No additional data collection for children
			• Parental controls available
			• No targeted advertising or tracking
			
			UPDATES AND CHANGES
			
			• Privacy policy updates will be notified within the app
			• No retroactive changes to data handling
			• Users can opt out of updates
			• Previous versions remain available
			
			CONTACT INFORMATION
			
			For privacy-related questions or concerns:
			• Email: privacy@internetguardpro.com
			• Support: support@internetguardpro.com
			• GitHub: github.com/internetguardpro
			
			COMPLIANCE
			
			• GDPR compliant (no data collection)
			• CCPA compliant (no data sale)
			• COPPA compliant (no child data collection)
			• Local privacy laws respected
			
			This privacy policy demonstrates our commitment to user privacy and data protection. InternetGuard Pro is designed to provide powerful internet protection while maintaining complete user privacy and data sovereignty.
		""".trimIndent()
	}
	
	/**
	 * Gets current date for privacy policy
	 */
	private fun getCurrentDate(): String {
		val formatter = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
		return formatter.format(java.util.Date())
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

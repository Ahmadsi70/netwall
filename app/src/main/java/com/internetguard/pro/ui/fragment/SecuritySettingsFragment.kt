package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.internetguard.pro.R
import com.internetguard.pro.databinding.FragmentSecuritySettingsBinding
import com.internetguard.pro.ui.viewmodel.SecuritySettingsViewModel
import com.internetguard.pro.data.model.EncryptionStatus
import com.internetguard.pro.data.model.PrivacyStatus

/**
 * Security Settings fragment for managing security and privacy features.
 * 
 * Provides controls for biometric authentication, database encryption,
 * advanced privacy features, and security status monitoring.
 */
class SecuritySettingsFragment : Fragment() {
	
	private var _binding: FragmentSecuritySettingsBinding? = null
	private val binding get() = _binding!!
	
	private lateinit var viewModel: SecuritySettingsViewModel
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentSecuritySettingsBinding.inflate(inflater, container, false)
		return binding.root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setupViewModel()
		setupClickListeners()
		observeViewModel()
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[SecuritySettingsViewModel::class.java]
	}
	
	/**
	 * Sets up click listeners
	 */
	private fun setupClickListeners() {
		// Biometric authentication
		binding.biometricCard.setOnClickListener {
			showBiometricSettingsDialog()
		}
		
		// Database encryption
		binding.encryptionCard.setOnClickListener {
			showEncryptionSettingsDialog()
		}
		
		// Advanced privacy
		binding.privacyCard.setOnClickListener {
			showAdvancedPrivacyDialog()
		}
		
		// Security status
		binding.securityStatusCard.setOnClickListener {
			showSecurityStatusDialog()
		}
		
		// Clear all data
		binding.clearDataCard.setOnClickListener {
			showClearDataDialog()
		}
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.isBiometricEnabled.observe(viewLifecycleOwner) { isEnabled ->
			binding.biometricSwitch.isChecked = isEnabled
		}
		
		viewModel.isBiometricAvailable.observe(viewLifecycleOwner) { isAvailable ->
			binding.biometricSwitch.isEnabled = isAvailable
			if (!isAvailable) {
				binding.biometricDescription.text = "Biometric authentication is not available on this device"
			}
		}
		
		viewModel.encryptionStatus.observe(viewLifecycleOwner) { status ->
			updateEncryptionStatus(status)
		}
		
		viewModel.privacyStatus.observe(viewLifecycleOwner) { status ->
			updatePrivacyStatus(status)
		}
		
		viewModel.securityScore.observe(viewLifecycleOwner) { score ->
			binding.securityScoreText.text = "$score/100"
			updateSecurityScoreColor(score)
		}
	}
	
	/**
	 * Shows biometric settings dialog
	 */
	private fun showBiometricSettingsDialog() {
		val isEnabled = viewModel.isBiometricEnabled.value ?: false
		val isAvailable = viewModel.isBiometricAvailable.value ?: false
		
		if (!isAvailable) {
			MaterialAlertDialogBuilder(requireContext())
				.setTitle("Biometric Authentication")
				.setMessage("Biometric authentication is not available on this device. Please set up biometric authentication in device settings.")
				.setPositiveButton("OK", null)
				.show()
			return
		}
		
		val message = if (isEnabled) {
			"Biometric authentication is currently enabled. This provides additional security for sensitive operations."
		} else {
			"Enable biometric authentication to secure access to settings, keyword lists, and other sensitive features."
		}
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Biometric Authentication")
			.setMessage(message)
			.setPositiveButton(if (isEnabled) "Disable" else "Enable") { _, _ ->
				if (isEnabled) {
					viewModel.disableBiometric()
				} else {
					viewModel.enableBiometric()
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows encryption settings dialog
	 */
	private fun showEncryptionSettingsDialog() {
		val status = viewModel.encryptionStatus.value
		val message = if (status?.isEncrypted == true) {
			"Database encryption is enabled with ${status.passwordStrength}% password strength. " +
			"All data is encrypted using SQLCipher."
		} else {
			"Database encryption is not enabled. Enable encryption to protect your data."
		}
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Database Encryption")
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show()
	}
	
	/**
	 * Shows advanced privacy dialog
	 */
	private fun showAdvancedPrivacyDialog() {
		val status = viewModel.privacyStatus.value
		val message = buildString {
			appendLine("Advanced Privacy Status:")
			appendLine("• Private DNS: ${if (status?.privateDNSEnabled == true) "Enabled" else "Disabled"}")
			appendLine("• VPN Passthrough: ${if (status?.vpnPassthroughEnabled == true) "Enabled" else "Disabled"}")
			appendLine("• Network Monitoring: ${if (status?.networkMonitoringEnabled == true) "Enabled" else "Disabled"}")
			appendLine("• DNS over HTTPS: ${if (status?.dnsOverHTTPSEnabled == true) "Enabled" else "Disabled"}")
			appendLine("• DNS over TLS: ${if (status?.dnsOverTLSEnabled == true) "Enabled" else "Disabled"}")
		}
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Advanced Privacy")
			.setMessage(message)
			.setPositiveButton("Configure") { _, _ ->
				// Navigate to advanced privacy settings
			}
			.setNegativeButton("OK", null)
			.show()
	}
	
	/**
	 * Shows security status dialog
	 */
	private fun showSecurityStatusDialog() {
		val score = viewModel.securityScore.value ?: 0
		val message = "Security Score: $score/100\n\n" +
				"This score is based on:\n" +
				"• Biometric authentication status\n" +
				"• Database encryption strength\n" +
				"• Privacy settings configuration\n" +
				"• Permission grants\n\n" +
				"Higher scores indicate better security."
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Security Status")
			.setMessage(message)
			.setPositiveButton("OK", null)
			.show()
	}
	
	/**
	 * Shows clear data confirmation dialog
	 */
	private fun showClearDataDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Clear All Data")
			.setMessage("This will permanently delete all app data including:\n\n" +
					"• All blocking rules\n" +
					"• Keyword blacklist\n" +
					"• Usage statistics\n" +
					"• Settings and preferences\n\n" +
					"This action cannot be undone.")
			.setPositiveButton("Clear All Data") { _, _ ->
				viewModel.clearAllData()
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Updates encryption status display
	 */
	private fun updateEncryptionStatus(status: EncryptionStatus?) {
		if (status != null) {
			binding.encryptionStatusText.text = if (status.isEncrypted) {
				"Enabled (${status.passwordStrength}% strength)"
			} else {
				"Disabled"
			}
		}
	}
	
	/**
	 * Updates privacy status display
	 */
	private fun updatePrivacyStatus(status: PrivacyStatus?) {
		if (status != null) {
			val enabledFeatures = listOfNotNull(
				if (status.privateDNSEnabled) "Private DNS" else null,
				if (status.vpnPassthroughEnabled) "VPN Passthrough" else null,
				if (status.networkMonitoringEnabled) "Network Monitoring" else null,
				if (status.dnsOverHTTPSEnabled) "DNS over HTTPS" else null,
				if (status.dnsOverTLSEnabled) "DNS over TLS" else null
			)
			
			binding.privacyStatusText.text = if (enabledFeatures.isNotEmpty()) {
				enabledFeatures.joinToString(", ")
			} else {
				"No advanced features enabled"
			}
		}
	}
	
	/**
	 * Updates security score color based on score
	 */
	private fun updateSecurityScoreColor(score: Int) {
		val color = when {
			score >= 80 -> R.color.success_color
			score >= 60 -> R.color.warning_color
			else -> R.color.error_color
		}
		binding.securityScoreText.setTextColor(requireContext().getColor(color))
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

package com.internetguard.pro.ui.fragment

import android.content.Intent
import android.content.Context
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.internetguard.pro.R
import com.internetguard.pro.databinding.FragmentSettingsBinding
import com.internetguard.pro.ui.viewmodel.SettingsViewModel
import com.internetguard.pro.security.PermissionManager
import com.internetguard.pro.utils.AccessibilityServiceChecker

/**
 * Settings fragment for app configuration and preferences.
 * 
 * Provides settings for themes, notifications, privacy,
 * and other app preferences with Material Design 3.
 */
class SettingsFragment : Fragment() {
	
	private var _binding: FragmentSettingsBinding? = null
	private val binding get() = _binding!!
	
	private lateinit var viewModel: SettingsViewModel
	private lateinit var permissionManager: PermissionManager
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentSettingsBinding.inflate(inflater, container, false)
		return binding.root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		// 🌍 LANGUAGE: Apply system language to fragment
		val systemLanguage = com.internetguard.pro.utils.LanguageManager.getBestMatchingLanguage(requireContext())
		com.internetguard.pro.utils.LanguageManager.applyLanguage(requireContext(), systemLanguage)
		
		setupViewModel()
		setupPermissionManager()
		setupClickListeners()
		observeViewModel()
		checkAccessibilityStatus()
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
	}
	
	/**
	 * Sets up PermissionManager
	 */
	private fun setupPermissionManager() {
		permissionManager = PermissionManager(requireActivity())
		permissionManager.initialize()
	}
	
	/**
	 * Sets up click listeners
	 */
	private fun setupClickListeners() {
		// Theme settings
		binding.themeCard.setOnClickListener {
			showThemeDialog()
		}
		
		// Language settings
		binding.languageCard.setOnClickListener {
			showLanguageDialog()
		}
		
		// Notification settings
		binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
			viewModel.setNotificationsEnabled(isChecked)
		}
		
		// Accessibility settings
		binding.accessibilityCard.setOnClickListener {
			handleAccessibilityCardClick()
		}
		
		// Privacy settings
		binding.privacyCard.setOnClickListener {
			showPrivacyDialog()
		}

		// Cloud moderation settings (AI API) - Card removed from layout
		
		// About settings
		binding.aboutCard.setOnClickListener {
			showAboutDialog()
		}
		
		// Export data
		binding.exportDataCard.setOnClickListener {
			viewModel.exportData()
		}
		
		// Import data
		binding.importDataCard.setOnClickListener {
			viewModel.importData()
		}
		
		// Clear data
		binding.clearDataCard.setOnClickListener {
			showClearDataDialog()
		}
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.isDarkTheme.observe(viewLifecycleOwner) { isDark ->
			updateThemeUI(isDark)
		}
		
		viewModel.isNotificationsEnabled.observe(viewLifecycleOwner) { isEnabled ->
			binding.notificationSwitch.isChecked = isEnabled
		}
		
		viewModel.currentLanguage.observe(viewLifecycleOwner) { language ->
			binding.languageValueText.text = language
		}
		
		viewModel.exportSuccess.observe(viewLifecycleOwner) { success ->
			if (success) {
				showSuccessMessage("Data exported successfully")
			} else {
				showErrorMessage("Failed to export data")
			}
		}
		
		viewModel.importSuccess.observe(viewLifecycleOwner) { success ->
			if (success) {
				showSuccessMessage("Data imported successfully")
			} else {
				showErrorMessage("Failed to import data")
			}
		}
	}
	
	/**
	 * Shows theme selection dialog
	 */
	private fun showThemeDialog() {
		val themes = arrayOf("Light", "Dark", "System")
		val currentTheme = viewModel.getCurrentTheme()
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Choose Theme")
			.setSingleChoiceItems(themes, currentTheme) { dialog, which ->
				viewModel.setTheme(which)
				dialog.dismiss()
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows language selection dialog
	 */
	private fun showLanguageDialog() {
		val languages = arrayOf("English", "Persian")
		val currentLanguage = viewModel.getCurrentLanguage()
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_choose_language_title))
			.setSingleChoiceItems(languages, currentLanguage) { dialog, which ->
				viewModel.setLanguage(which)
				dialog.dismiss()
			}
			.setNegativeButton(getString(R.string.cancel), null)
			.show()
	}
	
	/**
	 * Shows privacy settings dialog
	 */
	private fun showPrivacyDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_privacy_settings_title))
			.setMessage(getString(R.string.dialog_privacy_settings_message))
			.setPositiveButton(getString(R.string.ok), null)
			.show()
	}
	
	/**
	 * Shows about dialog
	 */
	private fun showAboutDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_about_title))
			.setMessage(getString(R.string.dialog_about_message))
			.setPositiveButton(getString(R.string.ok), null)
			.show()
	}

	private fun showCloudModerationDialog() {
		val view = layoutInflater.inflate(R.layout.dialog_cloud_moderation, null)
        val optInSwitch = view.findViewById<Switch>(R.id.switch_opt_in)

		// Load existing values
		val prefs = requireContext().getSharedPreferences("cloud_moderation_prefs", Context.MODE_PRIVATE)
        optInSwitch.isChecked = prefs.getBoolean("opt_in", true)

		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_cloud_ai_moderation_title))
			.setView(view)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
				prefs.edit()
                    .putBoolean("opt_in", optInSwitch.isChecked)
					.apply()
				
				val statusMessage = if (optInSwitch.isChecked) {
					getString(R.string.toast_cloud_ai_moderation_enabled)
				} else {
					getString(R.string.toast_cloud_ai_moderation_disabled)
				}
				Toast.makeText(requireContext(), statusMessage, Toast.LENGTH_SHORT).show()
			}
			.setNegativeButton(getString(R.string.cancel), null)
			.show()
	}


	
	/**
	 * Shows clear data confirmation dialog
	 */
	private fun showClearDataDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_clear_all_data_title))
			.setMessage(getString(R.string.dialog_clear_all_data_message))
			.setPositiveButton(getString(R.string.button_clear)) { _, _ ->
				viewModel.clearAllData()
			}
			.setNegativeButton(getString(R.string.cancel), null)
			.show()
	}
	
	/**
	 * Updates theme UI
	 */
	private fun updateThemeUI(isDark: Boolean) {
		val themeText = if (isDark) "Dark" else "Light"
		binding.themeValueText.text = themeText
	}
	
	/**
	 * Shows success message
	 */
	private fun showSuccessMessage(message: String) {
		// Implementation would show a success toast or snackbar
	}
	
	/**
	 * Shows error message
	 */
	private fun showErrorMessage(message: String) {
		// Implementation would show an error toast or snackbar
	}
	
	/**
	 * Handles accessibility card click
	 */
	private fun handleAccessibilityCardClick() {
		if (permissionManager.isAccessibilityServiceEnabled()) {
			// Service is enabled, show status dialog
			showAccessibilityStatusDialog()
		} else {
			// Service is disabled, request permission
			permissionManager.requestAccessibilityService()
		}
	}
	
	/**
	 * Checks accessibility service status and updates UI
	 */
	private fun checkAccessibilityStatus() {
		val serviceInfo = AccessibilityServiceChecker.getServiceInfo(requireContext())
		binding.accessibilityStatusText.text = serviceInfo.description
	}
	
	/**
	 * Shows accessibility status dialog
	 */
	private fun showAccessibilityStatusDialog() {
		val serviceInfo = AccessibilityServiceChecker.getServiceInfo(requireContext())
		
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_service_enabled_title))
			.setMessage(getString(R.string.dialog_service_enabled_message))
			.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
				dialog.dismiss()
			}
			.setNeutralButton("Open Settings") { _, _ ->
				permissionManager.openAccessibilitySettings()
			}
			.show()
	}
	
	override fun onResume() {
		super.onResume()
		// Check accessibility status when returning from settings
		checkAccessibilityStatus()
		
		// Check if user has enabled the service after going to settings
		if (permissionManager.checkAccessibilityServiceAfterSettings()) {
			// Show success message
			showAccessibilitySuccessMessage()
		}
	}
	
	/**
	 * Shows success message when accessibility service is enabled
	 */
	private fun showAccessibilitySuccessMessage() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(getString(R.string.dialog_service_enabled_success_title))
			.setMessage(getString(R.string.dialog_service_enabled_success_message))
			.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
				dialog.dismiss()
			}
			.setCancelable(false)
			.show()
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

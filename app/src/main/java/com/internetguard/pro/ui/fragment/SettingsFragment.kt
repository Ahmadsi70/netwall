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
		
		setupViewModel()
		setupClickListeners()
		observeViewModel()
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
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
			.setTitle("Choose Language")
			.setSingleChoiceItems(languages, currentLanguage) { dialog, which ->
				viewModel.setLanguage(which)
				dialog.dismiss()
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows privacy settings dialog
	 */
	private fun showPrivacyDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Privacy Settings")
			.setMessage("InternetGuard Pro respects your privacy. All data is stored locally and never shared with third parties.")
			.setPositiveButton("OK", null)
			.show()
	}
	
	/**
	 * Shows about dialog
	 */
	private fun showAboutDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("About InternetGuard Pro")
			.setMessage("Version 1.0.0\n\nA powerful internet guard app with AI-powered suggestions and advanced blocking features.")
			.setPositiveButton("OK", null)
			.show()
	}

	private fun showCloudModerationDialog() {
		val view = layoutInflater.inflate(R.layout.dialog_cloud_moderation, null)
        val optInSwitch = view.findViewById<Switch>(R.id.switch_opt_in)

		// Load existing values
		val prefs = requireContext().getSharedPreferences("cloud_moderation_prefs", Context.MODE_PRIVATE)
        optInSwitch.isChecked = prefs.getBoolean("opt_in", true)

		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Cloud AI Moderation")
			.setView(view)
            .setPositiveButton("Save") { _, _ ->
				prefs.edit()
                    .putBoolean("opt_in", optInSwitch.isChecked)
					.apply()
				
				val status = if (optInSwitch.isChecked) "enabled" else "disabled"
				Toast.makeText(requireContext(), "✅ Cloud AI Moderation $status", Toast.LENGTH_SHORT).show()
			}
			.setNegativeButton("Cancel", null)
			.show()
	}


	
	/**
	 * Shows clear data confirmation dialog
	 */
	private fun showClearDataDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle("Clear All Data")
			.setMessage("This will permanently delete all your data including rules, keywords, and statistics. This action cannot be undone.")
			.setPositiveButton("Clear") { _, _ ->
				viewModel.clearAllData()
			}
			.setNegativeButton("Cancel", null)
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
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

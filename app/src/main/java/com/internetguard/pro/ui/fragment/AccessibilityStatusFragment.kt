package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.internetguard.pro.R
import com.internetguard.pro.databinding.FragmentAccessibilityStatusBinding
import com.internetguard.pro.security.PermissionManager
import com.internetguard.pro.utils.AccessibilityServiceChecker
import kotlinx.coroutines.launch

/**
 * Fragment for displaying accessibility service status and management
 */
class AccessibilityStatusFragment : Fragment() {
	
	private var _binding: FragmentAccessibilityStatusBinding? = null
	private val binding get() = _binding!!
	
	private lateinit var permissionManager: PermissionManager
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAccessibilityStatusBinding.inflate(inflater, container, false)
		return binding.root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		// 🌍 LANGUAGE: Apply system language to fragment
		val systemLanguage = com.internetguard.pro.utils.LanguageManager.getBestMatchingLanguage(requireContext())
		com.internetguard.pro.utils.LanguageManager.applyLanguage(requireContext(), systemLanguage)
		
		permissionManager = PermissionManager(requireActivity())
		permissionManager.initialize()
		
		setupUI()
		checkAccessibilityStatus()
	}
	
	private fun setupUI() {
		binding.checkStatusButton.setOnClickListener {
			checkAccessibilityStatus()
		}
		
		binding.enableServiceButton.setOnClickListener {
			permissionManager.requestAccessibilityService()
		}
		
		binding.openSettingsButton.setOnClickListener {
			permissionManager.openAccessibilitySettings()
		}
		
		// Guide button functionality can be added later if needed
	}
	
	private fun checkAccessibilityStatus() {
		try {
			lifecycleScope.launch {
				try {
					val serviceInfo = AccessibilityServiceChecker.getServiceInfo(requireContext())
					
					// Update UI based on service status
					binding.serviceStatusText.text = serviceInfo.status
					binding.serviceDescriptionText.text = serviceInfo.description
					
					// Update button visibility
					if (serviceInfo.isEnabled) {
						binding.enableServiceButton.visibility = View.GONE
						binding.openSettingsButton.visibility = View.VISIBLE
						binding.statusIndicator.setBackgroundColor(
							requireContext().getColor(android.R.color.holo_green_light)
						)
					} else {
						binding.enableServiceButton.visibility = View.VISIBLE
						binding.openSettingsButton.visibility = View.GONE
						binding.statusIndicator.setBackgroundColor(
							requireContext().getColor(android.R.color.holo_red_light)
						)
					}
				} catch (e: Exception) {
					android.util.Log.e("AccessibilityStatusFragment", "Error checking accessibility status: ${e.message}", e)
					// Set default values on error
					try {
						binding.serviceStatusText.text = "Error"
						binding.serviceDescriptionText.text = "Unable to check service status"
						binding.enableServiceButton.visibility = View.VISIBLE
						binding.openSettingsButton.visibility = View.GONE
					} catch (e2: Exception) {
						android.util.Log.e("AccessibilityStatusFragment", "Error setting default values: ${e2.message}", e2)
					}
				}
			}
		} catch (e: Exception) {
			android.util.Log.e("AccessibilityStatusFragment", "Error in checkAccessibilityStatus: ${e.message}", e)
		}
	}
	
	override fun onResume() {
		super.onResume()
		// Check status when returning from settings
		checkAccessibilityStatus()
		
		// Check if user has enabled the service after going to settings
		if (permissionManager.checkAccessibilityServiceAfterSettings()) {
			// Show success message
			showSuccessMessage()
		}
	}
	
	private fun showSuccessMessage() {
		try {
			androidx.appcompat.app.AlertDialog.Builder(requireContext())
				.setTitle(getString(R.string.dialog_service_enabled_success_title))
				.setMessage(getString(R.string.dialog_service_enabled_success_message))
				.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
					dialog.dismiss()
				}
				.setCancelable(false)
				.show()
		} catch (e: Exception) {
			android.util.Log.e("AccessibilityStatusFragment", "Error showing success message: ${e.message}", e)
		}
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}

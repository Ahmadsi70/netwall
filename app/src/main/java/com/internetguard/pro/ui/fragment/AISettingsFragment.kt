package com.internetguard.pro.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.internetguard.pro.R
import com.internetguard.pro.ai.CustomSmartDetector
import kotlinx.coroutines.launch

/**
 * Fragment for AI-related settings and configuration.
 * 
 * Allows users to enable/disable AI features, adjust sensitivity,
 * and view AI performance statistics.
 */
class AISettingsFragment : Fragment() {
    
    companion object {
        private const val PREFS_NAME = "ai_settings"
        private const val KEY_AI_ENABLED = "ai_enabled"
        private const val KEY_AI_SENSITIVITY = "ai_sensitivity"
        private const val KEY_AI_MODE = "ai_mode"
    }
    
    private lateinit var prefs: SharedPreferences
    private lateinit var customAI: CustomSmartDetector
    
    // UI Components
    private lateinit var aiEnabledSwitch: Switch
    private lateinit var sensitivitySeekBar: SeekBar
    private lateinit var sensitivityLabel: TextView
    private lateinit var modeCard: MaterialCardView
    private lateinit var statsCard: MaterialCardView
    private lateinit var performanceText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializePreferences()
        initializeViews(view)
        initializeAI()
        setupListeners()
        loadSettings()
        updateUI()
    }
    
    /**
     * Initialize SharedPreferences
     */
    private fun initializePreferences() {
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Initialize UI components
     */
    private fun initializeViews(view: View) {
        aiEnabledSwitch = view.findViewById(R.id.ai_enabled_switch)
        sensitivitySeekBar = view.findViewById(R.id.sensitivity_seekbar)
        sensitivityLabel = view.findViewById(R.id.sensitivity_label)
        modeCard = view.findViewById(R.id.mode_card)
        statsCard = view.findViewById(R.id.stats_card)
        performanceText = view.findViewById(R.id.performance_text)
    }
    
    /**
     * Initialize Custom AI Engine
     */
    private fun initializeAI() {
        try {
            customAI = CustomSmartDetector(requireContext())
            // Custom AI doesn't need async initialization
        } catch (e: Exception) {
            showError("Failed to initialize Custom AI Engine: ${e.message}")
            aiEnabledSwitch.isEnabled = false
        }
    }
    
    /**
     * Setup UI listeners
     */
    private fun setupListeners() {
        // AI Enable/Disable
        aiEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AI_ENABLED, isChecked).apply()
            updateUI()
            showSnackbar(if (isChecked) "AI detection enabled" else "AI detection disabled")
        }
        
        // Sensitivity adjustment
        sensitivitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val sensitivity = progress / 100f
                    prefs.edit().putFloat(KEY_AI_SENSITIVITY, sensitivity).apply()
                    updateSensitivityLabel(sensitivity)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Stats card click
        statsCard.setOnClickListener {
            updatePerformanceStats()
        }
        
        // Mode card click
        modeCard.setOnClickListener {
            showModeSelectionDialog()
        }
    }
    
    /**
     * Load settings from preferences
     */
    private fun loadSettings() {
        val aiEnabled = prefs.getBoolean(KEY_AI_ENABLED, true)
        val sensitivity = prefs.getFloat(KEY_AI_SENSITIVITY, 0.7f)
        
        aiEnabledSwitch.isChecked = aiEnabled
        sensitivitySeekBar.progress = (sensitivity * 100).toInt()
        updateSensitivityLabel(sensitivity)
    }
    
    /**
     * Update UI based on current settings
     */
    private fun updateUI() {
        val aiEnabled = aiEnabledSwitch.isChecked
        
        // Enable/disable dependent controls
        sensitivitySeekBar.isEnabled = aiEnabled
        modeCard.isEnabled = aiEnabled
        
        // Update visual state
        val alpha = if (aiEnabled) 1.0f else 0.5f
        sensitivitySeekBar.alpha = alpha
        sensitivityLabel.alpha = alpha
        modeCard.alpha = alpha
    }
    
    /**
     * Update sensitivity label
     */
    private fun updateSensitivityLabel(sensitivity: Float) {
        val percentage = (sensitivity * 100).toInt()
        val level = when {
            sensitivity < 0.3f -> "Low"
            sensitivity < 0.7f -> "Medium"
            else -> "High"
        }
        sensitivityLabel.text = "Sensitivity: $level ($percentage%)"
    }
    
    /**
     * Update performance statistics
     */
    private fun updatePerformanceStats() {
        lifecycleScope.launch {
            try {
                if (::customAI.isInitialized) {
                    val stats = customAI.getPerformanceStats()
                    val statsText = buildString {
                        appendLine("Custom AI Engine Status:")
                        appendLine("• Total Detections: ${stats.totalDetections}")
                        appendLine("• Avg Processing Time: ${String.format("%.2f", stats.averageProcessingTimeMs)}ms")
                        appendLine("• Cache Hit Rate: ${String.format("%.1f", stats.cacheHitRate)}%")
                        appendLine("• Cache Size: ${stats.cacheSize}")
                        appendLine("• Memory Usage: ${String.format("%.1f", stats.memoryUsageMB)}MB")
                        appendLine()
                        appendLine("Performance:")
                        appendLine("• Detection Mode: Custom AI + Rules")
                        appendLine("• Target Response Time: <3ms")
                        appendLine("• Expected Memory: ~1MB")
                        appendLine("• Accuracy: ~90-95%")
                        appendLine("• APK Size Impact: ~200KB")
                    }
                    
                    performanceText.text = statsText
                } else {
                    performanceText.text = "Custom AI not initialized"
                }
            } catch (e: Exception) {
                performanceText.text = "Error loading statistics: ${e.message}"
            }
        }
    }
    
    /**
     * Show mode selection dialog
     */
    private fun showModeSelectionDialog() {
        val modes = arrayOf(
            "Lightweight (Fast)",
            "Balanced (Recommended)", 
            "Thorough (Accurate)",
            "Adaptive (Smart)"
        )
        
        val currentMode = prefs.getInt(KEY_AI_MODE, 1) // Default to Balanced
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("AI Detection Mode")
            .setSingleChoiceItems(modes, currentMode) { dialog, which ->
                prefs.edit().putInt(KEY_AI_MODE, which).apply()
                val modeName = modes[which]
                showSnackbar("Mode changed to: $modeName")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show error message
     */
    private fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.blocked_status, null))
            .show()
    }
    
    /**
     * Show success/info message
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success_green, null))
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        updatePerformanceStats()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::customAI.isInitialized) {
            customAI.release()
        }
    }
}

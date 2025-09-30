package com.internetguard.pro.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.internetguard.pro.R
import com.internetguard.pro.ai.IntelligentKeywordSuggestionEngine
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.model.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Dialog for selecting keywords for a specific app.
 * 
 * Allows users to:
 * - Enter custom keywords
 * - Get AI-powered suggestions
 * - Select from suggested keywords
 * - Save keywords for the app
 */
class AppKeywordSelectionDialog : DialogFragment() {
    
    companion object {
        private const val ARG_APP_INFO = "app_info"
        private const val ARG_EXISTING_KEYWORDS = "existing_keywords"
        
        fun newInstance(appInfo: AppInfo, existingKeywords: List<String> = emptyList()): AppKeywordSelectionDialog {
            val args = Bundle().apply {
                putParcelable(ARG_APP_INFO, appInfo)
                putStringArrayList(ARG_EXISTING_KEYWORDS, ArrayList(existingKeywords))
            }
            return AppKeywordSelectionDialog().apply { arguments = args }
        }
    }
    
    private lateinit var appInfo: AppInfo
    private lateinit var existingKeywords: List<String>
    private lateinit var aiSuggestionEngine: IntelligentKeywordSuggestionEngine
    
    // UI Components
    private lateinit var appIcon: ImageView
    private lateinit var appName: TextView
    private lateinit var packageName: TextView
    private lateinit var keywordsInput: TextInputEditText
    private lateinit var aiSuggestionInput: TextInputEditText
    private lateinit var generateSuggestionsButton: MaterialButton
    private lateinit var suggestionsLabel: TextView
    private lateinit var suggestionsChips: ChipGroup
    private lateinit var cancelButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    
    // Callbacks
    private var onKeywordsSaved: ((List<String>) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            appInfo = args.getParcelable(ARG_APP_INFO)!!
            existingKeywords = args.getStringArrayList(ARG_EXISTING_KEYWORDS) ?: emptyList()
        }
        aiSuggestionEngine = IntelligentKeywordSuggestionEngine()
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_app_keyword_selection, null)
        
        initViews(view)
        setupClickListeners()
        populateExistingKeywords()
        
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
    
    private fun initViews(view: View) {
        appIcon = view.findViewById(R.id.app_icon)
        appName = view.findViewById(R.id.app_name)
        packageName = view.findViewById(R.id.package_name)
        keywordsInput = view.findViewById(R.id.keywords_input)
        aiSuggestionInput = view.findViewById(R.id.ai_suggestion_input)
        generateSuggestionsButton = view.findViewById(R.id.generate_suggestions_button)
        suggestionsLabel = view.findViewById(R.id.suggestions_label)
        suggestionsChips = view.findViewById(R.id.suggestions_chips)
        cancelButton = view.findViewById(R.id.cancel_button)
        saveButton = view.findViewById(R.id.save_button)
        
        // Set app information
        appName.text = appInfo.appName
        packageName.text = appInfo.packageName
        appInfo.icon?.let { appIcon.setImageDrawable(it) }
            ?: appIcon.setImageResource(R.drawable.ic_app_default)
    }
    
    private fun setupClickListeners() {
        // Keywords input text watcher
        keywordsInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSaveButtonState()
            }
        })
        
        // Generate AI suggestions
        generateSuggestionsButton.setOnClickListener {
            generateAISuggestions()
        }
        
        // Cancel button
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        // Save button
        saveButton.setOnClickListener {
            saveKeywords()
        }
    }
    
    private fun populateExistingKeywords() {
        if (existingKeywords.isNotEmpty()) {
            val keywordsText = existingKeywords.joinToString(", ")
            keywordsInput.setText(keywordsText)
        }
        updateSaveButtonState()
    }
    
    private fun generateAISuggestions() {
        val inputText = aiSuggestionInput.text?.toString()?.trim()
        if (inputText.isNullOrEmpty()) {
            return
        }
        
        generateSuggestionsButton.isEnabled = false
        generateSuggestionsButton.text = "Generating..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val suggestions = aiSuggestionEngine.generateSuggestions(inputText)
                
                withContext(Dispatchers.Main) {
                    displaySuggestions(suggestions)
                    generateSuggestionsButton.isEnabled = true
                    generateSuggestionsButton.text = "Generate AI Suggestions"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    generateSuggestionsButton.isEnabled = true
                    generateSuggestionsButton.text = "Generate AI Suggestions"
                }
            }
        }
    }
    
    private fun displaySuggestions(suggestions: List<String>) {
        suggestionsChips.removeAllViews()
        
        if (suggestions.isNotEmpty()) {
            suggestionsLabel.visibility = View.VISIBLE
            
            suggestions.forEach { suggestion ->
                val chip = Chip(requireContext()).apply {
                    text = suggestion
                    isCheckable = true
                    isChecked = false
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            addSuggestionToKeywords(suggestion)
                        }
                    }
                }
                suggestionsChips.addView(chip)
            }
        } else {
            suggestionsLabel.visibility = View.GONE
        }
    }
    
    private fun addSuggestionToKeywords(suggestion: String) {
        val currentText = keywordsInput.text?.toString() ?: ""
        val keywords = if (currentText.isEmpty()) {
            suggestion
        } else {
            "$currentText, $suggestion"
        }
        keywordsInput.setText(keywords)
        keywordsInput.setSelection(keywords.length)
    }
    
    private fun updateSaveButtonState() {
        val hasKeywords = !keywordsInput.text?.toString()?.trim().isNullOrEmpty()
        saveButton.isEnabled = hasKeywords
    }
    
    private fun saveKeywords() {
        val keywordsText = keywordsInput.text?.toString()?.trim()
        if (keywordsText.isNullOrEmpty()) {
            return
        }
        
        val keywords = keywordsText.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        
        onKeywordsSaved?.invoke(keywords)
        dismiss()
    }
    
    fun setOnKeywordsSavedListener(listener: (List<String>) -> Unit) {
        onKeywordsSaved = listener
    }
}

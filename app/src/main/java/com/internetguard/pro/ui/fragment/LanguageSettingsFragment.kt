package com.internetguard.pro.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.internetguard.pro.databinding.FragmentLanguageSettingsBinding
import com.internetguard.pro.ui.adapter.LanguageAdapter
import com.internetguard.pro.ui.viewmodel.LanguageSettingsViewModel
import com.internetguard.pro.utils.LanguageManager

/**
 * Language Settings fragment for managing app language.
 * 
 * Allows users to select from supported languages and apply changes.
 * Supports RTL languages and provides language-specific features.
 */
class LanguageSettingsFragment : Fragment() {
    
    private var _binding: FragmentLanguageSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: LanguageSettingsViewModel
    private lateinit var languageAdapter: LanguageAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }
    
    /**
     * Sets up ViewModel
     */
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[LanguageSettingsViewModel::class.java]
    }
    
    /**
     * Sets up RecyclerView for language list
     */
    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter { languageCode ->
            viewModel.selectLanguage(languageCode)
        }
        
        binding.recyclerViewLanguages.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter
        }
    }
    
    /**
     * Sets up click listeners
     */
    private fun setupClickListeners() {
        binding.btnApplyLanguage.setOnClickListener {
            viewModel.applyLanguage()
        }
        
        binding.btnResetToSystem.setOnClickListener {
            viewModel.resetToSystemLanguage()
        }
    }
    
    /**
     * Observes ViewModel LiveData
     */
    private fun observeViewModel() {
        viewModel.supportedLanguages.observe(viewLifecycleOwner) { languages ->
            languageAdapter.submitList(languages)
        }
        
        viewModel.selectedLanguage.observe(viewLifecycleOwner) { languageCode ->
            languageAdapter.setSelectedLanguage(languageCode)
        }
        
        viewModel.currentLanguage.observe(viewLifecycleOwner) { languageCode ->
            updateLanguageInfo(languageCode)
        }
        
        viewModel.isRTL.observe(viewLifecycleOwner) { isRTL ->
            updateLayoutDirection(isRTL)
        }
    }
    
    /**
     * Updates language information display
     */
    private fun updateLanguageInfo(languageCode: String) {
        val displayName = LanguageManager.getLanguageDisplayName(languageCode)
        val direction = LanguageManager.getLanguageDirection(languageCode)
        
        binding.textCurrentLanguage.text = displayName
        binding.textLanguageDirection.text = "Direction: $direction"
        
        // Update RTL status
        val isRTL = LanguageManager.isRTLanguage(languageCode)
        binding.textRtlStatus.text = if (isRTL) "RTL Language" else "LTR Language"
    }
    
    /**
     * Updates layout direction based on language
     */
    private fun updateLayoutDirection(isRTL: Boolean) {
        val layoutDirection = if (isRTL) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        binding.root.layoutDirection = layoutDirection
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

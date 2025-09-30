package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.internetguard.pro.R
import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.ui.adapter.AppKeywordSettingsAdapter
import com.internetguard.pro.ui.viewmodel.AppKeywordSettingsViewModel
import com.internetguard.pro.ui.dialog.AppKeywordSelectionDialog

/**
 * Fragment for managing app-specific keyword blocking settings.
 * 
 * Allows users to select which apps should have keyword filtering
 * and configure which keywords apply to each app.
 */
class AppKeywordSettingsFragment : Fragment() {
    
    private lateinit var viewModel: AppKeywordSettingsViewModel
    private lateinit var adapter: AppKeywordSettingsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectAllButton: MaterialButton
    private lateinit var clearAllButton: MaterialButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_keyword_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.app_keyword_recycler)
        selectAllButton = view.findViewById(R.id.select_all_button)
        clearAllButton = view.findViewById(R.id.clear_all_button)
    }
    
    private fun setupRecyclerView() {
        adapter = AppKeywordSettingsAdapter(
            onAppToggle = { appInfo, isEnabled ->
                viewModel.toggleAppKeywordFiltering(appInfo.packageName, isEnabled)
            },
            onAppClick = { appInfo ->
                // Navigate to detailed keyword settings for this app
                showAppKeywordDetailsDialog(appInfo)
            }
        )
        
        recyclerView.apply {
            adapter = this@AppKeywordSettingsFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AppKeywordSettingsViewModel::class.java]
    }
    
    private fun setupClickListeners() {
        selectAllButton.setOnClickListener {
            viewModel.enableKeywordFilteringForAllApps()
        }
        
        clearAllButton.setOnClickListener {
            viewModel.disableKeywordFilteringForAllApps()
        }
    }
    
    private fun observeViewModel() {
        viewModel.appList.observe(viewLifecycleOwner) { apps ->
            adapter.submitList(apps)
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showErrorSnackbar(it)
                viewModel.clearError()
            }
        }
    }
    
    private fun showAppKeywordDetailsDialog(appInfo: AppInfo) {
        // Get existing keywords for this app
        viewModel.getActiveKeywordsForApp(appInfo.packageName).observe(viewLifecycleOwner) { keywords ->
            val existingKeywords = keywords.map { it.keyword }
            
            val dialog = AppKeywordSelectionDialog.newInstance(appInfo, existingKeywords)
            dialog.setOnKeywordsSavedListener { selectedKeywords ->
                // Save keywords for this app
                viewModel.updateAppKeywordRules(appInfo.packageName, selectedKeywords)
                
                Snackbar.make(
                    requireView(),
                    "Keywords saved for ${appInfo.appName}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            
            dialog.show(parentFragmentManager, "AppKeywordSelectionDialog")
        }
    }
    
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}

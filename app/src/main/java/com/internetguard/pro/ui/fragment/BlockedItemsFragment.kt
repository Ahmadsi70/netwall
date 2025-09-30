package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.ui.adapter.BlockedAppsAdapter
import com.internetguard.pro.ui.adapter.BlockedKeywordsAdapter
import com.internetguard.pro.ui.viewmodel.AppListViewModel
import kotlinx.coroutines.launch

class BlockedItemsFragment : Fragment() {
	
	private lateinit var viewModel: AppListViewModel
	private lateinit var blockedAppsAdapter: BlockedAppsAdapter
	private lateinit var blockedKeywordsAdapter: BlockedKeywordsAdapter
	
	private lateinit var blockedAppsRecycler: RecyclerView
	private lateinit var blockedKeywordsRecycler: RecyclerView
	private lateinit var blockedAppsCount: TextView
	private lateinit var blockedKeywordsCount: TextView
	private lateinit var noBlockedApps: TextView
	private lateinit var noBlockedKeywords: TextView
	private lateinit var appFilterSpinner: AutoCompleteTextView
	
	private var allKeywords = listOf<KeywordBlacklist>()
	private var selectedAppFilter: String? = null
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_blocked_items, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		initViews(view)
		setupViewModel()
		setupAdapters()
		setupAppFilter()
		observeData()
	}
	
	private fun initViews(view: View) {
		blockedAppsRecycler = view.findViewById(R.id.blocked_apps_recycler)
		blockedKeywordsRecycler = view.findViewById(R.id.blocked_keywords_recycler)
		blockedAppsCount = view.findViewById(R.id.blocked_apps_count)
		blockedKeywordsCount = view.findViewById(R.id.blocked_keywords_count)
		noBlockedApps = view.findViewById(R.id.no_blocked_apps)
		noBlockedKeywords = view.findViewById(R.id.no_blocked_keywords)
		appFilterSpinner = view.findViewById(R.id.app_filter_spinner)
	}
	
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[AppListViewModel::class.java]
	}
	
	private fun setupAdapters() {
		// Blocked Apps Adapter
		blockedAppsAdapter = BlockedAppsAdapter { app ->
			viewModel.updateWifiBlocking(app.packageName, false)
			viewModel.updateCellularBlocking(app.packageName, false)
		}
		blockedAppsRecycler.layoutManager = LinearLayoutManager(context)
		blockedAppsRecycler.adapter = blockedAppsAdapter
		
		// Blocked Keywords Adapter
		blockedKeywordsAdapter = BlockedKeywordsAdapter { keyword ->
			deleteKeyword(keyword)
		}
		blockedKeywordsRecycler.layoutManager = LinearLayoutManager(context)
		blockedKeywordsRecycler.adapter = blockedKeywordsAdapter
	}
	
	private fun setupAppFilter() {
		// Load all apps for filter
		viewLifecycleOwner.lifecycleScope.launch {
			val apps = (requireActivity().application as InternetGuardProApp).database
				.appKeywordRulesDao().getAllPackageNames()
			
			val filterOptions = mutableListOf("All Apps").apply {
				addAll(apps)
			}
			
			val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, filterOptions)
			appFilterSpinner.setAdapter(adapter)
			appFilterSpinner.setText("All Apps", false)
			
			appFilterSpinner.setOnItemClickListener { _, _, position, _ ->
				selectedAppFilter = if (position == 0) null else filterOptions[position]
				filterKeywords()
			}
		}
	}
	
	private fun observeData() {
		// Observe blocked apps
		viewModel.appList.observe(viewLifecycleOwner) { apps ->
			val blockedApps = apps.filter { it.blockWifi || it.blockCellular }
			blockedAppsAdapter.submitList(blockedApps)
			
			blockedAppsCount.text = "${blockedApps.size} apps blocked"
			noBlockedApps.visibility = if (blockedApps.isEmpty()) View.VISIBLE else View.GONE
			blockedAppsRecycler.visibility = if (blockedApps.isEmpty()) View.GONE else View.VISIBLE
		}
		
		// Load keywords
		loadKeywords()
	}
	
	private fun loadKeywords() {
		viewLifecycleOwner.lifecycleScope.launch {
			allKeywords = (requireActivity().application as InternetGuardProApp).database
				.keywordBlacklistDao().getAll()
			filterKeywords()
		}
	}
	
	private fun filterKeywords() {
		viewLifecycleOwner.lifecycleScope.launch {
			val filtered = if (selectedAppFilter == null) {
				allKeywords
			} else {
				// Filter keywords by app
				val rules = (requireActivity().application as InternetGuardProApp).database
					.appKeywordRulesDao().getRulesForApp(selectedAppFilter!!)
				val keywordIds = rules.map { it.keywordId }
				allKeywords.filter { it.id in keywordIds }
			}
			
			blockedKeywordsAdapter.submitList(filtered)
			blockedKeywordsCount.text = "${filtered.size} keywords blocked"
			noBlockedKeywords.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
			blockedKeywordsRecycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
		}
	}
	
	private fun deleteKeyword(keyword: KeywordBlacklist) {
		viewLifecycleOwner.lifecycleScope.launch {
			(requireActivity().application as InternetGuardProApp).database
				.keywordBlacklistDao().delete(keyword)
			loadKeywords()
		}
	}
	
	override fun onResume() {
		super.onResume()
		viewModel.loadInstalledApps()
		loadKeywords()
	}
} 
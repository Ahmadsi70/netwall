package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.ui.adapter.BlockedAppsAdapter
import com.internetguard.pro.ui.viewmodel.AppListViewModel
import kotlinx.coroutines.launch

class BlockedAppsFragment : Fragment() {
    
    private lateinit var viewModel: AppListViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var blockedAppsAdapter: BlockedAppsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blocked_apps, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.blocked_apps_recycler)
        emptyStateText = view.findViewById(R.id.empty_state_blocked)
    }
    
    private fun setupRecyclerView() {
        blockedAppsAdapter = BlockedAppsAdapter(
            onUnblockClick = { app ->
                lifecycleScope.launch {
                    viewModel.updateWifiBlocking(app.packageName, false)
                    viewModel.updateCellularBlocking(app.packageName, false)
                }
            }
        )
        
        recyclerView.apply {
            adapter = blockedAppsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AppListViewModel::class.java]
    }
    
    private fun observeViewModel() {
        viewModel.appList.observe(viewLifecycleOwner) { apps ->
            val blockedApps = apps?.filter { it.blockWifi || it.blockCellular } ?: emptyList()
            blockedAppsAdapter.submitList(blockedApps)
            
            if (blockedApps.isEmpty()) {
                emptyStateText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyStateText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadInstalledApps()
    }
} 
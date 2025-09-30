package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.internetguard.pro.databinding.FragmentDashboardBinding
import com.internetguard.pro.ui.adapter.BlockedAppsAdapter
import com.internetguard.pro.ui.viewmodel.DashboardViewModel

/**
 * Dashboard fragment showing blocked apps and statistics.
 * 
 * Displays blocked apps list and key metrics.
 */
class DashboardFragment : Fragment() {
	
	private var _binding: FragmentDashboardBinding? = null
	private val binding get() = _binding!!
	
	private lateinit var viewModel: DashboardViewModel
	private lateinit var blockedAppsAdapter: BlockedAppsAdapter
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentDashboardBinding.inflate(inflater, container, false)
		return binding.root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setupViewModel()
		setupBlockedAppsList()
		observeViewModel()
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
	}
	
	/**
	 * Sets up blocked apps RecyclerView
	 */
	private fun setupBlockedAppsList() {
		blockedAppsAdapter = BlockedAppsAdapter { app ->
			viewModel.unblockApp(app.packageName)
		}
		
		binding.blockedAppsRecycler.layoutManager = LinearLayoutManager(context)
		binding.blockedAppsRecycler.adapter = blockedAppsAdapter
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.blockedAppsList.observe(viewLifecycleOwner) { apps ->
			blockedAppsAdapter.submitList(apps)
		}
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
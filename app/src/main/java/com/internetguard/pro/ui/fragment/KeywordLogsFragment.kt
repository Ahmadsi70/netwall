package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.ui.adapter.KeywordLogsAdapter
import com.internetguard.pro.ui.viewmodel.KeywordLogsViewModel

/**
 * Fragment for displaying keyword blocking logs.
 * 
 * Shows a list of blocked content attempts with details about
 * when, where, and what was blocked.
 */
class KeywordLogsFragment : Fragment() {
	
	private lateinit var viewModel: KeywordLogsViewModel
	private lateinit var logsAdapter: KeywordLogsAdapter
	private lateinit var recyclerView: RecyclerView
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_keyword_logs, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		initViews(view)
		setupRecyclerView()
		setupViewModel()
		observeViewModel()
	}
	
	/**
	 * Initializes view references
	 */
	private fun initViews(view: View) {
		recyclerView = view.findViewById(R.id.logs_recycler)
	}
	
	/**
	 * Sets up RecyclerView with adapter
	 */
	private fun setupRecyclerView() {
		logsAdapter = KeywordLogsAdapter()
		
		recyclerView.apply {
			adapter = logsAdapter
			layoutManager = LinearLayoutManager(context)
		}
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[KeywordLogsViewModel::class.java]
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.logsList.observe(viewLifecycleOwner) { logs ->
			logsAdapter.submitList(logs)
		}
	}
}

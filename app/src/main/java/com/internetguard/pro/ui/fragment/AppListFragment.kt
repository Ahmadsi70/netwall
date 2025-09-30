package com.internetguard.pro.ui.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.internetguard.pro.R
import com.internetguard.pro.ui.adapter.AppListAdapter
import com.internetguard.pro.ui.viewmodel.AppListViewModel
import com.internetguard.pro.services.NetworkGuardVpnService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Fragment for displaying and managing app internet access controls.
 * 
 * Shows a list of installed apps with toggles for Wi-Fi and cellular blocking.
 * Integrates with VPN service for actual traffic control.
 */
class AppListFragment : Fragment() {
	
	companion object {
		private const val VPN_REQUEST_CODE = 1
	}
	
	private lateinit var viewModel: AppListViewModel
	private lateinit var appListAdapter: AppListAdapter
	private lateinit var recyclerView: RecyclerView
	private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
	
	// VPN permission broadcast receiver
	private val vpnPermissionReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == "com.internetguard.pro.REQUEST_VPN_PERMISSION") {
				val message = intent.getStringExtra("message") ?: "VPN permission required"
				showVpnPermissionDialog(message)
			}
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_app_list, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		initViews(view)
		setupRecyclerView()
		setupViewModel()
		setupClickListeners()
		observeViewModel()
		applyInitialFilterIfFromDashboard()
		
		// Register VPN permission broadcast receiver
		registerVpnPermissionReceiver()
		
		// Check VPN permission before allowing app blocking
		checkVpnPermission()
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		// Unregister broadcast receiver
		unregisterVpnPermissionReceiver()
	}
	
	/**
	 * Register VPN permission broadcast receiver
	 */
	private fun registerVpnPermissionReceiver() {
		val filter = IntentFilter("com.internetguard.pro.REQUEST_VPN_PERMISSION")
		requireContext().registerReceiver(vpnPermissionReceiver, filter)
	}
	
	/**
	 * Unregister VPN permission broadcast receiver
	 */
	private fun unregisterVpnPermissionReceiver() {
		try {
			requireContext().unregisterReceiver(vpnPermissionReceiver)
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error unregistering VPN permission receiver", e)
		}
	}
	
	/**
	 * Show VPN permission dialog
	 */
	private fun showVpnPermissionDialog(message: String) {
		Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
			.setAction("Grant Permission") {
				checkVpnPermission()
			}
			.show()
	}
	
	/**
	 * Check VPN permission
	 */
	private fun checkVpnPermission() {
		val intent = VpnService.prepare(requireContext())
		if (intent != null) {
			startActivityForResult(intent, VPN_REQUEST_CODE)
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == VPN_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				// VPN permission granted
				Log.d("AppListFragment", "VPN permission granted")
			} else {
				// VPN permission denied
				Log.w("AppListFragment", "VPN permission denied")
			}
		}
	}
	
	/**
	 * Initializes view references
	 */
	private fun initViews(view: View) {
		recyclerView = view.findViewById(R.id.app_list_recycler)
		toolbar = view.findViewById(R.id.toolbar)
	}
	
	/**
	 * Sets up RecyclerView with adapter and layout manager
	 */
	private fun setupRecyclerView() {
		appListAdapter = AppListAdapter(
			onWifiToggle = { packageName, isBlocked ->
				viewModel.updateWifiBlocking(packageName, isBlocked)
			},
			onCellularToggle = { packageName, isBlocked ->
				viewModel.updateCellularBlocking(packageName, isBlocked)
			}
		)
		
		recyclerView.apply {
			adapter = appListAdapter
			layoutManager = LinearLayoutManager(context)
		}
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[AppListViewModel::class.java]
	}

    private fun applyInitialFilterIfFromDashboard() {
        // نمایش همه اپ‌ها به‌صورت پیش‌فرض تا کاربر بتواند راحت انتخاب کند
        viewModel.setShowBlockedOnly(false)
        viewModel.loadInstalledApps()
    }
	
	/**
	 * Sets up click listeners and search
	 */
	private fun setupClickListeners() {
		// Toolbar menu with SearchView
		toolbar.setOnMenuItemClickListener { menuItem ->
			when (menuItem.itemId) {
				R.id.action_search -> {
					val searchView = menuItem.actionView as? androidx.appcompat.widget.SearchView
					searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
						override fun onQueryTextSubmit(query: String?): Boolean {
							viewModel.filterApps(query ?: "")
							return true
						}
						
						override fun onQueryTextChange(newText: String?): Boolean {
							viewModel.filterApps(newText ?: "")
							return true
						}
					})
					true
				}
				else -> false
			}
		}
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.appList.observe(viewLifecycleOwner) { apps ->
			appListAdapter.submitList(apps)
		}
		
		viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
			errorMessage?.let {
				showErrorSnackbar(it)
				viewModel.clearError()
			}
		}
	}
	
	/**
	 * Shows error message in Snackbar
	 */
	private fun showErrorSnackbar(message: String) {
		Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
	}
}

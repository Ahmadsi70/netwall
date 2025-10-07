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
	private lateinit var loadingOverlay: View
	private var searchJob: kotlinx.coroutines.Job? = null
	
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
		
		// 🌍 LANGUAGE: Apply system language to fragment
		val systemLanguage = com.internetguard.pro.utils.LanguageManager.getBestMatchingLanguage(requireContext())
		com.internetguard.pro.utils.LanguageManager.applyLanguage(requireContext(), systemLanguage)
		
		try {
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
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error in onViewCreated: ${e.message}", e)
			// Show error to user safely
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, "Error loading app list: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
			}
		}
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
		val context = context
		if (context == null) {
			Log.w("AppListFragment", "Context is null, cannot register VPN permission receiver")
			return
		}
		
		try {
			val filter = IntentFilter("com.internetguard.pro.REQUEST_VPN_PERMISSION")
			context.registerReceiver(vpnPermissionReceiver, filter)
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error registering VPN permission receiver: ${e.message}", e)
		}
	}
	
	/**
	 * Unregister VPN permission broadcast receiver
	 */
	private fun unregisterVpnPermissionReceiver() {
		val context = context
		if (context == null) {
			Log.w("AppListFragment", "Context is null, cannot unregister VPN permission receiver")
			return
		}
		
		try {
			context.unregisterReceiver(vpnPermissionReceiver)
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error unregistering VPN permission receiver: ${e.message}", e)
		}
	}
	
	/**
	 * Show VPN permission dialog
	 */
	private fun showVpnPermissionDialog(message: String) {
		val view = view
		if (view == null) {
			Log.w("AppListFragment", "View is null, cannot show VPN permission dialog")
			return
		}
		
		try {
			Snackbar.make(view, message, Snackbar.LENGTH_LONG)
				.setAction("Grant Permission") {
					checkVpnPermission()
				}
				.show()
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error showing VPN permission dialog: ${e.message}", e)
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Check VPN permission
	 */
	private fun checkVpnPermission() {
		val context = context
		if (context == null) {
			Log.w("AppListFragment", "Context is null, cannot check VPN permission")
			return
		}
		
		try {
			val intent = VpnService.prepare(context)
			if (intent != null) {
				startActivityForResult(intent, VPN_REQUEST_CODE)
			}
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error checking VPN permission: ${e.message}", e)
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
		try {
			recyclerView = view.findViewById(R.id.app_list_recycler)
			toolbar = view.findViewById(R.id.toolbar)
			loadingOverlay = view.findViewById(R.id.loading_overlay)
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error initializing views: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, "Error initializing interface: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Sets up RecyclerView with adapter and layout manager
	 */
	private fun setupRecyclerView() {
		try {
			appListAdapter = AppListAdapter(
				onWifiToggle = { packageName, isBlocked ->
					viewModel.updateWifiBlocking(packageName, isBlocked)
				},
				onCellularToggle = { packageName, isBlocked ->
					viewModel.updateCellularBlocking(packageName, isBlocked)
				}
			)
			
			val context = context
			if (context == null) {
				Log.w("AppListFragment", "Context is null, cannot setup RecyclerView")
				return
			}
			
			recyclerView.apply {
				adapter = appListAdapter
				layoutManager = LinearLayoutManager(context)
			}
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error setting up RecyclerView: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, "Error setting up app list: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		try {
			viewModel = ViewModelProvider(this)[AppListViewModel::class.java]
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error setting up ViewModel: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, "Error initializing app system: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}

    private fun applyInitialFilterIfFromDashboard() {
        try {
            // Show all apps by default so user can easily select
            viewModel.setShowBlockedOnly(false)
            viewModel.loadInstalledApps()
        } catch (e: Exception) {
            Log.e("AppListFragment", "Error applying initial filter: ${e.message}", e)
            // Show error to user
            val context = context
            if (context != null) {
                android.widget.Toast.makeText(context, "Error loading apps: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
	
	/**
	 * Sets up click listeners and search
	 */
	private fun setupClickListeners() {
		try {
			// Toolbar menu with SearchView
			toolbar.setOnMenuItemClickListener { menuItem ->
				when (menuItem.itemId) {
					R.id.action_search -> {
						try {
							val searchView = menuItem.actionView as? androidx.appcompat.widget.SearchView
							searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
								override fun onQueryTextSubmit(query: String?): Boolean {
									try {
										viewModel.filterApps(query ?: "")
									} catch (e: Exception) {
										Log.e("AppListFragment", "Error filtering apps on submit: ${e.message}", e)
									}
									return true
								}
								override fun onQueryTextChange(newText: String?): Boolean {
									try {
										searchJob?.cancel()
										searchJob = viewLifecycleOwner.lifecycleScope.launch {
											kotlinx.coroutines.delay(300)
											viewModel.filterApps(newText ?: "")
										}
									} catch (e: Exception) {
										Log.e("AppListFragment", "Error filtering apps on change: ${e.message}", e)
									}
									return true
								}
							})
						} catch (e: Exception) {
							Log.e("AppListFragment", "Error setting up search view: ${e.message}", e)
						}
						true
					}
					else -> false
				}
			}
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error setting up click listeners: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, "Error setting up interface: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		try {
			viewModel.appList.observe(viewLifecycleOwner) { apps ->
				try {
					appListAdapter.submitList(apps)
				} catch (e: Exception) {
					Log.e("AppListFragment", "Error updating app list: ${e.message}", e)
				}
			}
			viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
				try {
					loadingOverlay.visibility = if (isLoading == true) View.VISIBLE else View.GONE
				} catch (e: Exception) {
					Log.e("AppListFragment", "Error updating loading state: ${e.message}", e)
				}
			}
			viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
				try {
					errorMessage?.let {
						showErrorSnackbar(it)
						viewModel.clearError()
					}
				} catch (e: Exception) {
					Log.e("AppListFragment", "Error showing error message: ${e.message}", e)
				}
			}
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error setting up ViewModel observers: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, "Error setting up app monitoring: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Shows error message in Snackbar
	 */
	private fun showErrorSnackbar(message: String) {
		val view = view
		if (view == null) {
			Log.w("AppListFragment", "View is null, cannot show error snackbar")
			// Fallback to Toast
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
			}
			return
		}
		
		try {
			Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
		} catch (e: Exception) {
			Log.e("AppListFragment", "Error showing error snackbar: ${e.message}", e)
			// Fallback to Toast
			val context = context
			if (context != null) {
				android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
			}
		}
	}
}

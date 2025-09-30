package com.internetguard.pro.ui.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.ui.dialog.AppBlockingConfirmationDialog

/**
 * RecyclerView adapter for displaying app list with blocking controls.
 * 
 * Uses DiffUtil for efficient updates and supports optimistic UI updates
 * for instant feedback to users. Also supports batch selection mode.
 */
class AppListAdapter(
	private val onWifiToggle: (String, Boolean) -> Unit,
	private val onCellularToggle: (String, Boolean) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_app_list, parent, false)
		return AppViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
	
	/**
	 * ViewHolder for app list items
	 */
	inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
		private val appName: TextView = itemView.findViewById(R.id.app_name)
		private val packageName: TextView = itemView.findViewById(R.id.package_name)
		private val wifiSwitch: Switch = itemView.findViewById(R.id.wifi_switch)
		private val cellularSwitch: Switch = itemView.findViewById(R.id.cellular_switch)
		private val statusIndicator: View = itemView.findViewById(R.id.status_indicator)
		private val loadingIndicator: ProgressBar = itemView.findViewById(R.id.loading_indicator)
		
		fun bind(app: AppInfo) {
			appName.text = app.appName
			packageName.text = app.packageName
			appIcon.setImageDrawable(app.icon)
			
			// Remove listeners to prevent triggering during setup
			wifiSwitch.setOnCheckedChangeListener(null)
			cellularSwitch.setOnCheckedChangeListener(null)
			
			// Set switch states
			wifiSwitch.isChecked = app.blockWifi
			cellularSwitch.isChecked = app.blockCellular
			
			// Update status indicator
			updateStatusIndicator(app)
			
			// Optimistic UI: WiFi switch with instant feedback
			wifiSwitch.setOnCheckedChangeListener { view, isChecked ->
				// Skip if triggered programmatically
				if (!view.isPressed) return@setOnCheckedChangeListener
				
				// 1. Immediate visual feedback (optimistic)
				showOptimisticFeedback(view, loadingIndicator)
				
				// 2. Trigger actual VPN update (background)
				onWifiToggle(app.packageName, isChecked)
				
				// 3. Hide loading after short delay
				view.postDelayed({
					hideOptimisticFeedback(view, loadingIndicator)
				}, 400)
			}
			
			// Optimistic UI: Cellular switch with instant feedback
			cellularSwitch.setOnCheckedChangeListener { view, isChecked ->
				// Skip if triggered programmatically
				if (!view.isPressed) return@setOnCheckedChangeListener
				
				// 1. Immediate visual feedback
				showOptimisticFeedback(view, loadingIndicator)
				
				// 2. Trigger actual VPN update (background)
				onCellularToggle(app.packageName, isChecked)
				
				// 4. Hide loading after short delay
				view.postDelayed({
					hideOptimisticFeedback(view, loadingIndicator)
				}, 400)
			}
		}
		
		/**
		 * Updates the status indicator based on blocking state
		 */
		private fun updateStatusIndicator(app: AppInfo) {
			val isBlocked = app.blockWifi || app.blockCellular
			statusIndicator.setBackgroundColor(
				itemView.context.getColor(
					if (isBlocked) R.color.blocked_status else R.color.allowed_status
				)
			)
		}
		
		/**
		 * Show optimistic feedback when user toggles switch
		 * Provides instant visual response for better UX
		 */
		private fun showOptimisticFeedback(view: View, loading: ProgressBar) {
			// Reduce opacity to show processing
			view.alpha = 0.7f
			
			// Show loading indicator
			loading.visibility = View.VISIBLE
			
			// Haptic feedback for tactile response
			view.performHapticFeedback(
				HapticFeedbackConstants.VIRTUAL_KEY,
				HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
			)
		}
		
		/**
		 * Hide optimistic feedback and show success animation
		 */
		private fun hideOptimisticFeedback(view: View, loading: ProgressBar) {
			// Restore full opacity
			view.alpha = 1.0f
			
			// Hide loading indicator
			loading.visibility = View.GONE
			
			// Success animation: subtle scale bounce
			view.animate()
				.scaleX(1.15f)
				.scaleY(1.15f)
				.setDuration(100)
				.withEndAction {
					view.animate()
						.scaleX(1.0f)
						.scaleY(1.0f)
						.setDuration(100)
						.start()
				}
				.start()
		}
	}
	
	/**
	 * DiffUtil callback for efficient list updates
	 */
	class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
		override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
			return oldItem.packageName == newItem.packageName
		}
		
		override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
			return oldItem == newItem
		}
	}
}

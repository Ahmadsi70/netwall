package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.data.model.AppKeywordSettings

/**
 * Adapter for displaying app keyword filtering settings.
 * 
 * Shows a list of apps with toggles for enabling/disabling keyword filtering
 * and displays the current filtering status for each app.
 */
class AppKeywordSettingsAdapter(
    private val onAppToggle: (AppInfo, Boolean) -> Unit,
    private val onAppClick: (AppInfo) -> Unit
) : ListAdapter<AppKeywordSettings, AppKeywordSettingsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_keyword_settings, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
        private val appName: TextView = itemView.findViewById(R.id.app_name)
        private val packageName: TextView = itemView.findViewById(R.id.package_name)
        private val statusText: TextView = itemView.findViewById(R.id.status_text)
        private val keywordsPreviewText: TextView = itemView.findViewById(R.id.keywords_preview_text)
        private val keywordSwitch: Switch = itemView.findViewById(R.id.keyword_switch)
        
        fun bind(appSettings: AppKeywordSettings) {
            val appInfo = appSettings.appInfo
            
            // Set app information
            appName.text = appInfo.appName
            packageName.text = appInfo.packageName
            statusText.text = appSettings.displayStatus
            keywordsPreviewText.text = appSettings.previewText
            keywordsPreviewText.visibility = if (appSettings.previewText.isEmpty()) View.GONE else View.VISIBLE
            
            // Set app icon
            appInfo.icon?.let { drawable ->
                appIcon.setImageDrawable(drawable)
            } ?: run {
                appIcon.setImageResource(R.drawable.ic_app_default)
            }
            
            // Set switch state without triggering listener
            keywordSwitch.setOnCheckedChangeListener(null)
            keywordSwitch.isChecked = appSettings.hasKeywordFiltering
            
            // Set switch listener
            keywordSwitch.setOnCheckedChangeListener { _, isChecked ->
                onAppToggle(appInfo, isChecked)
            }
            
            // Set click listener for the whole item
            itemView.setOnClickListener {
                onAppClick(appInfo)
            }
            
            // Update status text color based on filtering state
            statusText.setTextColor(
                if (appSettings.hasKeywordFiltering) {
                    itemView.context.getColor(R.color.success_green)
                } else {
                    itemView.context.getColor(R.color.text_secondary)
                }
            )
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<AppKeywordSettings>() {
        override fun areItemsTheSame(
            oldItem: AppKeywordSettings,
            newItem: AppKeywordSettings
        ): Boolean {
            return oldItem.appInfo.packageName == newItem.appInfo.packageName
        }
        
        override fun areContentsTheSame(
            oldItem: AppKeywordSettings,
            newItem: AppKeywordSettings
        ): Boolean {
            return oldItem == newItem
        }
    }
}

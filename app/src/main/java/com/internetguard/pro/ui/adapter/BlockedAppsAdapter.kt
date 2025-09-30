package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.internetguard.pro.R
import com.internetguard.pro.data.model.AppInfo

class BlockedAppsAdapter(
    private val onUnblockClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, BlockedAppsAdapter.BlockedAppViewHolder>(BlockedAppDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedAppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_app, parent, false)
        return BlockedAppViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BlockedAppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class BlockedAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.blocked_app_icon)
        private val appName: TextView = itemView.findViewById(R.id.blocked_app_name)
        private val blockStatus: TextView = itemView.findViewById(R.id.block_status)
        private val unblockButton: MaterialButton = itemView.findViewById(R.id.unblock_button)
        
        fun bind(app: AppInfo) {
            appName.text = app.appName
            appIcon.setImageDrawable(app.icon)
            
            // Show block status
            val status = when {
                app.blockWifi && app.blockCellular -> "مسدود: WiFi + Cellular"
                app.blockWifi -> "مسدود: WiFi"
                app.blockCellular -> "مسدود: Cellular"
                else -> ""
            }
            blockStatus.text = status
            
            unblockButton.setOnClickListener {
                onUnblockClick(app)
            }
        }
    }
    
    class BlockedAppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }
        
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}

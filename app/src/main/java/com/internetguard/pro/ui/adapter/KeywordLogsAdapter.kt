package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.KeywordLogs
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying keyword blocking logs.
 * 
 * Shows blocked content attempts with timestamps and details.
 */
class KeywordLogsAdapter : ListAdapter<KeywordLogs, KeywordLogsAdapter.LogViewHolder>(LogDiffCallback()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_keyword_log, parent, false)
		return LogViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
	
	/**
	 * ViewHolder for log items
	 */
	inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val appNameText: TextView = itemView.findViewById(R.id.app_name_text)
		private val blockedContentText: TextView = itemView.findViewById(R.id.blocked_content_text)
		private val timestampText: TextView = itemView.findViewById(R.id.timestamp_text)
		
		fun bind(log: KeywordLogs) {
			appNameText.text = log.appPackageName
			blockedContentText.text = log.blockedContent
			timestampText.text = formatTimestamp(log.attemptTime)
		}
		
		/**
		 * Formats timestamp to readable format
		 */
		private fun formatTimestamp(timestamp: Long): String {
			val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
			return formatter.format(Date(timestamp))
		}
	}
	
	/**
	 * DiffUtil callback for efficient list updates
	 */
	class LogDiffCallback : DiffUtil.ItemCallback<KeywordLogs>() {
		override fun areItemsTheSame(oldItem: KeywordLogs, newItem: KeywordLogs): Boolean {
			return oldItem.id == newItem.id
		}
		
		override fun areContentsTheSame(oldItem: KeywordLogs, newItem: KeywordLogs): Boolean {
			return oldItem == newItem
		}
	}
}

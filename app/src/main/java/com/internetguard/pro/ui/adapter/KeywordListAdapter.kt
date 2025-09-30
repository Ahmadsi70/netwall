package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.KeywordBlacklist

/**
 * RecyclerView adapter for displaying keyword blacklist.
 * 
 * Shows keywords with category, language, and case sensitivity info.
 * Provides edit and delete actions for each item.
 */
class KeywordListAdapter(
    private val onEditClick: (KeywordBlacklist) -> Unit,
    private val onDeleteClick: (KeywordBlacklist) -> Unit,
    private val onAssignClick: (KeywordBlacklist) -> Unit
) : ListAdapter<KeywordBlacklist, KeywordListAdapter.KeywordViewHolder>(KeywordDiffCallback()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_keyword_list, parent, false)
		return KeywordViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
	
	/**
	 * ViewHolder for keyword list items
	 */
	inner class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val keywordText: TextView = itemView.findViewById(R.id.keyword_text)
		private val categoryText: TextView = itemView.findViewById(R.id.category_text)
		private val languageText: TextView = itemView.findViewById(R.id.language_text)
		private val caseSensitiveText: TextView = itemView.findViewById(R.id.case_sensitive_text)
		private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
		private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
            private val assignButton: ImageButton = itemView.findViewById(R.id.assign_button)
		
		fun bind(keyword: KeywordBlacklist) {
			keywordText.text = keyword.keyword
			
			// Display category if available
			categoryText.text = keyword.category ?: "No category"
			categoryText.visibility = if (keyword.category.isNullOrEmpty()) View.GONE else View.VISIBLE
			
			// Display language if available
			languageText.text = keyword.language ?: "Any language"
			languageText.visibility = if (keyword.language.isNullOrEmpty()) View.GONE else View.VISIBLE
			
			// Display case sensitivity
			caseSensitiveText.text = if (keyword.caseSensitive) "Case sensitive" else "Case insensitive"
			caseSensitiveText.setTextColor(
				itemView.context.getColor(
					if (keyword.caseSensitive) R.color.case_sensitive else R.color.case_insensitive
				)
			)
			
            // Set up click listeners
			editButton.setOnClickListener { onEditClick(keyword) }
			deleteButton.setOnClickListener { onDeleteClick(keyword) }
            assignButton.setOnClickListener { onAssignClick(keyword) }
		}
	}
	
	/**
	 * DiffUtil callback for efficient list updates
	 */
	class KeywordDiffCallback : DiffUtil.ItemCallback<KeywordBlacklist>() {
		override fun areItemsTheSame(oldItem: KeywordBlacklist, newItem: KeywordBlacklist): Boolean {
			return oldItem.id == newItem.id
		}
		
		override fun areContentsTheSame(oldItem: KeywordBlacklist, newItem: KeywordBlacklist): Boolean {
			return oldItem == newItem
		}
	}
}

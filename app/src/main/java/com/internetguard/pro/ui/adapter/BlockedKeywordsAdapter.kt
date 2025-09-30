package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.KeywordBlacklist

class BlockedKeywordsAdapter(
	private val onDelete: (KeywordBlacklist) -> Unit
) : ListAdapter<KeywordBlacklist, BlockedKeywordsAdapter.KeywordViewHolder>(KeywordDiffCallback()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_blocked_keyword, parent, false)
		return KeywordViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
	
	inner class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val keywordText: TextView = itemView.findViewById(R.id.keyword_text)
		private val categoryText: TextView = itemView.findViewById(R.id.category_text)
		private val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_button)
		
		fun bind(keyword: KeywordBlacklist) {
			keywordText.text = keyword.keyword
			categoryText.text = keyword.category ?: "General"
			
			deleteButton.setOnClickListener {
				onDelete(keyword)
			}
		}
	}
	
	private class KeywordDiffCallback : DiffUtil.ItemCallback<KeywordBlacklist>() {
		override fun areItemsTheSame(oldItem: KeywordBlacklist, newItem: KeywordBlacklist): Boolean {
			return oldItem.id == newItem.id
		}
		
		override fun areContentsTheSame(oldItem: KeywordBlacklist, newItem: KeywordBlacklist): Boolean {
			return oldItem == newItem
		}
	}
} 
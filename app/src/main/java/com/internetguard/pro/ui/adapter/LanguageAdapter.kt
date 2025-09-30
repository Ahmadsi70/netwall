package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.databinding.ItemLanguageBinding
import com.internetguard.pro.ui.viewmodel.LanguageSettingsViewModel.LanguageItem

/**
 * Adapter for displaying language selection list.
 * 
 * Shows supported languages with RTL indicators and selection states.
 * Handles language selection and provides visual feedback.
 */
class LanguageAdapter(
    private val onLanguageSelected: (String) -> Unit
) : ListAdapter<LanguageItem, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {
    
    private var selectedLanguageCode: String? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * Sets the selected language code
     */
    fun setSelectedLanguage(languageCode: String?) {
        selectedLanguageCode = languageCode
        notifyDataSetChanged()
    }
    
    /**
     * ViewHolder for language items
     */
    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(language: LanguageItem) {
            binding.apply {
                textLanguageName.text = language.name
                textLanguageCode.text = language.code
                
                // Show RTL indicator
                if (language.isRTL) {
                    textRtlIndicator.visibility = View.VISIBLE
                    textRtlIndicator.text = "RTL"
                } else {
                    textRtlIndicator.visibility = View.GONE
                }
                
                // Show selection state
                val isSelected = language.code == selectedLanguageCode
                radioButton.isChecked = isSelected
                
                // Set click listener
                root.setOnClickListener {
                    onLanguageSelected(language.code)
                }
                
                radioButton.setOnClickListener {
                    onLanguageSelected(language.code)
                }
            }
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates
     */
    class LanguageDiffCallback : DiffUtil.ItemCallback<LanguageItem>() {
        override fun areItemsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
            return oldItem.code == newItem.code
        }
        
        override fun areContentsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
            return oldItem == newItem
        }
    }
}

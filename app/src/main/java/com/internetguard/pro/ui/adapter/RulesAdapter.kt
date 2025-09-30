package com.internetguard.pro.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.CustomRules

/**
 * RecyclerView adapter for displaying custom rules.
 * 
 * Shows rules with their type, status, and priority.
 * Provides edit, delete, and toggle actions for each rule.
 */
class RulesAdapter(
	private val onEditClick: (CustomRules) -> Unit,
	private val onDeleteClick: (CustomRules) -> Unit,
	private val onToggleClick: (CustomRules, Boolean) -> Unit
) : ListAdapter<CustomRules, RulesAdapter.RuleViewHolder>(RuleDiffCallback()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
		val view = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_rule, parent, false)
		return RuleViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
	
	/**
	 * ViewHolder for rule items
	 */
	inner class RuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val ruleNameText: TextView = itemView.findViewById(R.id.rule_name_text)
		private val ruleTypeText: TextView = itemView.findViewById(R.id.rule_type_text)
		private val priorityText: TextView = itemView.findViewById(R.id.priority_text)
		private val enabledSwitch: Switch = itemView.findViewById(R.id.enabled_switch)
		private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
		private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
		
		fun bind(rule: CustomRules) {
			ruleNameText.text = rule.ruleName
			ruleTypeText.text = formatRuleType(rule.ruleType)
			priorityText.text = "Priority: ${rule.priority}"
			enabledSwitch.isChecked = rule.isEnabled
			
			// Set up click listeners
			enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
				onToggleClick(rule, isChecked)
			}
			
			editButton.setOnClickListener { onEditClick(rule) }
			deleteButton.setOnClickListener { onDeleteClick(rule) }
		}
		
		/**
		 * Formats rule type for display
		 */
		private fun formatRuleType(ruleType: String): String {
			return when (ruleType) {
				"time" -> "Time-based"
				"location" -> "Location-based"
				"battery" -> "Battery-based"
				"data_usage" -> "Data Usage"
				"context" -> "Context-based"
				else -> ruleType
			}
		}
	}
	
	/**
	 * DiffUtil callback for efficient list updates
	 */
	class RuleDiffCallback : DiffUtil.ItemCallback<CustomRules>() {
		override fun areItemsTheSame(oldItem: CustomRules, newItem: CustomRules): Boolean {
			return oldItem.id == newItem.id
		}
		
		override fun areContentsTheSame(oldItem: CustomRules, newItem: CustomRules): Boolean {
			return oldItem == newItem
		}
	}
}

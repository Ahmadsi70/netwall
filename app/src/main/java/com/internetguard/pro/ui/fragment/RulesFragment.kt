package com.internetguard.pro.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.CustomRules
import com.internetguard.pro.ui.adapter.RulesAdapter
import com.internetguard.pro.ui.viewmodel.RulesViewModel

/**
 * Fragment for managing custom rules.
 * 
 * Allows users to create, edit, and manage rules based on various conditions
 * like time, location, battery, data usage, and context.
 */
class RulesFragment : Fragment() {
	
	private lateinit var viewModel: RulesViewModel
	private lateinit var rulesAdapter: RulesAdapter
	private lateinit var recyclerView: RecyclerView
	private lateinit var addButton: FloatingActionButton
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_rules, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		initViews(view)
		setupRecyclerView()
		setupViewModel()
		setupClickListeners()
		observeViewModel()
	}
	
	/**
	 * Initializes view references
	 */
	private fun initViews(view: View) {
		recyclerView = view.findViewById(R.id.rules_recycler)
		addButton = view.findViewById(R.id.add_rule_fab)
	}
	
	/**
	 * Sets up RecyclerView with adapter
	 */
	private fun setupRecyclerView() {
		rulesAdapter = RulesAdapter(
			onEditClick = { rule -> showEditRuleDialog(rule) },
			onDeleteClick = { rule -> showDeleteConfirmDialog(rule) },
			onToggleClick = { rule, isEnabled -> viewModel.toggleRule(rule, isEnabled) }
		)
		
		recyclerView.apply {
			adapter = rulesAdapter
			layoutManager = LinearLayoutManager(context)
		}
	}
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[RulesViewModel::class.java]
	}
	
	/**
	 * Sets up click listeners
	 */
	private fun setupClickListeners() {
		addButton.setOnClickListener {
			showAddRuleDialog()
		}
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.rulesList.observe(viewLifecycleOwner) { rules ->
			rulesAdapter.submitList(rules)
		}
		
		viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
			errorMessage?.let {
				// Show error
				viewModel.clearError()
			}
		}
	}
	
	/**
	 * Shows dialog for adding new rule
	 */
	private fun showAddRuleDialog() {
		val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rule_input, null)
		val ruleNameInput = dialogView.findViewById<EditText>(R.id.rule_name_input)
		val ruleTypeSpinner = dialogView.findViewById<Spinner>(R.id.rule_type_spinner)
		val isEnabledSwitch = dialogView.findViewById<Switch>(R.id.is_enabled_switch)
		
		AlertDialog.Builder(requireContext())
			.setTitle("Add New Rule")
			.setView(dialogView)
			.setPositiveButton("Add") { _, _ ->
				val ruleName = ruleNameInput.text.toString().trim()
				val ruleType = ruleTypeSpinner.selectedItem.toString()
				val isEnabled = isEnabledSwitch.isChecked
				
				if (ruleName.isNotEmpty()) {
					val newRule = CustomRules(
						ruleName = ruleName,
						ruleType = ruleType,
						isEnabled = isEnabled,
						conditions = "{}", // Default empty conditions
						actions = "{}", // Default empty actions
						createdAt = System.currentTimeMillis()
					)
					viewModel.addRule(newRule)
				}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows dialog for editing existing rule
	 */
	private fun showEditRuleDialog(rule: CustomRules) {
		val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rule_input, null)
		val ruleNameInput = dialogView.findViewById<EditText>(R.id.rule_name_input)
		val ruleTypeSpinner = dialogView.findViewById<Spinner>(R.id.rule_type_spinner)
		val isEnabledSwitch = dialogView.findViewById<Switch>(R.id.is_enabled_switch)
		
		// Pre-fill with existing data
		ruleNameInput.setText(rule.ruleName)
		isEnabledSwitch.isChecked = rule.isEnabled
		
		AlertDialog.Builder(requireContext())
			.setTitle("Edit Rule")
			.setView(dialogView)
			.setPositiveButton("Save") { _, _ ->
				val updatedRule = rule.copy(
					ruleName = ruleNameInput.text.toString().trim(),
					ruleType = ruleTypeSpinner.selectedItem.toString(),
					isEnabled = isEnabledSwitch.isChecked
				)
				viewModel.updateRule(updatedRule)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows confirmation dialog for deleting rule
	 */
	private fun showDeleteConfirmDialog(rule: CustomRules) {
		AlertDialog.Builder(requireContext())
			.setTitle("Delete Rule")
			.setMessage("Are you sure you want to delete '${rule.ruleName}'?")
			.setPositiveButton("Delete") { _, _ ->
				viewModel.deleteRule(rule)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
}

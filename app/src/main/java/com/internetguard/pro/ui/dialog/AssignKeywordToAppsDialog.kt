package com.internetguard.pro.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.entities.AppKeywordRules
import com.internetguard.pro.InternetGuardProApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Simple dialog to assign a keyword to installed apps.
 * Lightweight implementation using a vertical list of checkboxes
 * to minimize integration risk.
 */
class AssignKeywordToAppsDialog : DialogFragment() {

    companion object {
        private const val ARG_KEYWORD = "arg_keyword"

        fun newInstance(keyword: KeywordBlacklist): AssignKeywordToAppsDialog {
            return AssignKeywordToAppsDialog().apply {
                arguments = Bundle().apply { putLong("keyword_id", keyword.id); putString("keyword_text", keyword.keyword) }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_assign_keyword_to_apps, null)

        val container = view.findViewById<LinearLayout>(R.id.apps_container)
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        val saveButton = view.findViewById<MaterialButton>(R.id.save_button)
        val cancelButton = view.findViewById<MaterialButton>(R.id.cancel_button)

        val database = (requireActivity().application as InternetGuardProApp).database
        val keywordId = requireArguments().getLong("keyword_id")
        val keywordText = requireArguments().getString("keyword_text") ?: ""

        // Load installed apps and existing rules
        lifecycleScope.launch(Dispatchers.IO) {
            val repo = com.internetguard.pro.data.repository.AppRepository(database.appBlockRulesDao(), requireContext().applicationContext)
            val apps = repo.getInstalledApps().sortedBy { it.appName }
            val existingRulesFlow = database.appKeywordRulesDao().getRulesForKeyword(keywordId)

            // Render initial and on rules change
            existingRulesFlow.collect { rules ->
                withContext(Dispatchers.Main) {
                    val selected = rules.map { it.appPackageName }.toSet()
                    renderAppsList(apps, selected, container)
                }
            }
        }

        // Search filter
        searchInput.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim()?.lowercase().orEmpty()
                lifecycleScope.launch(Dispatchers.IO) {
                    val repo = com.internetguard.pro.data.repository.AppRepository(database.appBlockRulesDao(), requireContext().applicationContext)
                    val all = repo.getInstalledApps().sortedBy { it.appName }
                    val filtered = if (query.isEmpty()) all else all.filter { it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query) }
                    val selected = database.appKeywordRulesDao().getRulesForKeywordSync(keywordId)
                    withContext(Dispatchers.Main) {
                        renderAppsList(filtered, selected.map { it.appPackageName }.toSet(), container)
                    }
                }
            }
        })

        

        // Save
        saveButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val selectedPackages = mutableListOf<String>()
                for (i in 0 until container.childCount) {
                    val child = container.getChildAt(i)
                    if (child is CheckBox && child.isChecked) {
                        selectedPackages.add(child.tag as String)
                    }
                }

                // Replace rules for this keyword
                database.appKeywordRulesDao().deleteAllRulesForKeyword(keywordId)
                val toInsert = selectedPackages.map { pkg ->
                    AppKeywordRules(appPackageName = pkg, keywordId = keywordId, isEnabled = true)
                }
                toInsert.forEach { database.appKeywordRulesDao().insert(it) }

                withContext(Dispatchers.Main) { dismiss() }
            }
        }

        cancelButton.setOnClickListener { dismiss() }

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.assign_keyword_title, keywordText))
            .setView(view)
            .create()
    }

    private fun renderAppsList(apps: List<com.internetguard.pro.data.model.AppInfo>, selected: Set<String>, container: LinearLayout) {
        container.removeAllViews()
        apps.forEach { appInfo ->
            val cb = CheckBox(requireContext()).apply {
                text = "${appInfo.appName} (${appInfo.packageName})"
                isChecked = selected.contains(appInfo.packageName)
                tag = appInfo.packageName
            }
            container.addView(cb)
        }
    }
}



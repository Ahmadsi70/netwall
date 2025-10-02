package com.internetguard.pro.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.internetguard.pro.R
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.ui.adapter.KeywordListAdapter
import com.internetguard.pro.ui.viewmodel.KeywordListViewModel
import com.internetguard.pro.ai.IntelligentKeywordSuggestionEngine
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.internetguard.pro.ui.dialog.AssignKeywordToAppsDialog

/**
 * Fragment for managing keyword/phrase blacklist.
 * 
 * Displays list of blocked keywords with options to add, edit, delete.
 * Supports multiple languages and categories.
 */
class KeywordListFragment : Fragment() {
	
	private lateinit var viewModel: KeywordListViewModel
	private lateinit var keywordAdapter: KeywordListAdapter
	private lateinit var recyclerView: RecyclerView
	private lateinit var addButton: FloatingActionButton
	private lateinit var clearAllButton: MaterialButton
	private lateinit var aiSuggestionEngine: IntelligentKeywordSuggestionEngine
	
	companion object {
		private const val DIALOG_ADD_KEYWORD = 1
		private const val DIALOG_EDIT_KEYWORD = 2
	}
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_keyword_list, container, false)
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
		recyclerView = view.findViewById(R.id.keyword_recycler)
		addButton = view.findViewById(R.id.add_keyword_fab)
		clearAllButton = view.findViewById(R.id.clear_all_button)
		
		// Setup toolbar menu
		val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
		toolbar.setOnMenuItemClickListener { menuItem ->
			when (menuItem.itemId) {
				R.id.action_test_api -> {
					findNavController().navigate(R.id.apiTestFragment)
					true
				}
				else -> false
			}
		}
	}
	
	/**
	 * Sets up RecyclerView with adapter
	 */
	private fun setupRecyclerView() {
        keywordAdapter = KeywordListAdapter(
            onEditClick = { keyword -> showEditKeywordDialog(keyword) },
            onDeleteClick = { keyword -> showDeleteConfirmDialog(keyword) },
            onAssignClick = { keyword -> showAssignKeywordToAppsDialog(keyword) }
        )
		
		recyclerView.apply {
			adapter = keywordAdapter
			layoutManager = LinearLayoutManager(context)
		}

    }

    private fun renderApps(apps: List<com.internetguard.pro.data.model.AppInfo>, container: android.widget.LinearLayout) {
        container.removeAllViews()
        apps.forEach { appInfo ->
            val cb = android.widget.CheckBox(requireContext()).apply {
                text = "${appInfo.appName} (${appInfo.packageName})"
                tag = appInfo.packageName
            }
            container.addView(cb)
        }
    }

    private fun collectSelectedPackages(container: android.widget.LinearLayout): List<String> {
        val result = mutableListOf<String>()
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is android.widget.CheckBox && child.isChecked) {
                result.add(child.tag as String)
            }
        }
        return result
    }

    private fun showAssignKeywordToAppsDialog(keyword: KeywordBlacklist) {
        val dialog = AssignKeywordToAppsDialog.newInstance(keyword)
        dialog.show(parentFragmentManager, "AssignKeywordToAppsDialog")
    }
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		viewModel = ViewModelProvider(this)[KeywordListViewModel::class.java]
		aiSuggestionEngine = IntelligentKeywordSuggestionEngine()
	}
	
	/**
	 * Sets up click listeners
	 */
	private fun setupClickListeners() {
		addButton.setOnClickListener {
			showAddKeywordDialog()
		}
		
		clearAllButton.setOnClickListener {
			showClearAllConfirmDialog()
		}
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		viewModel.keywordList.observe(viewLifecycleOwner) { keywords ->
			keywordAdapter.submitList(keywords)
		}
		
		viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
			errorMessage?.let {
				showErrorSnackbar(it)
				viewModel.clearError()
			}
		}
	}
	
	/**
	 * Shows dialog for adding new keyword with AI suggestions
	 */
	private fun showAddKeywordDialog() {
		val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_keyword_input_ai, null)
		val keywordInput = dialogView.findViewById<EditText>(R.id.keyword_input)
		val categoryInput = dialogView.findViewById<EditText>(R.id.category_input)
		val languageInput = dialogView.findViewById<EditText>(R.id.language_input)
		val caseSensitiveSwitch = dialogView.findViewById<Switch>(R.id.case_sensitive_switch)
		val aiSuggestionsGroup = dialogView.findViewById<ChipGroup>(R.id.ai_suggestions_group)
		val generateSuggestionsButton = dialogView.findViewById<MaterialButton>(R.id.generate_suggestions_button)
		
        // Generate AI suggestions (proxy first, fallback to local)
        generateSuggestionsButton.setOnClickListener {
            val inputKeyword = keywordInput.text.toString().trim()
            if (inputKeyword.isEmpty()) {
                Toast.makeText(context, "Please enter a keyword first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Try remote AI suggestions (developer's proxy)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val suggestUrl = com.internetguard.pro.ai.api.RemoteConfig.SUGGEST_URL
                val client = com.internetguard.pro.ai.api.RemoteModerationClient(endpoint = suggestUrl, timeoutMs = 2500)
                val suggest = (client as com.internetguard.pro.ai.api.SuggestionClient).suggest(inputKeyword, null, null)
                val any = suggest.synonyms.isNotEmpty() || suggest.variants.isNotEmpty() || suggest.obfuscations.isNotEmpty()
                withContext(Dispatchers.Main) {
                    if (any) {
                        renderSuggestionChips(suggest, aiSuggestionsGroup, keywordInput)
                        Toast.makeText(context, "AI suggestions loaded", Toast.LENGTH_SHORT).show()
                    } else {
                        showAISuggestions(inputKeyword, aiSuggestionsGroup, keywordInput)
                    }
                }
            }
        }
		
        // Inject apps UI controls
        val appsContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.apps_container)
        val appsSearch = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.apps_search_input)
        val applyAll = dialogView.findViewById<android.widget.Switch>(R.id.apply_all_switch)

        // Load apps lazily
        val app = requireActivity().application as com.internetguard.pro.InternetGuardProApp
        val db = app.database
        val repo = com.internetguard.pro.data.repository.AppRepository(db.appBlockRulesDao(), requireContext().applicationContext)
        val allApps = java.util.concurrent.atomic.AtomicReference<List<com.internetguard.pro.data.model.AppInfo>>(emptyList())
        viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val list = repo.getInstalledApps().sortedBy { it.appName }
            allApps.set(list)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                renderApps(list, appsContainer)
            }
        }

        // Search filter
        appsSearch?.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()
                val list = allApps.get()
                val filtered = if (q.isEmpty()) list else list.filter { it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
                renderApps(filtered, appsContainer)
            }
        })

        // Apply all toggle
        applyAll?.setOnCheckedChangeListener { _, isChecked ->
            appsContainer.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        val alert = AlertDialog.Builder(requireContext())
            .setTitle("Add New Keyword")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        alert.setOnShowListener {
            val addBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            addBtn.setOnClickListener {
                val keyword = keywordInput.text.toString().trim()
                val category = categoryInput.text.toString().trim().takeIf { it.isNotEmpty() }
                val language = languageInput.text.toString().trim().takeIf { it.isNotEmpty() }
                val caseSensitive = caseSensitiveSwitch.isChecked
                if (keyword.isEmpty()) {
                    Toast.makeText(context, "Please enter a keyword", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    // 1) Upsert keyword
                    val newKeyword = KeywordBlacklist(
                        keyword = keyword,
                        category = category,
                        caseSensitive = caseSensitive,
                        language = language,
                        createdAt = System.currentTimeMillis()
                    )
                    val keywordId = (requireActivity().application as com.internetguard.pro.InternetGuardProApp).database.keywordBlacklistDao().upsert(newKeyword)

                    // 2) Build rules
                    val selectedPackages = collectSelectedPackages(appsContainer)
                    val targetPackages: List<String> = if (applyAll?.isChecked == true) {
                        // Apply to all apps
                        allApps.get().map { it.packageName }
                    } else if (selectedPackages.isNotEmpty()) {
                        // Apply to selected apps only
                        selectedPackages
                    } else {
                        // اگر هیچ اپی انتخاب نشده، global block می‌شود (برای همه اپ‌ها)
                        emptyList() // Global keyword - will block in all apps
                    }

                    val dao = (requireActivity().application as com.internetguard.pro.InternetGuardProApp).database.appKeywordRulesDao()
                    
                    if (targetPackages.isEmpty()) {
                        // Global keyword - نیازی به rules نیست
                        Log.d("KeywordListFragment", "Keyword added as global (applies to all apps)")
                    } else {
                        // اختصاص به اپ‌های خاص
                        targetPackages.forEach { pkg ->
                            dao.insert(com.internetguard.pro.data.entities.AppKeywordRules(appPackageName = pkg, keywordId = keywordId, isEnabled = true))
                        }
                        Log.d("KeywordListFragment", "Keyword assigned to ${targetPackages.size} apps")
                    }

                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        alert.dismiss()
                    }
                }
            }
        }

        alert.show()
	}
	
	/**
	 * Show AI-generated suggestions
	 */
	private fun showAISuggestions(keyword: String, chipGroup: ChipGroup, keywordInput: EditText) {
        // Clear previous suggestions
        chipGroup.removeAllViews()
        
        // Generate AI suggestions
        val suggestions = aiSuggestionEngine.generateSuggestions(keyword)
        
        if (suggestions.isEmpty()) {
            Toast.makeText(context, "No suggestions found", Toast.LENGTH_SHORT).show()
            return
        }

        // Create chips for each suggestion
        suggestions.forEach { suggestion ->
            val chip = Chip(requireContext())
            chip.text = suggestion
            chip.isCheckable = true
            chip.setOnClickListener {
                if (chip.isChecked) {
                    // Add suggestion to the keyword input
                    val currentText = keywordInput.text.toString()
                    if (currentText.isEmpty()) {
                        keywordInput.setText(suggestion)
                    } else {
                        keywordInput.setText("$currentText, $suggestion")
                    }
                }
            }
            chipGroup.addView(chip)
        }
        
        Toast.makeText(context, "Generated ${suggestions.size} AI suggestions", Toast.LENGTH_SHORT).show()
    }

    private fun renderSuggestionChips(s: com.internetguard.pro.ai.api.SuggestResult, chipGroup: ChipGroup, keywordInput: EditText) {
        chipGroup.removeAllViews()
        val all = (s.synonyms + s.variants + s.obfuscations).distinct().take(24)
        if (all.isEmpty()) return
        all.forEach { suggestion ->
            val chip = com.google.android.material.chip.Chip(requireContext())
            chip.text = suggestion
            chip.isCheckable = true
            chip.setOnClickListener {
                if (chip.isChecked) {
                    val currentText = keywordInput.text.toString()
                    if (currentText.isEmpty()) keywordInput.setText(suggestion) else keywordInput.setText("$currentText, $suggestion")
                }
            }
            chipGroup.addView(chip)
        }
    }
	
	/**
	 * Shows dialog for editing existing keyword
	 */
	private fun showEditKeywordDialog(keyword: KeywordBlacklist) {
		val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_keyword_input, null)
		val keywordInput = dialogView.findViewById<EditText>(R.id.keyword_input)
		val categoryInput = dialogView.findViewById<EditText>(R.id.category_input)
		val languageInput = dialogView.findViewById<EditText>(R.id.language_input)
		val caseSensitiveSwitch = dialogView.findViewById<Switch>(R.id.case_sensitive_switch)
		
		// Pre-fill with existing data
		keywordInput.setText(keyword.keyword)
		categoryInput.setText(keyword.category ?: "")
		languageInput.setText(keyword.language ?: "")
		caseSensitiveSwitch.isChecked = keyword.caseSensitive
		
		AlertDialog.Builder(requireContext())
			.setTitle("Edit Keyword")
			.setView(dialogView)
			.setPositiveButton("Save") { _, _ ->
				val updatedKeyword = keyword.copy(
					keyword = keywordInput.text.toString().trim(),
					category = categoryInput.text.toString().trim().takeIf { it.isNotEmpty() },
					language = languageInput.text.toString().trim().takeIf { it.isNotEmpty() },
					caseSensitive = caseSensitiveSwitch.isChecked
				)
				viewModel.updateKeyword(updatedKeyword)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows confirmation dialog for deleting keyword
	 */
	private fun showDeleteConfirmDialog(keyword: KeywordBlacklist) {
		AlertDialog.Builder(requireContext())
			.setTitle("Delete Keyword")
			.setMessage("Are you sure you want to delete '${keyword.keyword}'?")
			.setPositiveButton("Delete") { _, _ ->
				viewModel.deleteKeyword(keyword)
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows confirmation dialog for clearing all keywords
	 */
	private fun showClearAllConfirmDialog() {
		AlertDialog.Builder(requireContext())
			.setTitle("Clear All Keywords")
			.setMessage("Are you sure you want to delete all keywords? This action cannot be undone.")
			.setPositiveButton("Clear All") { _, _ ->
				viewModel.clearAllKeywords()
			}
			.setNegativeButton("Cancel", null)
			.show()
	}
	
	/**
	 * Shows error message in Snackbar
	 */
	private fun showErrorSnackbar(message: String) {
		Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
	}
}

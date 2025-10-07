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
import android.widget.TextView
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
import com.internetguard.pro.utils.AccessibilityServiceChecker
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
	
	// New UI elements
	private lateinit var enableServiceButton: MaterialButton
	private lateinit var testApiButton: MaterialButton
	private lateinit var helpButton: MaterialButton
	private lateinit var serviceStatusText: TextView
	private lateinit var serviceDescriptionText: TextView
	private lateinit var statusIndicator: View
	
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
		
		// 🌍 LANGUAGE: Apply system language to fragment
		val systemLanguage = com.internetguard.pro.utils.LanguageManager.getBestMatchingLanguage(requireContext())
		com.internetguard.pro.utils.LanguageManager.applyLanguage(requireContext(), systemLanguage)
		
		try {
			initViews(view)
			setupRecyclerView()
			setupViewModel()
			setupClickListeners()
			observeViewModel()
			
			// Check accessibility service permission when entering keyword section
			checkAccessibilityPermission()
			
			// Update accessibility service status
			updateAccessibilityServiceStatus()
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error in onViewCreated: ${e.message}", e)
			// Show error to user safely
			context?.let { ctx ->
				Toast.makeText(ctx, ctx.getString(R.string.toast_error_loading_keywords, e.message ?: ""), Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Initializes view references
	 */
	private fun initViews(view: View) {
		try {
			recyclerView = view.findViewById(R.id.keyword_recycler)
			addButton = view.findViewById(R.id.add_keyword_fab)
			clearAllButton = view.findViewById(R.id.clear_all_button)
			
			// New UI elements
			enableServiceButton = view.findViewById(R.id.enable_service_button)
			testApiButton = view.findViewById(R.id.test_api_button)
			helpButton = view.findViewById(R.id.help_button)
			serviceStatusText = view.findViewById(R.id.service_status_text)
			serviceDescriptionText = view.findViewById(R.id.service_description_text)
			statusIndicator = view.findViewById(R.id.status_indicator)
			
			// Setup toolbar menu
			val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
			toolbar.setOnMenuItemClickListener { menuItem ->
				when (menuItem.itemId) {
					R.id.action_test_api -> {
						try {
							findNavController().navigate(R.id.apiTestFragment)
							true
						} catch (e: Exception) {
							Log.e("KeywordListFragment", "Error navigating to API test: ${e.message}", e)
							false
						}
					}
					else -> false
				}
			}
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error initializing views: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				Toast.makeText(context, "Error initializing interface: ${e.message}", Toast.LENGTH_LONG).show()
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
		
		val context = context
		if (context == null) {
			Log.w("KeywordListFragment", "Context is null, cannot setup RecyclerView")
			return
		}
		
		recyclerView.apply {
			adapter = keywordAdapter
			layoutManager = LinearLayoutManager(context)
		}

    }

    private fun renderApps(apps: List<com.internetguard.pro.data.model.AppInfo>, container: android.widget.LinearLayout) {
        container.removeAllViews()
        val context = context
        if (context == null) {
            Log.w("KeywordListFragment", "Context is null, cannot render apps")
            return
        }
        
        apps.forEach { appInfo ->
            val cb = android.widget.CheckBox(context).apply {
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
        try {
            val dialog = AssignKeywordToAppsDialog.newInstance(keyword)
            dialog.show(parentFragmentManager, "AssignKeywordToAppsDialog")
        } catch (e: Exception) {
            Log.e("KeywordListFragment", "Error showing assign keyword dialog: ${e.message}", e)
            val context = context
            if (context != null) {
                Toast.makeText(context, "Error opening app assignment dialog", Toast.LENGTH_SHORT).show()
            }
        }
    }
	
	/**
	 * Sets up ViewModel
	 */
	private fun setupViewModel() {
		try {
			viewModel = ViewModelProvider(this)[KeywordListViewModel::class.java]
			aiSuggestionEngine = IntelligentKeywordSuggestionEngine()
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error setting up ViewModel: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				Toast.makeText(context, "Error initializing keyword system: ${e.message}", Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Sets up click listeners
	 */
	private fun setupClickListeners() {
		try {
			addButton.setOnClickListener {
				// Check accessibility permission before adding keyword
				checkAccessibilityPermissionBeforeAction {
					showAddKeywordDialog()
				}
			}
			
			clearAllButton.setOnClickListener {
				showClearAllConfirmDialog()
			}
			
			// New button click listeners
			enableServiceButton.setOnClickListener {
				try {
					val activity = activity
					if (activity != null) {
						val permissionManager = com.internetguard.pro.security.PermissionManager(activity)
						permissionManager.initialize()
						permissionManager.requestAccessibilityService()
					} else {
						Log.w("KeywordListFragment", "Activity is null, cannot enable accessibility service")
						context?.let { ctx ->
							Toast.makeText(ctx, "Cannot enable service: Activity not available", Toast.LENGTH_SHORT).show()
						}
					}
				} catch (e: Exception) {
					Log.e("KeywordListFragment", "Error enabling accessibility service: ${e.message}", e)
					context?.let { ctx ->
						Toast.makeText(ctx, "Error enabling service: ${e.message}", Toast.LENGTH_SHORT).show()
					}
				}
			}
			
			testApiButton.setOnClickListener {
				try {
					findNavController().navigate(R.id.apiTestFragment)
				} catch (e: Exception) {
					Log.e("KeywordListFragment", "Error navigating to API test: ${e.message}", e)
				}
			}
			
			helpButton.setOnClickListener {
				showHelpDialog()
			}
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error setting up click listeners: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				Toast.makeText(context, "Error setting up interface: ${e.message}", Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Checks accessibility permission before performing an action
	 */
	private fun checkAccessibilityPermissionBeforeAction(action: () -> Unit) {
		try {
			val activity = activity
			if (activity == null) {
				Log.w("KeywordListFragment", "Activity is null, proceeding with action")
				action()
				return
			}
			
			val permissionManager = com.internetguard.pro.security.PermissionManager(activity)
			permissionManager.initialize()
			
			if (permissionManager.isAccessibilityServiceEnabled()) {
				// Permission is granted, proceed with action
				action()
			} else {
				// Show permission request with callback
				showPermissionRequiredDialog {
					permissionManager.requestAccessibilityService()
				}
			}
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error checking accessibility permission before action: ${e.message}", e)
			// Proceed with action anyway to avoid blocking user
			action()
		}
	}
	
	/**
	 * Shows dialog explaining that accessibility permission is required
	 */
	private fun showPermissionRequiredDialog(onEnable: () -> Unit) {
		val context = context
		if (context == null) {
			Log.w("KeywordListFragment", "Context is null, cannot show permission dialog")
			return
		}
		
		androidx.appcompat.app.AlertDialog.Builder(context)
			.setTitle(context.getString(R.string.dialog_permission_required_title))
			.setMessage(context.getString(R.string.dialog_permission_required_message))
			.setPositiveButton(context.getString(R.string.button_enable_now)) { _, _ ->
				onEnable()
			}
			.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
				dialog.dismiss()
			}
			.setCancelable(false)
			.show()
	}
	
	/**
	 * Observes ViewModel LiveData
	 */
	private fun observeViewModel() {
		try {
			viewModel.keywordList.observe(viewLifecycleOwner) { keywords ->
				keywordAdapter.submitList(keywords)
			}
			
			viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
				errorMessage?.let {
					showErrorSnackbar(it)
					viewModel.clearError()
				}
			}
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error observing ViewModel: ${e.message}", e)
			// Show error to user
			val context = context
			if (context != null) {
				Toast.makeText(context, "Error loading keyword data: ${e.message}", Toast.LENGTH_LONG).show()
			}
		}
	}
	
	/**
	 * Checks accessibility service permission and requests if needed
	 */
	private fun checkAccessibilityPermission() {
		try {
			val activity = activity
			if (activity == null) {
				Log.w("KeywordListFragment", "Activity is null, cannot check accessibility permission")
				return
			}
			
			val permissionManager = com.internetguard.pro.security.PermissionManager(activity)
			permissionManager.initialize()
			
			// Check if accessibility service is enabled
			if (!permissionManager.isAccessibilityServiceEnabled()) {
				// Show permission request dialog
				permissionManager.requestAccessibilityService()
			}
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error checking accessibility permission: ${e.message}", e)
		}
	}
	
	override fun onResume() {
		super.onResume()
		// Update accessibility service status when returning to fragment
		updateAccessibilityServiceStatus()
	}
	
	/**
	 * Updates accessibility service status display
	 */
	private fun updateAccessibilityServiceStatus() {
		try {
			val context = context ?: return
			val serviceInfo = AccessibilityServiceChecker.getServiceInfo(context)
			
			// Update status text safely
			serviceStatusText?.text = serviceInfo.status
			serviceDescriptionText?.text = serviceInfo.description
			
			// Update button visibility and status indicator safely
			if (serviceInfo.isEnabled) {
				enableServiceButton?.visibility = View.GONE
				statusIndicator?.setBackgroundColor(
					context.getColor(android.R.color.holo_green_light)
				)
			} else {
				enableServiceButton?.visibility = View.VISIBLE
				statusIndicator?.setBackgroundColor(
					context.getColor(android.R.color.holo_red_light)
				)
			}
		} catch (e: Exception) {
			Log.e("KeywordListFragment", "Error updating accessibility service status: ${e.message}", e)
			// Set default values on error
			try {
				serviceStatusText?.text = "Error"
				serviceDescriptionText?.text = "Unable to check service status"
				enableServiceButton?.visibility = View.VISIBLE
			} catch (e2: Exception) {
				Log.e("KeywordListFragment", "Error setting default values: ${e2.message}", e2)
			}
		}
	}
	
	/**
	 * Shows help dialog
	 */
	private fun showHelpDialog() {
		val context = context ?: return
		
		androidx.appcompat.app.AlertDialog.Builder(context)
			.setTitle(context.getString(R.string.dialog_help_keyword_blocking_title))
			.setMessage(context.getString(R.string.dialog_help_keyword_blocking_message))
			.setPositiveButton(context.getString(R.string.button_got_it)) { dialog, _ ->
				dialog.dismiss()
			}
			.setNeutralButton(context.getString(R.string.button_enable_service)) { _, _ ->
				try {
					val activity = activity
					if (activity != null) {
						val permissionManager = com.internetguard.pro.security.PermissionManager(activity)
						permissionManager.initialize()
						permissionManager.requestAccessibilityService()
					} else {
						Log.w("KeywordListFragment", "Activity is null in help dialog")
						Toast.makeText(context, context.getString(R.string.toast_cannot_enable_service), Toast.LENGTH_SHORT).show()
					}
				} catch (e: Exception) {
					Log.e("KeywordListFragment", "Error enabling service from help dialog: ${e.message}", e)
					Toast.makeText(context, context.getString(R.string.toast_error_enabling_service, e.message ?: ""), Toast.LENGTH_SHORT).show()
				}
			}
			.show()
	}
	
	/**
	 * Shows dialog for adding new keyword with AI suggestions
	 */
	private fun showAddKeywordDialog() {
		val context = context
		if (context == null) {
			Log.w("KeywordListFragment", "Context is null, cannot show add keyword dialog")
			return
		}
		
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
                try {
                    val suggestUrl = com.internetguard.pro.ai.api.RemoteConfig.SUGGEST_URL
                    val client = com.internetguard.pro.ai.api.LocalBackendClient(endpoint = suggestUrl, timeoutMs = 2500)
                    val suggest = client.suggest(inputKeyword, null, null)
                    val any = suggest.synonyms.isNotEmpty() || suggest.variants.isNotEmpty() || suggest.obfuscations.isNotEmpty()
                    withContext(Dispatchers.Main) {
                        if (any) {
                            try {
                                renderSuggestionChips(suggest, aiSuggestionsGroup, keywordInput)
                                Toast.makeText(context, "AI suggestions loaded", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("KeywordListFragment", "Error rendering suggestion chips: ${e.message}", e)
                                try {
                                    Toast.makeText(context, "Error displaying suggestions: ${e.message}", Toast.LENGTH_SHORT).show()
                                } catch (e2: Exception) {
                                    Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
                                }
                            }
                        } else {
                            try {
                                showAISuggestions(inputKeyword, aiSuggestionsGroup, keywordInput)
                            } catch (e: Exception) {
                                Log.e("KeywordListFragment", "Error showing AI suggestions: ${e.message}", e)
                                try {
                                    Toast.makeText(context, "Error generating suggestions: ${e.message}", Toast.LENGTH_SHORT).show()
                                } catch (e2: Exception) {
                                    Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("KeywordListFragment", "Error generating AI suggestions: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        try {
                            showAISuggestions(inputKeyword, aiSuggestionsGroup, keywordInput)
                        } catch (e2: Exception) {
                            Log.e("KeywordListFragment", "Error showing fallback AI suggestions: ${e2.message}", e2)
                            try {
                                Toast.makeText(context, "Error generating suggestions: ${e2.message}", Toast.LENGTH_SHORT).show()
                            } catch (e3: Exception) {
                                Log.e("KeywordListFragment", "Error showing error toast: ${e3.message}", e3)
                            }
                        }
                    }
                }
            }
        }
		
        // Inject apps UI controls
        val appsContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.apps_container)
        val appsSearch = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.apps_search_input)
        val applyAll = dialogView.findViewById<android.widget.Switch>(R.id.apply_all_switch)

        // Load apps lazily
        val activity = activity
        if (activity == null) {
            Log.w("KeywordListFragment", "Activity is null, cannot load apps")
            return
        }
        
        val app = activity.application as com.internetguard.pro.InternetGuardProApp
        val db = app.database
        val repo = com.internetguard.pro.data.repository.AppRepository(db.appBlockRulesDao(), context.applicationContext)
        val allApps = java.util.concurrent.atomic.AtomicReference<List<com.internetguard.pro.data.model.AppInfo>>(emptyList())
        viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val list = repo.getInstalledApps().sortedBy { it.appName }
                allApps.set(list)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        renderApps(list, appsContainer)
                    } catch (e: Exception) {
                        Log.e("KeywordListFragment", "Error rendering apps: ${e.message}", e)
                        try {
                            Toast.makeText(context, "Error displaying apps: ${e.message}", Toast.LENGTH_SHORT).show()
                        } catch (e2: Exception) {
                            Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("KeywordListFragment", "Error loading apps: ${e.message}", e)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        Toast.makeText(context, "Error loading apps: ${e.message}", Toast.LENGTH_SHORT).show()
                    } catch (e2: Exception) {
                        Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
                    }
                }
            }
        }

        // Search filter
        appsSearch?.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                try {
                    val q = s?.toString()?.trim()?.lowercase().orEmpty()
                    val list = allApps.get()
                    val filtered = if (q.isEmpty()) list else list.filter { it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
                    try {
                        renderApps(filtered, appsContainer)
                    } catch (e: Exception) {
                        Log.e("KeywordListFragment", "Error rendering filtered apps: ${e.message}", e)
                    }
                } catch (e: Exception) {
                    Log.e("KeywordListFragment", "Error filtering apps: ${e.message}", e)
                }
            }
        })

        // Apply all toggle
        applyAll?.setOnCheckedChangeListener { _, isChecked ->
            try {
                appsContainer.visibility = if (isChecked) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Log.e("KeywordListFragment", "Error toggling apply all: ${e.message}", e)
            }
        }

        val alert = AlertDialog.Builder(context)
            .setTitle("Add New Keyword")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        alert.setOnShowListener {
            val addBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE)
            addBtn.setOnClickListener {
                try {
                    val keyword = keywordInput.text.toString().trim()
                    val category = categoryInput.text.toString().trim().takeIf { it.isNotEmpty() }
                    val language = languageInput.text.toString().trim().takeIf { it.isNotEmpty() }
                    val caseSensitive = caseSensitiveSwitch.isChecked
                    if (keyword.isEmpty()) {
                        Toast.makeText(context, "Please enter a keyword", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            // 1) Upsert keyword
                            val newKeyword = KeywordBlacklist(
                                keyword = keyword,
                                category = category,
                                caseSensitive = caseSensitive,
                                language = language,
                                createdAt = System.currentTimeMillis()
                            )
                            val keywordId = (activity.application as com.internetguard.pro.InternetGuardProApp).database.keywordBlacklistDao().upsert(newKeyword)

                            // 2) Build rules
                            val selectedPackages = try {
                                collectSelectedPackages(appsContainer)
                            } catch (e: Exception) {
                                Log.e("KeywordListFragment", "Error collecting selected packages: ${e.message}", e)
                                emptyList()
                            }
                            val targetPackages: List<String> = try {
                                if (applyAll?.isChecked == true) {
                                    // Apply to all apps
                                    allApps.get().map { it.packageName }
                                } else if (selectedPackages.isNotEmpty()) {
                                    // Apply to selected apps only
                                    selectedPackages
                                } else {
                                    // If no app is selected, global block (for all apps)
                                    emptyList() // Global keyword - will block in all apps
                                }
                            } catch (e: Exception) {
                                Log.e("KeywordListFragment", "Error determining target packages: ${e.message}", e)
                                emptyList()
                            }

                            val dao = (activity.application as com.internetguard.pro.InternetGuardProApp).database.appKeywordRulesDao()
                            
                            if (targetPackages.isEmpty()) {
                                // Global keyword - no rules needed
                                Log.d("KeywordListFragment", "Keyword added as global (applies to all apps)")
                            } else {
                                // Assign to specific apps
                                try {
                                    targetPackages.forEach { pkg ->
                                        dao.insert(com.internetguard.pro.data.entities.AppKeywordRules(appPackageName = pkg, keywordId = keywordId, isEnabled = true))
                                    }
                                    Log.d("KeywordListFragment", "Keyword assigned to ${targetPackages.size} apps")
                                } catch (e: Exception) {
                                    Log.e("KeywordListFragment", "Error inserting keyword rules: ${e.message}", e)
                                }
                            }

                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                try {
                                    alert.dismiss()
                                } catch (e: Exception) {
                                    Log.e("KeywordListFragment", "Error dismissing dialog: ${e.message}", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("KeywordListFragment", "Error adding keyword: ${e.message}", e)
                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                try {
                                    Toast.makeText(context, "Error adding keyword: ${e.message}", Toast.LENGTH_LONG).show()
                                } catch (e2: Exception) {
                                    Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("KeywordListFragment", "Error in add button click: ${e.message}", e)
                    try {
                        Toast.makeText(context, "Error adding keyword: ${e.message}", Toast.LENGTH_LONG).show()
                    } catch (e2: Exception) {
                        Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
                    }
                }
            }
        }

        try {
            alert.show()
        } catch (e: Exception) {
            Log.e("KeywordListFragment", "Error showing add keyword dialog: ${e.message}", e)
            try {
                Toast.makeText(context, "Error opening dialog: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e2: Exception) {
                Log.e("KeywordListFragment", "Error showing error toast: ${e2.message}", e2)
            }
        }
	}
	
	/**
	 * Show AI-generated suggestions
	 */
	private fun showAISuggestions(keyword: String, chipGroup: ChipGroup, keywordInput: EditText) {
        // Clear previous suggestions
        chipGroup.removeAllViews()
        
        val context = context
        if (context == null) {
            Log.w("KeywordListFragment", "Context is null, cannot show AI suggestions")
            return
        }
        
        // Generate AI suggestions
        val suggestions = aiSuggestionEngine.generateSuggestions(keyword)
        
        if (suggestions.isEmpty()) {
            Toast.makeText(context, "No suggestions found", Toast.LENGTH_SHORT).show()
            return
        }

        // Create chips for each suggestion
        suggestions.forEach { suggestion ->
            val chip = Chip(context)
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
        
        val context = context
        if (context == null) {
            Log.w("KeywordListFragment", "Context is null, cannot render suggestion chips")
            return
        }
        
        all.forEach { suggestion ->
            val chip = com.google.android.material.chip.Chip(context)
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
		val context = context
		if (context == null) {
			Log.w("KeywordListFragment", "Context is null, cannot show edit keyword dialog")
			return
		}
		
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
		
		AlertDialog.Builder(context)
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
		val context = context
		if (context == null) {
			Log.w("KeywordListFragment", "Context is null, cannot show delete confirm dialog")
			return
		}
		
		AlertDialog.Builder(context)
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
		val context = context
		if (context == null) {
			Log.w("KeywordListFragment", "Context is null, cannot show clear all confirm dialog")
			return
		}
		
		AlertDialog.Builder(context)
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
		val view = view
		if (view == null) {
			Log.w("KeywordListFragment", "View is null, cannot show error snackbar")
			return
		}
		
		Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
	}
}

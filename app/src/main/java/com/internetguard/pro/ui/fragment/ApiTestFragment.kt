package com.internetguard.pro.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.internetguard.pro.R
import com.internetguard.pro.test.ApiKeyTester
import kotlinx.coroutines.launch

/**
 * Fragment for testing API key functionality
 */
class ApiTestFragment : Fragment() {
	
	private lateinit var btnRunTest: MaterialButton
	private lateinit var progressIndicator: ProgressBar
	private lateinit var resultsCard: MaterialCardView
	private lateinit var resultsText: TextView
	
	private val tester = ApiKeyTester()
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_api_test, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		initViews(view)
		setupClickListeners()
	}
	
	private fun initViews(view: View) {
		btnRunTest = view.findViewById(R.id.btn_run_test)
		progressIndicator = view.findViewById(R.id.progress_indicator)
		resultsCard = view.findViewById(R.id.results_card)
		resultsText = view.findViewById(R.id.results_text)
	}
	
	private fun setupClickListeners() {
		btnRunTest.setOnClickListener {
			runTests()
		}
	}
	
	private fun runTests() {
		// Show loading
		btnRunTest.isEnabled = false
		progressIndicator.visibility = View.VISIBLE
		resultsCard.visibility = View.GONE
		
		// Run tests
		viewLifecycleOwner.lifecycleScope.launch {
			try {
				val results = tester.runAllTests()
				val formattedResults = tester.formatResults(results)
				
				// Show results
				resultsText.text = formattedResults
				resultsCard.visibility = View.VISIBLE
				
			} catch (e: Exception) {
				resultsText.text = "Error running tests: ${e.message}\n\n${e.stackTraceToString()}"
				resultsCard.visibility = View.VISIBLE
			} finally {
				// Hide loading
				progressIndicator.visibility = View.GONE
				btnRunTest.isEnabled = true
			}
		}
	}
} 
package com.internetguard.pro.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.internetguard.pro.R
import com.internetguard.pro.test.SimpleCodeTester
import kotlinx.coroutines.launch

/**
 * Test Activity for running code integrity tests
 */
class TestActivity : AppCompatActivity() {
    
    private lateinit var testResultsText: TextView
    private lateinit var runTestsButton: Button
    private lateinit var clearResultsButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        testResultsText = findViewById(R.id.test_results_text)
        runTestsButton = findViewById(R.id.run_tests_button)
        clearResultsButton = findViewById(R.id.clear_results_button)
        
        // Set initial text
        testResultsText.text = "Click 'Run Tests' to start code integrity testing..."
    }
    
    private fun setupClickListeners() {
        runTestsButton.setOnClickListener {
            runTests()
        }
        
        clearResultsButton.setOnClickListener {
            testResultsText.text = "Results cleared. Click 'Run Tests' to start testing..."
        }
    }
    
    private fun runTests() {
        runTestsButton.isEnabled = false
        runTestsButton.text = "Running Tests..."
        testResultsText.text = "🧪 Running Code Integrity Tests...\n\nPlease wait..."
        
        lifecycleScope.launch {
            try {
                val results = SimpleCodeTester.runBasicTests(this@TestActivity)
                
                runOnUiThread {
                    testResultsText.text = results.getSummary()
                    runTestsButton.isEnabled = true
                    runTestsButton.text = "Run Tests Again"
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    testResultsText.text = "❌ Test execution failed: ${e.message}\n\n${e.stackTraceToString()}"
                    runTestsButton.isEnabled = true
                    runTestsButton.text = "Run Tests Again"
                }
            }
        }
    }
}

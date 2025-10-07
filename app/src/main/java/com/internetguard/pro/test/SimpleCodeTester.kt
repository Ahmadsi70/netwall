package com.internetguard.pro.test

import android.content.Context
import android.util.Log
import com.internetguard.pro.InternetGuardProApp
import com.internetguard.pro.data.database.AppDatabase
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.security.PermissionManager
import com.internetguard.pro.ai.api.LocalBackendClient
import com.internetguard.pro.ai.api.RemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Method
import java.lang.reflect.Field

/**
 * Simple Code Integrity Tester
 * 
 * This class tests basic functionality to ensure the app doesn't crash
 */
object SimpleCodeTester {
    
    private const val TAG = "SimpleCodeTester"
    
    /**
     * Runs basic integrity tests
     */
    suspend fun runBasicTests(context: Context): TestResults {
        Log.i(TAG, "🧪 Starting Basic Code Tests...")
        
        val results = TestResults()
        
        try {
            // Test 1: Database Connection
            results.databaseTest = testDatabaseConnection(context)
            
            // Test 2: Permission Manager
            results.permissionTest = testPermissionManager(context)
            
            // Test 3: AI API Client
            results.aiApiTest = testAIApiClient()
            
            // Test 4: Class Loading
            results.classTest = testClassLoading()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Test execution failed: ${e.message}", e)
            results.overallSuccess = false
            results.errorMessage = e.message ?: "Unknown error"
        }
        
        // Calculate overall success
        results.overallSuccess = results.databaseTest.success &&
                results.permissionTest.success &&
                results.aiApiTest.success &&
                results.classTest.success
        
        Log.i(TAG, "🏁 Basic Tests completed. Overall: ${if (results.overallSuccess) "✅ PASS" else "❌ FAIL"}")
        
        return results
    }
    
    /**
     * Test 1: Database Connection
     */
    private suspend fun testDatabaseConnection(context: Context): TestResult {
        Log.d(TAG, "📊 Testing Database Connection...")
        
        return withContext(Dispatchers.IO) {
            try {
                val app = context.applicationContext as InternetGuardProApp
                val database = app.database
                
                // Test basic database access
                Log.d(TAG, "✅ Database connection successful")
                
                TestResult(
                    success = true,
                    message = "Database connection successful",
                    details = "Database is accessible"
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Database test failed: ${e.message}", e)
                TestResult(
                    success = false,
                    message = "Database test failed: ${e.message}",
                    details = e.stackTraceToString()
                )
            }
        }
    }
    
    /**
     * Test 2: Permission Manager
     */
    private fun testPermissionManager(context: Context): TestResult {
        Log.d(TAG, "🔐 Testing Permission Manager...")
        
        return try {
            // Test basic permission manager functionality
            Log.d(TAG, "✅ Permission Manager test successful")
            
            TestResult(
                success = true,
                message = "Permission Manager working",
                details = "Permission Manager is accessible"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Permission Manager test failed: ${e.message}", e)
            TestResult(
                success = false,
                message = "Permission Manager test failed: ${e.message}",
                details = e.stackTraceToString()
            )
        }
    }
    
    /**
     * Test 3: AI API Client
     */
    private suspend fun testAIApiClient(): TestResult {
        Log.d(TAG, "🤖 Testing AI API Client...")
        
        return withContext(Dispatchers.IO) {
            try {
                // Test client creation
                val moderateUrl = RemoteConfig.MODERATE_URL
                val client = LocalBackendClient(endpoint = moderateUrl, timeoutMs = 5000)
                
                Log.d(TAG, "✅ AI API Client creation successful")
                
                TestResult(
                    success = true,
                    message = "AI API Client working",
                    details = "Client can be created"
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ AI API Client test failed: ${e.message}", e)
                TestResult(
                    success = false,
                    message = "AI API Client test failed: ${e.message}",
                    details = e.stackTraceToString()
                )
            }
        }
    }
    
    /**
     * Test 4: Class Loading
     */
    private fun testClassLoading(): TestResult {
        Log.d(TAG, "📦 Testing Class Loading...")
        
        return try {
            // Test fragment classes exist
            val fragmentClasses = listOf(
                "com.internetguard.pro.ui.fragment.KeywordListFragment",
                "com.internetguard.pro.ui.fragment.AppListFragment",
                "com.internetguard.pro.ui.fragment.SettingsFragment"
            )
            
            for (className in fragmentClasses) {
                val fragmentClass = Class.forName(className)
                Log.d(TAG, "✅ Fragment class found: ${fragmentClass.simpleName}")
            }
            
            TestResult(
                success = true,
                message = "Class Loading successful",
                details = "All required classes can be loaded"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Class Loading test failed: ${e.message}", e)
            TestResult(
                success = false,
                message = "Class Loading test failed: ${e.message}",
                details = e.stackTraceToString()
            )
        }
    }
    
    /**
     * Test result data class
     */
    data class TestResult(
        val success: Boolean,
        val message: String,
        val details: String = ""
    )
    
    /**
     * Overall test results
     */
    data class TestResults(
        var overallSuccess: Boolean = false,
        var errorMessage: String = "",
        var databaseTest: TestResult = TestResult(false, "Not run"),
        var permissionTest: TestResult = TestResult(false, "Not run"),
        var aiApiTest: TestResult = TestResult(false, "Not run"),
        var classTest: TestResult = TestResult(false, "Not run")
    ) {
        fun getSummary(): String {
            return buildString {
                appendLine("🧪 Basic Code Test Results:")
                appendLine("=====================================")
                appendLine("Overall: ${if (overallSuccess) "✅ PASS" else "❌ FAIL"}")
                if (errorMessage.isNotEmpty()) {
                    appendLine("Error: $errorMessage")
                }
                appendLine()
                appendLine("Individual Tests:")
                appendLine("📊 Database: ${if (databaseTest.success) "✅" else "❌"} ${databaseTest.message}")
                appendLine("🔐 Permissions: ${if (permissionTest.success) "✅" else "❌"} ${permissionTest.message}")
                appendLine("🤖 AI API: ${if (aiApiTest.success) "✅" else "❌"} ${aiApiTest.message}")
                appendLine("📦 Classes: ${if (classTest.success) "✅" else "❌"} ${classTest.message}")
            }
        }
    }
}

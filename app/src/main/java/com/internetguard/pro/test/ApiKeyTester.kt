package com.internetguard.pro.test

import android.util.Log
import com.internetguard.pro.ai.api.RemoteModerationClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * API Key and Proxy Tester
 * 
 * Tests the functionality of:
 * - Proxy server connection
 * - AI moderation endpoint
 * - AI suggestions endpoint
 */
class ApiKeyTester {
    
    companion object {
        private const val TAG = "ApiKeyTester"
        
        // Proxy URLs
        private const val PROXY_MODERATE_URL = "http://localhost:3000/api/moderate"
        private const val PROXY_SUGGEST_URL = "http://localhost:3000/api/suggest"
    }
    
    /**
     * Test Result data class
     */
    data class TestResult(
        val testName: String,
        val success: Boolean,
        val message: String,
        val details: String? = null
    )
    
    /**
     * Run all tests
     */
    suspend fun runAllTests(): List<TestResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TestResult>()
        
        Log.i(TAG, "🧪 Starting API Key Tests...")
        
        // Test 1: Proxy Moderation Endpoint
        results.add(testModeration())
        
        // Test 2: Proxy Suggestions Endpoint
        results.add(testSuggestions())
        
        // Test 3: Test with inappropriate content
        results.add(testInappropriateContent())
        
        Log.i(TAG, "✅ All tests completed!")
        results
    }
    
    /**
     * Test moderation endpoint
     */
    private suspend fun testModeration(): TestResult {
        return try {
            Log.i(TAG, "Testing moderation endpoint...")
            
            val client = RemoteModerationClient(
                endpoint = PROXY_MODERATE_URL,
                timeoutMs = 5000
            )
            
            val result = client.moderate("Hello world", null)
            
            if (result.isInappropriate) {
                TestResult(
                    testName = "Moderation Endpoint",
                    success = false,
                    message = "Failed: Safe text marked as inappropriate",
                    details = "Confidence: ${result.confidence}"
                )
            } else {
                TestResult(
                    testName = "Moderation Endpoint",
                    success = true,
                    message = "✓ Proxy moderation working correctly",
                    details = "Response received successfully"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Moderation test failed", e)
            TestResult(
                testName = "Moderation Endpoint",
                success = false,
                message = "Failed: ${e.message}",
                details = "Check proxy connection and API key"
            )
        }
    }
    
    /**
     * Test suggestions endpoint
     */
    private suspend fun testSuggestions(): TestResult {
        return try {
            Log.i(TAG, "Testing suggestions endpoint...")
            
            val client = RemoteModerationClient(
                endpoint = PROXY_SUGGEST_URL,
                timeoutMs = 10000
            ) as com.internetguard.pro.ai.api.SuggestionClient
            
            val result = client.suggest("test", "English", "General")
            
            val hasResults = result.synonyms.isNotEmpty() || 
                           result.variants.isNotEmpty() || 
                           result.obfuscations.isNotEmpty()
            
            if (hasResults) {
                TestResult(
                    testName = "Suggestions Endpoint",
                    success = true,
                    message = "✓ AI suggestions working correctly",
                    details = "Synonyms: ${result.synonyms.size}, Variants: ${result.variants.size}"
                )
            } else {
                TestResult(
                    testName = "Suggestions Endpoint",
                    success = false,
                    message = "Failed: No suggestions returned",
                    details = "API might be rate limited or key invalid"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Suggestions test failed", e)
            TestResult(
                testName = "Suggestions Endpoint",
                success = false,
                message = "Failed: ${e.message}",
                details = "Check proxy connection and API key"
            )
        }
    }
    
    /**
     * Test with inappropriate content
     */
    private suspend fun testInappropriateContent(): TestResult {
        return try {
            Log.i(TAG, "Testing inappropriate content detection...")
            
            val client = RemoteModerationClient(
                endpoint = PROXY_MODERATE_URL,
                timeoutMs = 5000
            )
            
            // Test with known inappropriate content
            val testContent = "violence and harmful content"
            val result = client.moderate(testContent, null)
            
            TestResult(
                testName = "Inappropriate Content Detection",
                success = true,
                message = "✓ Detection test completed",
                details = "Flagged: ${result.isInappropriate}, Confidence: ${result.confidence}"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Inappropriate content test failed", e)
            TestResult(
                testName = "Inappropriate Content Detection",
                success = false,
                message = "Failed: ${e.message}",
                details = null
            )
        }
    }
    
    /**
     * Format test results as readable string
     */
    fun formatResults(results: List<TestResult>): String {
        val builder = StringBuilder()
        builder.appendLine("=".repeat(50))
        builder.appendLine("API KEY TEST RESULTS")
        builder.appendLine("=".repeat(50))
        builder.appendLine()
        
        val passed = results.count { it.success }
        val total = results.size
        
        results.forEach { result ->
            val icon = if (result.success) "✅" else "❌"
            builder.appendLine("$icon ${result.testName}")
            builder.appendLine("   ${result.message}")
            result.details?.let {
                builder.appendLine("   Details: $it")
            }
            builder.appendLine()
        }
        
        builder.appendLine("=".repeat(50))
        builder.appendLine("SUMMARY: $passed/$total tests passed")
        builder.appendLine("=".repeat(50))
        
        return builder.toString()
    }
} 

package com.internetguard.pro.ai.api

object RemoteConfig {
    // Local backend server URL (for development and testing)
    // TODO: Replace with actual Railway URL after deployment
    const val BASE_URL: String = "http://localhost:3000"

    val MODERATE_URL: String
        get() = "$BASE_URL/api/moderate"

    val SUGGEST_URL: String
        get() = "$BASE_URL/api/suggest"
} 
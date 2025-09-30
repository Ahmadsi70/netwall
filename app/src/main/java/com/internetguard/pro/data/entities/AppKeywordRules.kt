package com.internetguard.pro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing app-specific keyword blocking rules.
 * 
 * This entity stores which keywords should be blocked for specific applications,
 * allowing users to have different keyword rules for different apps.
 * 
 * @property id Unique identifier for the rule (auto-generated)
 * @property appPackageName Package name of the application
 * @property keywordId ID of the keyword to block in this app
 * @property isEnabled Whether this rule is currently active
 * @property createdAt Timestamp when this rule was created
 */
@Entity(
    tableName = "app_keyword_rules",
    primaryKeys = ["appPackageName", "keywordId"]
)
data class AppKeywordRules(
    val appPackageName: String,
    val keywordId: Long,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

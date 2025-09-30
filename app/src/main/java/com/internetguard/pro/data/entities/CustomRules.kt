package com.internetguard.pro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing custom rules for app blocking based on various conditions.
 */
@Entity(tableName = "custom_rules")
data class CustomRules(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val ruleName: String,
	val ruleType: String, // "time", "location", "battery", "data_usage", "context"
	val isEnabled: Boolean = true,
	val conditions: String, // JSON string containing rule conditions
	val actions: String, // JSON string containing rule actions
	val priority: Int = 0, // Higher number = higher priority
	val createdAt: Long,
	val lastTriggered: Long? = null
)

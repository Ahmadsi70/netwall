package com.internetguard.pro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing per-app internet control rules.
 * 
 * This entity stores the blocking configuration for individual applications,
 * including which network types (WiFi, Cellular) should be blocked and
 * the blocking mode (always, schedule, etc.).
 * 
 * @property id Unique identifier for the rule (auto-generated)
 * @property appUid Android UID of the application
 * @property appPackageName Package name of the application
 * @property appName Display name of the application
 * @property blockWifi Whether WiFi access should be blocked for this app
 * @property blockCellular Whether cellular access should be blocked for this app
 * @property blockMode The blocking mode (e.g., "always", "schedule", "time_limit")
 * @property createdAt Timestamp when this rule was created
 */
@Entity(tableName = "app_block_rules")
data class AppBlockRules(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val appUid: Int,
	val appPackageName: String,
	val appName: String,
	val blockWifi: Boolean,
	val blockCellular: Boolean,
	val blockMode: String,
	val createdAt: Long
)

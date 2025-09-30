package com.internetguard.pro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing user-defined keywords/phrases to block.
 * 
 * This entity stores keywords and phrases that should be blocked across
 * applications and web content. Supports multiple languages and categories
 * for better organization and management.
 * 
 * @property id Unique identifier for the keyword (auto-generated)
 * @property keyword The actual keyword or phrase to block
 * @property category Optional category for organizing keywords (e.g., "Content", "Gambling")
 * @property caseSensitive Whether the keyword matching should be case-sensitive
 * @property language Language code for the keyword (e.g., "en", "fa", "zh")
 * @property createdAt Timestamp when this keyword was added
 */
@Entity(tableName = "keyword_blacklist")
data class KeywordBlacklist(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val keyword: String,
	val category: String?,
	val caseSensitive: Boolean,
	val language: String?,
	val createdAt: Long
)

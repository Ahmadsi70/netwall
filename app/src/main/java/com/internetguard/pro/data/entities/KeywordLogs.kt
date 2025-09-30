package com.internetguard.pro.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a log of blocked keyword occurrence.
 */
@Entity(
	tableName = "keyword_logs",
	foreignKeys = [
		ForeignKey(
			entity = KeywordBlacklist::class,
			parentColumns = ["id"],
			childColumns = ["keywordId"],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [Index(value = ["keywordId"])])
data class KeywordLogs(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val keywordId: Long,
	val appPackageName: String,
	val attemptTime: Long,
	val blockedContent: String
)

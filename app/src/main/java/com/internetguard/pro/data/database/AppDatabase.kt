package com.internetguard.pro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.internetguard.pro.data.daos.AppBlockRulesDao
import com.internetguard.pro.data.daos.KeywordBlacklistDao
import com.internetguard.pro.data.daos.KeywordLogsDao
import com.internetguard.pro.data.daos.CustomRulesDao
import com.internetguard.pro.data.daos.AppKeywordRulesDao
import com.internetguard.pro.data.entities.AppBlockRules
import com.internetguard.pro.data.entities.KeywordBlacklist
import com.internetguard.pro.data.entities.KeywordLogs
import com.internetguard.pro.data.entities.CustomRules
import com.internetguard.pro.data.entities.AppKeywordRules

/**
 * Main application Room database, encrypted via SQLCipher SupportFactory
 * configured in [com.internetguard.pro.InternetGuardProApp].
 */
@Database(
	entities = [
		AppBlockRules::class,
		KeywordBlacklist::class,
		KeywordLogs::class,
		CustomRules::class,
		AppKeywordRules::class
	],
	version = 5,
	exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun appBlockRulesDao(): AppBlockRulesDao
	abstract fun keywordBlacklistDao(): KeywordBlacklistDao
	abstract fun keywordLogsDao(): KeywordLogsDao
	abstract fun customRulesDao(): CustomRulesDao
	abstract fun appKeywordRulesDao(): AppKeywordRulesDao

	companion object {
		@Volatile
		private var INSTANCE: AppDatabase? = null

		fun getDatabase(context: Context): AppDatabase = getInstance(context)

		fun getInstance(context: Context): AppDatabase {
			return INSTANCE ?: synchronized(this) {
				INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
			}
		}

		private fun buildDatabase(appContext: Context): AppDatabase {
			return Room.databaseBuilder(
				appContext,
				AppDatabase::class.java,
				"internetguard.db"
			)
			.fallbackToDestructiveMigration()
			.build()
		}
	}
}

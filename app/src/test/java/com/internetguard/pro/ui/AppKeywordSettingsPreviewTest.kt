package com.internetguard.pro.ui

import com.internetguard.pro.data.model.AppInfo
import com.internetguard.pro.data.model.AppKeywordSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AppKeywordSettingsPreviewTest {

    @Test
    fun previewText_withMoreThanThreeShowsCounter() {
        val settings = AppKeywordSettings(
            appInfo = AppInfo(packageName = "com.test", appName = "Test", icon = null, uid = 0),
            hasKeywordFiltering = true,
            activeKeywordsCount = 5,
            previewKeywords = listOf("a","b","c","d","e")
        )
        assertEquals("a, b, c (+2)", settings.previewText)
    }

    @Test
    fun previewText_withTwoShowsTwo() {
        val settings = AppKeywordSettings(
            appInfo = AppInfo(packageName = "com.test", appName = "Test", icon = null, uid = 0),
            hasKeywordFiltering = true,
            activeKeywordsCount = 2,
            previewKeywords = listOf("x","y")
        )
        assertEquals("x, y", settings.previewText)
    }
}



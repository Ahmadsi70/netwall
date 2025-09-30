package com.internetguard.pro.ui.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.internetguard.pro.R
import com.internetguard.pro.ui.activity.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for KeywordListFragment.
 * 
 * Tests UI flows for adding, editing, and managing keyword blocking rules.
 * Includes tests for multi-language keyword support.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class KeywordListFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Test that keyword list fragment is displayed.
     */
    @Test
    fun keywordListFragment_shouldDisplayCorrectly() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Verify keyword list fragment is displayed
        onView(withId(R.id.keyword_list_fragment))
            .check(matches(isDisplayed()))
        
        // Verify add button is displayed
        onView(withId(R.id.fab_add_keyword))
            .check(matches(isDisplayed()))
    }

    /**
     * Test adding a new keyword.
     */
    @Test
    fun addKeyword_shouldOpenDialog() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Click add button
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        // Verify add keyword dialog is displayed
        onView(withId(R.id.dialog_add_keyword))
            .check(matches(isDisplayed()))
        
        // Verify input fields are displayed
        onView(withId(R.id.et_keyword))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.spinner_language))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.cb_regex))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.cb_case_sensitive))
            .check(matches(isDisplayed()))
    }

    /**
     * Test adding an English keyword.
     */
    @Test
    fun addEnglishKeyword_shouldAddSuccessfully() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Click add button
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        // Enter keyword
        onView(withId(R.id.et_keyword))
            .perform(typeText("violence"))
        
        // Select English language
        onView(withId(R.id.spinner_language))
            .perform(click())
        
        onView(withText("English"))
            .perform(click())
        
        // Click save button
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Verify keyword is added to list
        onView(withText("violence"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test adding a Persian keyword.
     */
    @Test
    fun addPersianKeyword_shouldAddSuccessfully() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Click add button
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        // Enter keyword
        onView(withId(R.id.et_keyword))
            .perform(typeText("خشونت"))
        
        // Select Persian language
        onView(withId(R.id.spinner_language))
            .perform(click())
        
        onView(withText("Persian"))
            .perform(click())
        
        // Click save button
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Verify keyword is added to list
        onView(withText("خشونت"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test adding a regex keyword.
     */
    @Test
    fun addRegexKeyword_shouldAddSuccessfully() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Click add button
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        // Enter regex keyword
        onView(withId(R.id.et_keyword))
            .perform(typeText(".*gambling.*"))
        
        // Check regex checkbox
        onView(withId(R.id.cb_regex))
            .perform(click())
        
        // Select English language
        onView(withId(R.id.spinner_language))
            .perform(click())
        
        onView(withText("English"))
            .perform(click())
        
        // Click save button
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Verify regex keyword is added to list
        onView(withText(".*gambling.*"))
            .check(matches(isDisplayed()))
    }

    /**
     * Test editing an existing keyword.
     */
    @Test
    fun editKeyword_shouldOpenEditDialog() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Add a keyword first
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("test"))
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Long click on the keyword to edit
        onView(withText("test"))
            .perform(longClick())
        
        // Verify edit dialog is displayed
        onView(withId(R.id.dialog_edit_keyword))
            .check(matches(isDisplayed()))
    }

    /**
     * Test deleting a keyword.
     */
    @Test
    fun deleteKeyword_shouldRemoveFromList() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Add a keyword first
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("test"))
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Long click on the keyword to delete
        onView(withText("test"))
            .perform(longClick())
        
        // Click delete button
        onView(withId(R.id.btn_delete))
            .perform(click())
        
        // Confirm deletion
        onView(withText("Yes"))
            .perform(click())
        
        // Verify keyword is removed from list
        onView(withText("test"))
            .check(matches(isNotDisplayed()))
    }

    /**
     * Test searching keywords.
     */
    @Test
    fun searchKeywords_shouldFilterResults() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Add multiple keywords
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("violence"))
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("gambling"))
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Search for "violence"
        onView(withId(R.id.et_search))
            .perform(typeText("violence"))
        
        // Verify only "violence" is displayed
        onView(withText("violence"))
            .check(matches(isDisplayed()))
        
        onView(withText("gambling"))
            .check(matches(isNotDisplayed()))
    }

    /**
     * Test filtering keywords by language.
     */
    @Test
    fun filterKeywordsByLanguage_shouldShowOnlySelectedLanguage() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Add English keyword
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("violence"))
        
        onView(withId(R.id.spinner_language))
            .perform(click())
        
        onView(withText("English"))
            .perform(click())
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Add Persian keyword
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("خشونت"))
        
        onView(withId(R.id.spinner_language))
            .perform(click())
        
        onView(withText("Persian"))
            .perform(click())
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Filter by English
        onView(withId(R.id.spinner_filter_language))
            .perform(click())
        
        onView(withText("English"))
            .perform(click())
        
        // Verify only English keyword is displayed
        onView(withText("violence"))
            .check(matches(isDisplayed()))
        
        onView(withText("خشونت"))
            .check(matches(isNotDisplayed()))
    }

    /**
     * Test toggling keyword active status.
     */
    @Test
    fun toggleKeywordActive_shouldUpdateStatus() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Add a keyword
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        onView(withId(R.id.et_keyword))
            .perform(typeText("test"))
        
        onView(withId(R.id.btn_save))
            .perform(click())
        
        // Toggle active status
        onView(withId(R.id.switch_active))
            .perform(click())
        
        // Verify status is updated
        onView(withId(R.id.switch_active))
            .check(matches(isNotChecked()))
    }

    /**
     * Test canceling add keyword dialog.
     */
    @Test
    fun cancelAddKeyword_shouldCloseDialog() {
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Click add button
        onView(withId(R.id.fab_add_keyword))
            .perform(click())
        
        // Click cancel button
        onView(withId(R.id.btn_cancel))
            .perform(click())
        
        // Verify dialog is closed
        onView(withId(R.id.dialog_add_keyword))
            .check(matches(isNotDisplayed()))
    }
}

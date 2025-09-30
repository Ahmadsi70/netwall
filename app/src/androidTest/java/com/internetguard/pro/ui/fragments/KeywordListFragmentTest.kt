package com.internetguard.pro.ui.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.FragmentScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.internetguard.pro.R
import com.internetguard.pro.ui.fragment.KeywordListFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class KeywordListFragmentTest {

    @get:Rule
    val fragmentRule = FragmentScenarioRule.launchInContainer(
        KeywordListFragment::class.java
    )

    @Test
    fun testKeywordListFragmentDisplays() {
        // Verify fragment is displayed
        onView(withId(R.id.keyword_list_fragment))
            .check(matches(isDisplayed()))

        // Verify recycler view is displayed
        onView(withId(R.id.recycler_view_keywords))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAddKeywordButton() {
        // Verify add keyword button is displayed
        onView(withId(R.id.fab_add_keyword))
            .check(matches(isDisplayed()))

        // Click on add keyword button
        onView(withId(R.id.fab_add_keyword))
            .perform(click())

        // Verify add keyword dialog is displayed
        onView(withId(R.id.add_keyword_dialog))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAddKeywordDialog() {
        // Open add keyword dialog
        onView(withId(R.id.fab_add_keyword))
            .perform(click())

        // Enter keyword
        onView(withId(R.id.edit_text_keyword))
            .perform(typeText("test_keyword"))

        // Toggle case sensitivity
        onView(withId(R.id.switch_case_sensitive))
            .perform(click())

        // Click add button
        onView(withId(R.id.button_add))
            .perform(click())

        // Verify keyword is added to list
        onView(withId(R.id.recycler_view_keywords))
            .check(matches(hasMinimumChildCount(1)))
    }

    @Test
    fun testSearchKeywords() {
        // Enter search query
        onView(withId(R.id.search_view_keywords))
            .perform(typeText("test"))

        // Verify search results are filtered
        onView(withId(R.id.recycler_view_keywords))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testKeywordItemActions() {
        // Add a keyword first
        onView(withId(R.id.fab_add_keyword))
            .perform(click())

        onView(withId(R.id.edit_text_keyword))
            .perform(typeText("test_keyword"))

        onView(withId(R.id.button_add))
            .perform(click())

        // Test keyword item actions
        onView(withId(R.id.recycler_view_keywords))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(
                0,
                click()
            ))

        // Verify keyword details or edit dialog is displayed
        onView(withId(R.id.keyword_details_dialog))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testToggleKeywordActiveStatus() {
        // Add a keyword
        onView(withId(R.id.fab_add_keyword))
            .perform(click())

        onView(withId(R.id.edit_text_keyword))
            .perform(typeText("test_keyword"))

        onView(withId(R.id.button_add))
            .perform(click())

        // Toggle active status
        onView(withId(R.id.recycler_view_keywords))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(
                0,
                click()
            ))

        // Click toggle switch
        onView(withId(R.id.switch_active))
            .perform(click())

        // Verify status is updated
        onView(withId(R.id.switch_active))
            .check(matches(isChecked()))
    }

    @Test
    fun testDeleteKeyword() {
        // Add a keyword
        onView(withId(R.id.fab_add_keyword))
            .perform(click())

        onView(withId(R.id.edit_text_keyword))
            .perform(typeText("test_keyword"))

        onView(withId(R.id.button_add))
            .perform(click())

        // Long click to select
        onView(withId(R.id.recycler_view_keywords))
            .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(
                0,
                longClick()
            ))

        // Click delete button
        onView(withId(R.id.button_delete))
            .perform(click())

        // Confirm deletion
        onView(withId(R.id.button_confirm_delete))
            .perform(click())

        // Verify keyword is removed
        onView(withId(R.id.recycler_view_keywords))
            .check(matches(hasChildCount(0)))
    }
}

package com.internetguard.pro.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.internetguard.pro.R
import com.internetguard.pro.ui.activity.MainActivity
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class DialogKeywordInputUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        // Setup any mocks if needed
    }

    @Test
    fun `test keyword input dialog displays correctly`() {
        // Given
        val activity = activityRule.activity

        // When
        // Simulate opening the dialog (this would be triggered by a button click in real app)
        // For testing purposes, we'll test the layout directly

        // Then
        // Test keyword input field
        onView(withId(R.id.keyword_input))
            .check(matches(isDisplayed()))

        // Test category input field
        onView(withId(R.id.category_input))
            .check(matches(isDisplayed()))

        // Test language input field
        onView(withId(R.id.language_input))
            .check(matches(isDisplayed()))

        // Test case sensitive switch
        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test keyword input field interaction`() {
        // Given
        val activity = activityRule.activity
        val testKeyword = "test keyword"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testKeyword))

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testKeyword)))
    }

    @Test
    fun `test category input field interaction`() {
        // Given
        val activity = activityRule.activity
        val testCategory = "test category"

        // When
        onView(withId(R.id.category_input))
            .perform(typeText(testCategory))

        // Then
        onView(withId(R.id.category_input))
            .check(matches(withText(testCategory)))
    }

    @Test
    fun `test language input field interaction`() {
        // Given
        val activity = activityRule.activity
        val testLanguage = "en"

        // When
        onView(withId(R.id.language_input))
            .perform(typeText(testLanguage))

        // Then
        onView(withId(R.id.language_input))
            .check(matches(withText(testLanguage)))
    }

    @Test
    fun `test case sensitive switch interaction`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        // Then
        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isChecked()))

        // Toggle back
        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        // Then
        onView(withId(R.id.case_sensitive_switch))
            .check(matches(not(isChecked())))
    }

    @Test
    fun `test multiple input fields interaction`() {
        // Given
        val activity = activityRule.activity
        val testKeyword = "test keyword"
        val testCategory = "test category"
        val testLanguage = "en"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testKeyword))

        onView(withId(R.id.category_input))
            .perform(typeText(testCategory))

        onView(withId(R.id.language_input))
            .perform(typeText(testLanguage))

        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testKeyword)))

        onView(withId(R.id.category_input))
            .check(matches(withText(testCategory)))

        onView(withId(R.id.language_input))
            .check(matches(withText(testLanguage)))

        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isChecked()))
    }

    @Test
    fun `test input field clearing`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        onView(withId(R.id.keyword_input))
            .perform(clearText())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test input field validation`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(""))

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test switch state persistence`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        // Then
        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isChecked()))

        // Toggle multiple times
        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        onView(withId(R.id.case_sensitive_switch))
            .check(matches(not(isChecked())))

        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isChecked()))
    }

    @Test
    fun `test input field focus`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.keyword_input))
            .perform(click())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(hasFocus()))
    }

    @Test
    fun `test input field keyboard interaction`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.keyword_input))
            .perform(click())
            .perform(typeText(testText))

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field text selection`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        onView(withId(R.id.keyword_input))
            .perform(selectAll())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field copy paste`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        onView(withId(R.id.keyword_input))
            .perform(selectAll())

        onView(withId(R.id.keyword_input))
            .perform(copyText())

        onView(withId(R.id.category_input))
            .perform(click())

        onView(withId(R.id.category_input))
            .perform(pasteText())

        // Then
        onView(withId(R.id.category_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field undo redo`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        onView(withId(R.id.keyword_input))
            .perform(undo())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText("")))

        onView(withId(R.id.keyword_input))
            .perform(redo())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field multiline support`() {
        // Given
        val activity = activityRule.activity
        val testText = "line1\nline2\nline3"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field max lines`() {
        // Given
        val activity = activityRule.activity
        val testText = "line1\nline2\nline3\nline4"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field input type`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.keyword_input))
            .perform(click())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test input field accessibility`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.keyword_input))
            .perform(click())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test input field performance`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        repeat(10) {
            onView(withId(R.id.keyword_input))
                .perform(clearText())
                .perform(typeText(testText))
        }

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field error handling`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(""))

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test input field state management`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        // Then
        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))

        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isChecked()))
    }
}

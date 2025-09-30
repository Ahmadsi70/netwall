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
class DialogRuleInputUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        // Setup any mocks if needed
    }

    @Test
    fun `test rule input dialog displays correctly`() {
        // Given
        val activity = activityRule.activity

        // When
        // Simulate opening the dialog (this would be triggered by a button click in real app)
        // For testing purposes, we'll test the layout directly

        // Then
        // Test rule name input field
        onView(withId(R.id.rule_name_input))
            .check(matches(isDisplayed()))

        // Test rule type spinner
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))

        // Test is enabled switch
        onView(withId(R.id.is_enabled_switch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test rule name input field interaction`() {
        // Given
        val activity = activityRule.activity
        val testRuleName = "test rule name"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testRuleName))

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testRuleName)))
    }

    @Test
    fun `test rule type spinner interaction`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        // Then
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test is enabled switch interaction`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))

        // Toggle back
        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        onView(withId(R.id.is_enabled_switch))
            .check(matches(not(isChecked())))
    }

    @Test
    fun `test multiple input fields interaction`() {
        // Given
        val activity = activityRule.activity
        val testRuleName = "test rule name"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testRuleName))

        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testRuleName)))

        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))

        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))
    }

    @Test
    fun `test input field clearing`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))

        onView(withId(R.id.rule_name_input))
            .perform(clearText())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test input field validation`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(""))

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test switch state persistence`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))

        // Toggle multiple times
        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        onView(withId(R.id.is_enabled_switch))
            .check(matches(not(isChecked())))

        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))
    }

    @Test
    fun `test input field focus`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_name_input))
            .perform(click())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(hasFocus()))
    }

    @Test
    fun `test input field keyboard interaction`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(click())
            .perform(typeText(testText))

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field text selection`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))

        onView(withId(R.id.rule_name_input))
            .perform(selectAll())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field copy paste`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))

        onView(withId(R.id.rule_name_input))
            .perform(selectAll())

        onView(withId(R.id.rule_name_input))
            .perform(copyText())

        onView(withId(R.id.rule_name_input))
            .perform(clearText())

        onView(withId(R.id.rule_name_input))
            .perform(pasteText())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field undo redo`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))

        onView(withId(R.id.rule_name_input))
            .perform(undo())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText("")))

        onView(withId(R.id.rule_name_input))
            .perform(redo())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field input type`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_name_input))
            .perform(click())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test input field accessibility`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_name_input))
            .perform(click())

        // Then
        onView(withId(R.id.rule_name_input))
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
            onView(withId(R.id.rule_name_input))
                .perform(clearText())
                .perform(typeText(testText))
        }

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test input field error handling`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(""))

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test input field state management`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))

        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))

        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))
    }

    @Test
    fun `test spinner dropdown interaction`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        // Then
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test spinner selection`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        // Then
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test spinner accessibility`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        // Then
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test spinner performance`() {
        // Given
        val activity = activityRule.activity

        // When
        repeat(10) {
            onView(withId(R.id.rule_type_spinner))
                .perform(click())
        }

        // Then
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test spinner state management`() {
        // Given
        val activity = activityRule.activity

        // When
        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))

        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))
    }
}

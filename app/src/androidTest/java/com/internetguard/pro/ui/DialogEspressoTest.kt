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
class DialogEspressoTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        // Setup any mocks if needed
    }

    @Test
    fun `test complete keyword dialog ui with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test complete keyword dialog UI

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

        // Test case sensitive text
        onView(withText("Case sensitive"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test complete rule dialog ui with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test complete rule dialog UI

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

        // Test is enabled text
        onView(withText("Enabled"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test keyword dialog input interactions with espresso`() {
        // Given
        val activity = activityRule.activity
        val testKeyword = "test keyword"
        val testCategory = "test category"
        val testLanguage = "en"

        // When
        // Test keyword input interactions
        onView(withId(R.id.keyword_input))
            .perform(typeText(testKeyword))

        onView(withId(R.id.category_input))
            .perform(typeText(testCategory))

        onView(withId(R.id.language_input))
            .perform(typeText(testLanguage))

        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        // Then
        // Verify inputs
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
    fun `test rule dialog input interactions with espresso`() {
        // Given
        val activity = activityRule.activity
        val testRuleName = "test rule name"

        // When
        // Test rule input interactions
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testRuleName))

        onView(withId(R.id.rule_type_spinner))
            .perform(click())

        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        // Then
        // Verify inputs
        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testRuleName)))

        onView(withId(R.id.rule_type_spinner))
            .check(matches(isDisplayed()))

        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))
    }

    @Test
    fun `test dialog button interactions with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test dialog button interactions

        // Then
        // Test positive button
        onView(withText("Add"))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())

        // Test negative button
        onView(withText("Cancel"))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())

        // Test save button
        onView(withText("Save"))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
    }

    @Test
    fun `test dialog switch interactions with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test dialog switch interactions

        // Then
        // Test case sensitive switch
        onView(withId(R.id.case_sensitive_switch))
            .perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.case_sensitive_switch))
            .perform(click())
            .check(matches(not(isChecked())))

        // Test is enabled switch
        onView(withId(R.id.is_enabled_switch))
            .perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.is_enabled_switch))
            .perform(click())
            .check(matches(not(isChecked())))
    }

    @Test
    fun `test dialog input field validation with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test input field validation

        // Then
        // Test empty keyword input
        onView(withId(R.id.keyword_input))
            .perform(typeText(""))

        onView(withId(R.id.keyword_input))
            .check(matches(withText("")))

        // Test empty rule name input
        onView(withId(R.id.rule_name_input))
            .perform(typeText(""))

        onView(withId(R.id.rule_name_input))
            .check(matches(withText("")))
    }

    @Test
    fun `test dialog input field clearing with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        // Test input field clearing

        // Then
        // Test keyword input clearing
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))
            .perform(clearText())
            .check(matches(withText("")))

        // Test rule name input clearing
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))
            .perform(clearText())
            .check(matches(withText("")))
    }

    @Test
    fun `test dialog input field copy paste with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        // Test input field copy paste

        // Then
        // Test keyword input copy paste
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))
            .perform(selectAll())
            .perform(copyText())

        onView(withId(R.id.category_input))
            .perform(click())
            .perform(pasteText())
            .check(matches(withText(testText)))

        // Test rule name input copy paste
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))
            .perform(selectAll())
            .perform(copyText())

        onView(withId(R.id.rule_name_input))
            .perform(clearText())
            .perform(pasteText())
            .check(matches(withText(testText)))
    }

    @Test
    fun `test dialog input field undo redo with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        // Test input field undo redo

        // Then
        // Test keyword input undo redo
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))
            .perform(undo())
            .check(matches(withText("")))

        onView(withId(R.id.keyword_input))
            .perform(redo())
            .check(matches(withText(testText)))

        // Test rule name input undo redo
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))
            .perform(undo())
            .check(matches(withText("")))

        onView(withId(R.id.rule_name_input))
            .perform(redo())
            .check(matches(withText(testText)))
    }

    @Test
    fun `test dialog input field multiline with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "line1\nline2\nline3"

        // When
        // Test input field multiline

        // Then
        // Test keyword input multiline
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test dialog input field focus with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test input field focus

        // Then
        // Test keyword input focus
        onView(withId(R.id.keyword_input))
            .perform(click())
            .check(matches(hasFocus()))

        // Test rule name input focus
        onView(withId(R.id.rule_name_input))
            .perform(click())
            .check(matches(hasFocus()))
    }

    @Test
    fun `test dialog input field keyboard with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        // Test input field keyboard

        // Then
        // Test keyword input keyboard
        onView(withId(R.id.keyword_input))
            .perform(click())
            .perform(typeText(testText))
            .check(matches(withText(testText)))

        // Test rule name input keyboard
        onView(withId(R.id.rule_name_input))
            .perform(click())
            .perform(typeText(testText))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test dialog input field accessibility with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test input field accessibility

        // Then
        // Test keyword input accessibility
        onView(withId(R.id.keyword_input))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // Test rule name input accessibility
        onView(withId(R.id.rule_name_input))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test dialog input field performance with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        // Test input field performance

        // Then
        // Test keyword input performance
        repeat(10) {
            onView(withId(R.id.keyword_input))
                .perform(clearText())
                .perform(typeText(testText))
        }

        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))

        // Test rule name input performance
        repeat(10) {
            onView(withId(R.id.rule_name_input))
                .perform(clearText())
                .perform(typeText(testText))
        }

        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))
    }

    @Test
    fun `test dialog input field error handling with espresso`() {
        // Given
        val activity = activityRule.activity

        // When
        // Test input field error handling

        // Then
        // Test keyword input error handling
        onView(withId(R.id.keyword_input))
            .perform(typeText(""))
            .check(matches(withText("")))

        // Test rule name input error handling
        onView(withId(R.id.rule_name_input))
            .perform(typeText(""))
            .check(matches(withText("")))
    }

    @Test
    fun `test dialog input field state management with espresso`() {
        // Given
        val activity = activityRule.activity
        val testText = "test text"

        // When
        // Test input field state management

        // Then
        // Test keyword input state management
        onView(withId(R.id.keyword_input))
            .perform(typeText(testText))

        onView(withId(R.id.case_sensitive_switch))
            .perform(click())

        onView(withId(R.id.keyword_input))
            .check(matches(withText(testText)))

        onView(withId(R.id.case_sensitive_switch))
            .check(matches(isChecked()))

        // Test rule name input state management
        onView(withId(R.id.rule_name_input))
            .perform(typeText(testText))

        onView(withId(R.id.is_enabled_switch))
            .perform(click())

        onView(withId(R.id.rule_name_input))
            .check(matches(withText(testText)))

        onView(withId(R.id.is_enabled_switch))
            .check(matches(isChecked()))
    }
}

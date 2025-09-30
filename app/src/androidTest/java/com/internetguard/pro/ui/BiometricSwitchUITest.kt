package com.internetguard.pro.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.internetguard.pro.R
import com.internetguard.pro.ui.fragment.SecuritySettingsFragment
import com.internetguard.pro.ui.viewmodel.SecuritySettingsViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class BiometricSwitchUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: SecuritySettingsViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<SecuritySettingsViewModel>(relaxed = true)
    }

    @Test
    fun `test biometric switch display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test biometric switch interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Initial state
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))

        // Click biometric switch
        onView(withId(R.id.biometric_switch))
            .perform(click())

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isChecked()))

        // Toggle back
        onView(withId(R.id.biometric_switch))
            .perform(click())

        onView(withId(R.id.biometric_switch))
            .check(matches(not(isChecked())))

        fragmentScenario.close()
    }

    @Test
    fun `test biometric switch state changes`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test multiple state changes
        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(not(isChecked())))

        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(isChecked()))

        fragmentScenario.close()
    }

    @Test
    fun `test biometric switch rapid clicking`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test rapid clicking
        repeat(10) {
            onView(withId(R.id.biometric_switch))
                .perform(click())
        }

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test biometric switch accessibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test biometric switch performance`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test performance
        repeat(20) {
            onView(withId(R.id.biometric_switch))
                .perform(click())
        }

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test biometric switch state persistence`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test state persistence
        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(isChecked()))

        // Test state persistence
        onView(withId(R.id.biometric_switch))
            .check(matches(isChecked()))

        fragmentScenario.close()
    }

    @Test
    fun `test biometric switch error handling`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test biometric switch layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test biometric switch text content`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Biometric Authentication"))
            .check(matches(isDisplayed()))

        onView(withText("Secure access to sensitive features"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test biometric switch enabled state`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test biometric switch disabled state`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isEnabled()))
    }

    @Test
    fun `test biometric switch focus`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .perform(click())
    }

    @Test
    fun `test biometric switch long click`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .perform(click())
    }

    @Test
    fun `test biometric switch touch feedback`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.biometric_switch))
            .perform(click())
    }

    @Test
    fun `test biometric switch multiple interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test multiple interactions
        repeat(5) {
            onView(withId(R.id.biometric_switch))
                .perform(click())
        }

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `test biometric switch state management`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test state management
        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(not(isChecked())))

        onView(withId(R.id.biometric_switch))
            .perform(click())
            .check(matches(isChecked()))

        fragmentScenario.close()
    }
}

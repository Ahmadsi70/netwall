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
import com.internetguard.pro.ui.fragment.SettingsFragment
import com.internetguard.pro.ui.viewmodel.SettingsViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class SettingsFragmentUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: SettingsViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<SettingsViewModel>(relaxed = true)
    }

    @Test
    fun `test settings fragment displays correctly`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test toolbar
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
            .check(matches(hasDescendant(withText("Settings"))))

        // Test appearance section
        onView(withText("Appearance"))
            .check(matches(isDisplayed()))

        // Test theme card
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withText("Theme"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.theme_value_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Light")))

        // Test language card
        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withText("Language"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_value_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("English")))

        // Test notifications section
        onView(withText("Notifications"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.notification_switch))
            .check(matches(isDisplayed()))

        onView(withText("Enable Notifications"))
            .check(matches(isDisplayed()))

        onView(withText("Receive alerts about blocked content and app activity"))
            .check(matches(isDisplayed()))

        // Test privacy section
        onView(withText("Privacy & Security"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        onView(withText("Privacy Settings"))
            .check(matches(isDisplayed()))

        onView(withText("Manage your privacy and data settings"))
            .check(matches(isDisplayed()))

        // Test data management section
        onView(withText("Data Management"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.export_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Export Data"))
            .check(matches(isDisplayed()))

        onView(withText("Export your rules and settings"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.import_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Import Data"))
            .check(matches(isDisplayed()))

        onView(withText("Import rules and settings from file"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.clear_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        onView(withText("Permanently delete all app data"))
            .check(matches(isDisplayed()))

        // Test about section
        onView(withText("About"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.about_card))
            .check(matches(isDisplayed()))

        onView(withText("About InternetGuard Pro"))
            .check(matches(isDisplayed()))

        onView(withText("Version 1.0.0"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test theme card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click theme card
        onView(withId(R.id.theme_card))
            .perform(click())

        // Then
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withText("Theme"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.theme_value_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test language card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click language card
        onView(withId(R.id.language_card))
            .perform(click())

        // Then
        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withText("Language"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_value_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test notification switch interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Initial state
        onView(withId(R.id.notification_switch))
            .check(matches(isDisplayed()))

        // Click notification switch
        onView(withId(R.id.notification_switch))
            .perform(click())

        // Then
        onView(withId(R.id.notification_switch))
            .check(matches(isChecked()))

        // Toggle back
        onView(withId(R.id.notification_switch))
            .perform(click())

        onView(withId(R.id.notification_switch))
            .check(matches(not(isChecked())))

        fragmentScenario.close()
    }

    @Test
    fun `test notification switch state changes`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test multiple state changes
        onView(withId(R.id.notification_switch))
            .perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.notification_switch))
            .perform(click())
            .check(matches(not(isChecked())))

        onView(withId(R.id.notification_switch))
            .perform(click())
            .check(matches(isChecked()))

        fragmentScenario.close()
    }

    @Test
    fun `test privacy card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click privacy card
        onView(withId(R.id.privacy_card))
            .perform(click())

        // Then
        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        onView(withText("Privacy Settings"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test export data card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click export data card
        onView(withId(R.id.export_data_card))
            .perform(click())

        // Then
        onView(withId(R.id.export_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Export Data"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test import data card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click import data card
        onView(withId(R.id.import_data_card))
            .perform(click())

        // Then
        onView(withId(R.id.import_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Import Data"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test clear data card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click clear data card
        onView(withId(R.id.clear_data_card))
            .perform(click())

        // Then
        onView(withId(R.id.clear_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test about card interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click about card
        onView(withId(R.id.about_card))
            .perform(click())

        // Then
        onView(withId(R.id.about_card))
            .check(matches(isDisplayed()))

        onView(withText("About InternetGuard Pro"))
            .check(matches(isDisplayed()))

        onView(withText("Version 1.0.0"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test multiple card interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test multiple card interactions
        onView(withId(R.id.theme_card))
            .perform(click())

        onView(withId(R.id.language_card))
            .perform(click())

        onView(withId(R.id.privacy_card))
            .perform(click())

        onView(withId(R.id.export_data_card))
            .perform(click())

        onView(withId(R.id.import_data_card))
            .perform(click())

        onView(withId(R.id.clear_data_card))
            .perform(click())

        onView(withId(R.id.about_card))
            .perform(click())

        // Then
        // Verify all cards are still displayed
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.export_data_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.import_data_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.clear_data_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.about_card))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test notification switch rapid clicking`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test rapid clicking
        repeat(10) {
            onView(withId(R.id.notification_switch))
                .perform(click())
        }

        // Then
        onView(withId(R.id.notification_switch))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test fragment lifecycle with ui updates`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.notification_switch))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements accessibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.notification_switch))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements text content`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Settings"))
            .check(matches(isDisplayed()))

        onView(withText("Appearance"))
            .check(matches(isDisplayed()))

        onView(withText("Theme"))
            .check(matches(isDisplayed()))

        onView(withText("Language"))
            .check(matches(isDisplayed()))

        onView(withText("Notifications"))
            .check(matches(isDisplayed()))

        onView(withText("Privacy & Security"))
            .check(matches(isDisplayed()))

        onView(withText("Data Management"))
            .check(matches(isDisplayed()))

        onView(withText("About"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }
}

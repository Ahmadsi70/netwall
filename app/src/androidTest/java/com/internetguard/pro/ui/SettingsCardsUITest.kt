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
class SettingsCardsUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: SettingsViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<SettingsViewModel>(relaxed = true)
    }

    @Test
    fun `test theme card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withText("Theme"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.theme_value_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Light")))

        fragmentScenario.close()
    }

    @Test
    fun `test language card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withText("Language"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_value_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("English")))

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
    fun `test privacy card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        onView(withText("Privacy Settings"))
            .check(matches(isDisplayed()))

        onView(withText("Manage your privacy and data settings"))
            .check(matches(isDisplayed()))

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
    fun `test export data card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.export_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Export Data"))
            .check(matches(isDisplayed()))

        onView(withText("Export your rules and settings"))
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
    fun `test import data card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.import_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Import Data"))
            .check(matches(isDisplayed()))

        onView(withText("Import rules and settings from file"))
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
    fun `test clear data card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.clear_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        onView(withText("Permanently delete all app data"))
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
    fun `test about card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

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
    fun `test card accessibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
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
    fun `test card performance`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test performance
        repeat(10) {
            onView(withId(R.id.theme_card))
                .perform(click())

            onView(withId(R.id.language_card))
                .perform(click())

            onView(withId(R.id.privacy_card))
                .perform(click())
        }

        // Then
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test card state management`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test state management
        onView(withId(R.id.theme_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .perform(click())
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test card error handling`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test card layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.theme_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test card text content`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Theme"))
            .check(matches(isDisplayed()))

        onView(withText("Language"))
            .check(matches(isDisplayed()))

        onView(withText("Privacy Settings"))
            .check(matches(isDisplayed()))

        onView(withText("Export Data"))
            .check(matches(isDisplayed()))

        onView(withText("Import Data"))
            .check(matches(isDisplayed()))

        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        onView(withText("About InternetGuard Pro"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }
}

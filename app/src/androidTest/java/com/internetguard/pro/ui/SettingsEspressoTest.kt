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
import com.internetguard.pro.ui.fragment.SettingsFragment
import com.internetguard.pro.ui.viewmodel.SecuritySettingsViewModel
import com.internetguard.pro.ui.viewmodel.SettingsViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class SettingsEspressoTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockSecurityViewModel: SecuritySettingsViewModel
    private lateinit var mockSettingsViewModel: SettingsViewModel

    @Before
    fun setup() {
        mockSecurityViewModel = mockk<SecuritySettingsViewModel>(relaxed = true)
        mockSettingsViewModel = mockk<SettingsViewModel>(relaxed = true)
    }

    @Test
    fun `test complete security settings fragment with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test toolbar
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
            .check(matches(hasDescendant(withText("Security Settings"))))

        // Test security score card
        onView(withId(R.id.security_score_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0/100")))

        onView(withText("Security Score"))
            .check(matches(isDisplayed()))

        // Test biometric card
        onView(withId(R.id.biometric_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))

        onView(withText("Biometric Authentication"))
            .check(matches(isDisplayed()))

        onView(withText("Secure access to sensitive features"))
            .check(matches(isDisplayed()))

        // Test encryption card
        onView(withId(R.id.encryption_card))
            .check(matches(isDisplayed()))

        onView(withText("Database Encryption"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.encryption_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Enabled")))

        // Test privacy card
        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        onView(withText("Advanced Privacy"))
            .check(matches(isDisplayed()))

        onView(withText("Private DNS, VPN Passthrough"))
            .check(matches(isDisplayed()))

        // Test security status card
        onView(withId(R.id.security_status_card))
            .check(matches(isDisplayed()))

        onView(withText("Security Status"))
            .check(matches(isDisplayed()))

        onView(withText("View detailed security analysis"))
            .check(matches(isDisplayed()))

        // Test clear data card
        onView(withId(R.id.clear_data_card))
            .check(matches(isDisplayed()))

        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        onView(withText("Permanently delete all app data"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test complete settings fragment with espresso`() {
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
    fun `test biometric switch interactions with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test biometric switch interactions
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
    fun `test notification switch interactions with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test notification switch interactions
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
    fun `test card interactions with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test card interactions
        onView(withId(R.id.theme_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.language_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.export_data_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.import_data_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.clear_data_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.about_card))
            .perform(click())
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test security settings card interactions with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test security settings card interactions
        onView(withId(R.id.biometric_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.encryption_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.security_status_card))
            .perform(click())
            .check(matches(isDisplayed()))

        onView(withId(R.id.clear_data_card))
            .perform(click())
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test switch rapid clicking with espresso`() {
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
    fun `test notification switch rapid clicking with espresso`() {
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
    fun `test fragment lifecycle with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))

        onView(withId(R.id.security_score_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test settings fragment lifecycle with espresso`() {
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
    fun `test ui elements accessibility with espresso`() {
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

        onView(withId(R.id.security_score_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.encryption_status_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test settings ui elements accessibility with espresso`() {
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
    fun `test ui elements text content with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Security Settings"))
            .check(matches(isDisplayed()))

        onView(withText("Security Score"))
            .check(matches(isDisplayed()))

        onView(withText("Biometric Authentication"))
            .check(matches(isDisplayed()))

        onView(withText("Database Encryption"))
            .check(matches(isDisplayed()))

        onView(withText("Advanced Privacy"))
            .check(matches(isDisplayed()))

        onView(withText("Security Status"))
            .check(matches(isDisplayed()))

        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test settings ui elements text content with espresso`() {
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

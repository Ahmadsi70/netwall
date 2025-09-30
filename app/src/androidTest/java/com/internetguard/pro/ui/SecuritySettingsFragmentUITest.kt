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
class SecuritySettingsFragmentUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: SecuritySettingsViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<SecuritySettingsViewModel>(relaxed = true)
    }

    @Test
    fun `test security settings fragment displays correctly`() {
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
    fun `test security score display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.security_score_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0/100")))

        onView(withText("Security Score"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test encryption status display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.encryption_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Enabled")))

        onView(withText("Database Encryption"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test privacy status display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Advanced Privacy"))
            .check(matches(isDisplayed()))

        onView(withText("Private DNS, VPN Passthrough"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test security status display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Security Status"))
            .check(matches(isDisplayed()))

        onView(withText("View detailed security analysis"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test clear data card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Clear All Data"))
            .check(matches(isDisplayed()))

        onView(withText("Permanently delete all app data"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test card interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test card clicks
        onView(withId(R.id.biometric_card))
            .perform(click())

        onView(withId(R.id.encryption_card))
            .perform(click())

        onView(withId(R.id.privacy_card))
            .perform(click())

        onView(withId(R.id.security_status_card))
            .perform(click())

        onView(withId(R.id.clear_data_card))
            .perform(click())

        // Then
        // Verify cards are still displayed
        onView(withId(R.id.biometric_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.encryption_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.privacy_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.security_status_card))
            .check(matches(isDisplayed()))

        onView(withId(R.id.clear_data_card))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test fragment lifecycle with ui updates`() {
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
    fun `test multiple switch interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<SecuritySettingsFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test multiple switch interactions
        repeat(5) {
            onView(withId(R.id.biometric_switch))
                .perform(click())
        }

        // Then
        onView(withId(R.id.biometric_switch))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements accessibility`() {
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
    fun `test ui elements text content`() {
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
}

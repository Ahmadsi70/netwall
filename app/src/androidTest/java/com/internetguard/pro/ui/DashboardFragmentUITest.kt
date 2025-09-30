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
import com.internetguard.pro.ui.fragment.DashboardFragment
import com.internetguard.pro.ui.viewmodel.DashboardViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class DashboardFragmentUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<DashboardViewModel>(relaxed = true)
    }

    @Test
    fun `test dashboard fragment displays correctly`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
            .check(matches(hasDescendant(withText("Dashboard"))))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))

        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0")))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn toggle button click`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        // Verify button text changes or other UI updates
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test refresh button click`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify refresh action is triggered
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn status indicator visibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        fragmentScenario.close()
    }

    @Test
    fun `test statistics cards display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0")))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ai suggestions section display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test progress indicator visibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.progress_indicator))
            .check(matches(isDisplayed()))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn toggle button state changes`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Initial state
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(withText("Connect")))

        onView(withId(R.id.vpn_status_text))
            .check(matches(withText("Not Connected")))

        // Click to toggle
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        // Verify UI updates (this would depend on actual implementation)
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test refresh button functionality`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())

        // Verify refresh action
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test blocked count text updates`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0")))

        // Verify the text is visible and has correct initial value
        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test time saved text updates`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        // Verify the text is visible and has correct initial value
        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn status card layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test statistics cards layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Blocked count card
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        // Time saved card
        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ai suggestions section layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test fragment lifecycle with ui updates`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test multiple button clicks`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Multiple clicks on VPN toggle
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())
            .perform(click())
            .perform(click())

        // Multiple clicks on refresh button
        onView(withId(R.id.refresh_button))
            .perform(click())
            .perform(click())
            .perform(click())

        // Then
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements accessibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements text content`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Dashboard"))
            .check(matches(isDisplayed()))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withText("Not Connected"))
            .check(matches(isDisplayed()))

        onView(withText("Connect"))
            .check(matches(isDisplayed()))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withText("Refresh"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }
}

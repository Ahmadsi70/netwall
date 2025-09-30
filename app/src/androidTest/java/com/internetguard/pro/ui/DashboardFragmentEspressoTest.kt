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
class DashboardFragmentEspressoTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<DashboardViewModel>(relaxed = true)
    }

    @Test
    fun `test complete dashboard fragment ui with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test toolbar
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
            .check(matches(hasDescendant(withText("Dashboard"))))

        // Test VPN status card
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))
            .check(matches(isEnabled()))

        // Test statistics cards
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

        // Test AI suggestions section
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))
            .check(matches(isEnabled()))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        // Test progress indicator
        onView(withId(R.id.progress_indicator))
            .check(matches(isDisplayed()))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn toggle button interaction with espresso`() {
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

        // Click VPN toggle
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        // Verify button is still displayed and enabled
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test refresh button interaction with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click refresh button
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify button is still displayed and enabled
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // Verify other elements are still displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test data display with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test blocked count display
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0")))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        // Test time saved display
        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        // Test VPN status display
        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test button states with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test VPN toggle button
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(withText("Connect")))

        // Test refresh button
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(withText("Refresh")))

        fragmentScenario.close()
    }

    @Test
    fun `test ui layout with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test toolbar layout
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withText("Dashboard"))
            .check(matches(isDisplayed()))

        // Test VPN status card layout
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        // Test statistics cards layout
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        // Test AI suggestions section layout
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test multiple interactions with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform multiple interactions
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify UI remains stable
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
    fun `test rapid clicking with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Rapid clicking on VPN toggle
        repeat(5) {
            onView(withId(R.id.vpn_toggle_button))
                .perform(click())
        }

        // Rapid clicking on refresh button
        repeat(5) {
            onView(withId(R.id.refresh_button))
                .perform(click())
        }

        // Then
        // Verify UI remains stable
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test fragment lifecycle with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify UI elements are displayed
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui responsiveness with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform various interactions quickly
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify UI remains responsive
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // Verify other elements are still visible
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test complete ui flow with espresso`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Test initial state
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(withText("Connect")))

        onView(withId(R.id.vpn_status_text))
            .check(matches(withText("Not Connected")))

        onView(withId(R.id.blocked_count_text))
            .check(matches(withText("0")))

        onView(withId(R.id.time_saved_text))
            .check(matches(withText("0m")))

        // Perform interactions
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify UI remains stable
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
}

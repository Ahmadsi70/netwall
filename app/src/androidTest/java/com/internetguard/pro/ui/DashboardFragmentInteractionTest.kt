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
class DashboardFragmentInteractionTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<DashboardViewModel>(relaxed = true)
    }

    @Test
    fun `test vpn toggle button interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Initial state check
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))

        onView(withId(R.id.vpn_status_text))
            .check(matches(withText("Not Connected")))

        // Perform click
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        // Verify button is still displayed and enabled
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test refresh button interaction`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Initial state check
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))
            .check(matches(isEnabled()))

        // Perform click
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify button is still displayed and enabled
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn status indicator interaction`() {
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

        // Click VPN toggle to change status
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Verify status indicator is still visible
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test statistics cards interaction`() {
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
            .check(matches(withText("0")))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        // Time saved card
        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        // Click refresh to update statistics
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Verify statistics are still displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ai suggestions section interaction`() {
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
            .perform(click())

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        // Verify section is still displayed after refresh
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test progress indicator interaction`() {
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

        // Click refresh to potentially show loading
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Verify progress indicator is still present
        onView(withId(R.id.progress_indicator))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test multiple vpn toggle clicks`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform multiple clicks
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())
            .perform(click())
            .perform(click())

        // Then
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test multiple refresh button clicks`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform multiple clicks
        onView(withId(R.id.refresh_button))
            .perform(click())
            .perform(click())
            .perform(click())

        // Then
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
    fun `test vpn toggle and refresh button sequence`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click VPN toggle
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Click refresh
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Click VPN toggle again
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements remain visible after interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform various interactions
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify all UI elements remain visible
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test button states after interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform interactions
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify buttons remain enabled and clickable
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // Verify buttons can still be clicked
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        fragmentScenario.close()
    }

    @Test
    fun `test text content updates after interactions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform interactions
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        // Verify text content is still displayed
        onView(withText("Dashboard"))
            .check(matches(isDisplayed()))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test rapid button clicking`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Rapid clicking on VPN toggle
        repeat(10) {
            onView(withId(R.id.vpn_toggle_button))
                .perform(click())
        }

        // Rapid clicking on refresh button
        repeat(10) {
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
    fun `test ui responsiveness during interactions`() {
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
}

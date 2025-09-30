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
class DashboardFragmentDataDisplayTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<DashboardViewModel>(relaxed = true)
    }

    @Test
    fun `test blocked count text initial display`() {
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

        fragmentScenario.close()
    }

    @Test
    fun `test time saved text initial display`() {
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

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn status text initial display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn toggle button initial display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test refresh button initial display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test statistics cards layout and content`() {
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

        fragmentScenario.close()
    }

    @Test
    fun `test vpn status card layout and content`() {
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

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))

        fragmentScenario.close()
    }

    @Test
    fun `test ai suggestions section layout and content`() {
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
    fun `test progress indicator initial state`() {
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
    fun `test toolbar content and layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withText("Dashboard"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test data display after refresh button click`() {
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
        // Verify data is still displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test data display after vpn toggle click`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click VPN toggle button
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        // Verify data is still displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test text content consistency`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify all text content is displayed consistently
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

    @Test
    fun `test numeric values display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify numeric values are displayed correctly
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0")))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        fragmentScenario.close()
    }

    @Test
    fun `test button text content`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify button text content
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))

        fragmentScenario.close()
    }

    @Test
    fun `test data display after multiple interactions`() {
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
        // Verify data is still displayed correctly
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test data display stability`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform rapid interactions
        repeat(5) {
            onView(withId(R.id.vpn_toggle_button))
                .perform(click())

            onView(withId(R.id.refresh_button))
                .perform(click())
        }

        // Then
        // Verify data display remains stable
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test data display after fragment lifecycle changes`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify data is displayed correctly after lifecycle changes
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }
}

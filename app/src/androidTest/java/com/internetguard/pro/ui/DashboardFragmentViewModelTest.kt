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
class DashboardFragmentViewModelTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: DashboardViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<DashboardViewModel>(relaxed = true)
    }

    @Test
    fun `test viewmodel integration with vpn toggle button`() {
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
        // Verify button is still displayed and enabled
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        // Verify VPN status text is displayed
        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel integration with refresh button`() {
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

        // Verify data elements are still displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with blocked count`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify blocked count is displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0")))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with time saved`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify time saved is displayed
        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0m")))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with vpn status`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify VPN status is displayed
        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with vpn toggle button`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify VPN toggle button is displayed
        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with refresh button`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify refresh button is displayed
        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with ai suggestions`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify AI suggestions section is displayed
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with progress indicator`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify progress indicator is displayed
        onView(withId(R.id.progress_indicator))
            .check(matches(isDisplayed()))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with toolbar`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify toolbar is displayed
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withText("Dashboard"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with vpn status indicator`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify VPN status indicator is displayed
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with statistics cards`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify statistics cards are displayed
        onView(withId(R.id.blocked_count_text))
            .check(matches(isDisplayed()))

        onView(withText("Content Blocked"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.time_saved_text))
            .check(matches(isDisplayed()))

        onView(withText("Time Saved"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with ai suggestions section`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify AI suggestions section is displayed
        onView(withText("AI Suggestions"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))

        onView(withId(R.id.suggestions_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with vpn status card`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify VPN status card is displayed
        onView(withId(R.id.vpn_status_indicator))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test viewmodel data binding with complete ui`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify complete UI is displayed
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
    fun `test viewmodel data binding after interactions`() {
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
        // Verify data binding remains stable
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
    fun `test viewmodel data binding with fragment lifecycle`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<DashboardFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Verify data binding works through lifecycle
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

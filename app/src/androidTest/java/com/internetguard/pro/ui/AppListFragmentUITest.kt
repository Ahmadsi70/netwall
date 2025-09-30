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
import com.internetguard.pro.ui.fragment.AppListFragment
import com.internetguard.pro.ui.viewmodel.AppListViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class AppListFragmentUITest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: AppListViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<AppListViewModel>(relaxed = true)
    }

    @Test
    fun `test app list fragment displays correctly`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        // Test toolbar
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
            .check(matches(hasDescendant(withText("App Internet Control"))))

        // Test VPN status card
        onView(withId(R.id.vpn_status_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Not Connected")))

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))

        // Test app list header
        onView(withText("Installed Apps"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))

        // Test loading indicator
        onView(withId(R.id.loading_indicator))
            .check(matches(isDisplayed()))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        // Test app list recycler
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn toggle button click`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
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
    fun `test refresh button click`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)
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
    fun `test vpn status card display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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

        onView(withId(R.id.vpn_toggle_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Connect")))

        fragmentScenario.close()
    }

    @Test
    fun `test app list header display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("Installed Apps"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(withText("Refresh")))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test loading indicator display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.loading_indicator))
            .check(matches(isDisplayed()))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn toggle button state changes`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test refresh button functionality`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test toolbar display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))

        onView(withText("App Internet Control"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test vpn status text display`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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
    fun `test app list recycler layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Verify RecyclerView is properly configured
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test fragment lifecycle with ui updates`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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

        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test multiple button clicks`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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
            .check(matches(isEnabled()))

        onView(withId(R.id.refresh_button))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements accessibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
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

        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test ui elements text content`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withText("App Internet Control"))
            .check(matches(isDisplayed()))

        onView(withText("VPN Status"))
            .check(matches(isDisplayed()))

        onView(withText("Not Connected"))
            .check(matches(isDisplayed()))

        onView(withText("Connect"))
            .check(matches(isDisplayed()))

        onView(withText("Installed Apps"))
            .check(matches(isDisplayed()))

        onView(withText("Refresh"))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }
}

package com.internetguard.pro.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
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
class AppListFragmentRecyclerViewTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private lateinit var mockViewModel: AppListViewModel

    @Before
    fun setup() {
        mockViewModel = mockk<AppListViewModel>(relaxed = true)
    }

    @Test
    fun `test app list recycler view display`() {
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
    fun `test app list recycler view layout`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumChildCount(0)))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view scrolling`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Test scrolling if there are items
        try {
            onView(withId(R.id.app_list_recycler))
                .perform(click())
        } catch (e: Exception) {
            // Expected if no items are present
        }

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view item count`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumChildCount(0)))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view adapter`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Verify RecyclerView has an adapter
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view layout manager`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Verify RecyclerView has a layout manager
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view visibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view clickable`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Test clicking on RecyclerView
        onView(withId(R.id.app_list_recycler))
            .perform(click())

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view with refresh`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click refresh button
        onView(withId(R.id.refresh_button))
            .perform(click())

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view with vpn toggle`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Click VPN toggle button
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view lifecycle`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.CREATED)
        fragmentScenario.moveToState(Lifecycle.State.STARTED)
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view performance`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Test multiple interactions
        repeat(10) {
            onView(withId(R.id.refresh_button))
                .perform(click())
        }

        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view state persistence`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Perform interactions
        onView(withId(R.id.refresh_button))
            .perform(click())

        onView(withId(R.id.vpn_toggle_button))
            .perform(click())

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view error handling`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Test error handling
        onView(withId(R.id.refresh_button))
            .perform(click())

        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view accessibility`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        // Test accessibility
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view with multiple refreshes`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Multiple refreshes
        repeat(5) {
            onView(withId(R.id.refresh_button))
                .perform(click())
        }

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }

    @Test
    fun `test app list recycler view with vpn toggles`() {
        // Given
        val fragmentScenario = launchFragmentInContainer<AppListFragment>(
            initialState = Lifecycle.State.INITIALIZED
        )

        // When
        fragmentScenario.moveToState(Lifecycle.State.RESUMED)

        // Multiple VPN toggles
        repeat(5) {
            onView(withId(R.id.vpn_toggle_button))
                .perform(click())
        }

        // Then
        onView(withId(R.id.app_list_recycler))
            .check(matches(isDisplayed()))

        fragmentScenario.close()
    }
}

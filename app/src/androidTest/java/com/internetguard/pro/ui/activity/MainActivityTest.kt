package com.internetguard.pro.ui.activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.internetguard.pro.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for MainActivity.
 * 
 * Tests UI flows and navigation between different fragments.
 * Uses Espresso for UI interactions and assertions.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Test that the main activity launches successfully.
     */
    @Test
    fun mainActivity_shouldLaunchSuccessfully() {
        // Verify that the main activity is displayed
        onView(withId(R.id.nav_host_fragment))
            .check(matches(isDisplayed()))
        
        // Verify that the bottom navigation is displayed
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))
    }

    /**
     * Test navigation to dashboard fragment.
     */
    @Test
    fun bottomNavigation_clickDashboard_shouldNavigateToDashboard() {
        // Click on dashboard tab
        onView(withId(R.id.nav_dashboard))
            .perform(click())
        
        // Verify dashboard fragment is displayed
        onView(withId(R.id.dashboard_fragment))
            .check(matches(isDisplayed()))
    }

    /**
     * Test navigation to app list fragment.
     */
    @Test
    fun bottomNavigation_clickApps_shouldNavigateToAppList() {
        // Click on apps tab
        onView(withId(R.id.nav_apps))
            .perform(click())
        
        // Verify app list fragment is displayed
        onView(withId(R.id.app_list_fragment))
            .check(matches(isDisplayed()))
    }

    /**
     * Test navigation to keyword list fragment.
     */
    @Test
    fun bottomNavigation_clickKeywords_shouldNavigateToKeywordList() {
        // Click on keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        // Verify keyword list fragment is displayed
        onView(withId(R.id.keyword_list_fragment))
            .check(matches(isDisplayed()))
    }

    /**
     * Test navigation to rules fragment.
     */
    @Test
    fun bottomNavigation_clickRules_shouldNavigateToRules() {
        // Click on rules tab
        onView(withId(R.id.nav_rules))
            .perform(click())
        
        // Verify rules fragment is displayed
        onView(withId(R.id.rules_fragment))
            .check(matches(isDisplayed()))
    }

    /**
     * Test navigation to analytics fragment.
     */
    @Test
    fun bottomNavigation_clickAnalytics_shouldNavigateToAnalytics() {
        // Click on analytics tab
        onView(withId(R.id.nav_analytics))
            .perform(click())
        
        // Verify analytics fragment is displayed
        onView(withId(R.id.analytics_fragment))
            .check(matches(isDisplayed()))
    }

    /**
     * Test that bottom navigation items are visible and clickable.
     */
    @Test
    fun bottomNavigation_shouldHaveAllItemsVisible() {
        // Verify all navigation items are displayed
        onView(withId(R.id.nav_dashboard))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.nav_apps))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.nav_keywords))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.nav_rules))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.nav_analytics))
            .check(matches(isDisplayed()))
    }

    /**
     * Test that the toolbar is displayed.
     */
    @Test
    fun mainActivity_shouldDisplayToolbar() {
        // Verify toolbar is displayed
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    /**
     * Test navigation between different tabs.
     */
    @Test
    fun bottomNavigation_shouldNavigateBetweenTabs() {
        // Navigate to apps tab
        onView(withId(R.id.nav_apps))
            .perform(click())
        
        onView(withId(R.id.app_list_fragment))
            .check(matches(isDisplayed()))
        
        // Navigate to keywords tab
        onView(withId(R.id.nav_keywords))
            .perform(click())
        
        onView(withId(R.id.keyword_list_fragment))
            .check(matches(isDisplayed()))
        
        // Navigate back to dashboard
        onView(withId(R.id.nav_dashboard))
            .perform(click())
        
        onView(withId(R.id.dashboard_fragment))
            .check(matches(isDisplayed()))
    }
}

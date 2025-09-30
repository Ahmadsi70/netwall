package com.internetguard.pro.ui.activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.internetguard.pro.MainActivity
import com.internetguard.pro.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testMainActivityLaunches() {
        // Verify that the main activity launches successfully
        onView(withId(R.id.main_activity))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationDrawerToggle() {
        // Click on the navigation drawer toggle
        onView(withId(R.id.nav_drawer_toggle))
            .perform(click())

        // Verify that the navigation drawer is displayed
        onView(withId(R.id.nav_drawer))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testBottomNavigation() {
        // Test bottom navigation items
        onView(withId(R.id.bottom_navigation))
            .check(matches(isDisplayed()))

        // Click on dashboard tab
        onView(withId(R.id.nav_dashboard))
            .perform(click())

        // Verify dashboard content is displayed
        onView(withId(R.id.dashboard_content))
            .check(matches(isDisplayed()))

        // Click on rules tab
        onView(withId(R.id.nav_rules))
            .perform(click())

        // Verify rules content is displayed
        onView(withId(R.id.rules_content))
            .check(matches(isDisplayed()))

        // Click on settings tab
        onView(withId(R.id.nav_settings))
            .perform(click())

        // Verify settings content is displayed
        onView(withId(R.id.settings_content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testFloatingActionButton() {
        // Verify FAB is displayed
        onView(withId(R.id.fab_add_rule))
            .check(matches(isDisplayed()))

        // Click on FAB
        onView(withId(R.id.fab_add_rule))
            .perform(click())

        // Verify add rule dialog or activity is displayed
        onView(withId(R.id.add_rule_dialog))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testToolbarTitle() {
        // Verify toolbar title is displayed
        onView(withId(R.id.toolbar_title))
            .check(matches(isDisplayed()))
            .check(matches(withText("InternetGuard Pro")))
    }
}

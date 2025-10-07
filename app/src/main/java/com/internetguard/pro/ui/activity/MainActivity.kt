package com.internetguard.pro.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.internetguard.pro.R
import com.internetguard.pro.databinding.ActivityMainBinding
import com.internetguard.pro.utils.LanguageManager

/**
 * Main activity hosting the navigation component and bottom navigation.
 * 
 * Provides the main navigation structure for the app with Material Design 3
 * bottom navigation and dynamic theming support.
 */
class MainActivity : AppCompatActivity() {
	
	private lateinit var binding: ActivityMainBinding
    private var vpnPermissionReceiver: BroadcastReceiver? = null
    private var vpnStatusReceiver: BroadcastReceiver? = null
	
	override fun attachBaseContext(newBase: Context) {
		// 🌍 LANGUAGE: Apply system language before creating the activity
		val systemLanguage = LanguageManager.getBestMatchingLanguage(newBase)
		val context = LanguageManager.createContextWithLanguage(newBase, systemLanguage)
		super.attachBaseContext(context)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// 🌍 LANGUAGE: Ensure language is applied after context attachment
		val systemLanguage = LanguageManager.getBestMatchingLanguage(this)
		LanguageManager.applyLanguage(this, systemLanguage)
		
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		setupNavigation()
		setupTheme()
		setupTestButton()

        registerVpnPermissionReceiver()
        registerVpnStatusReceiver()
	}
	
	/**
	 * Sets up navigation component with bottom navigation
	 */
	private fun setupNavigation() {
		val navHostFragment = supportFragmentManager
			.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
		val navController = navHostFragment.navController
		
		binding.bottomNavigation.setupWithNavController(navController)
	}
	
	/**
	 * Sets up test button for development/testing
	 */
	private fun setupTestButton() {
		// Add test button to toolbar if it exists
		val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
		toolbar?.let {
			val testButton = android.widget.Button(this).apply {
				text = "🧪 Test"
				textSize = 12f
				setOnClickListener {
					val intent = Intent(this@MainActivity, TestActivity::class.java)
					startActivity(intent)
				}
			}
			it.addView(testButton)
		}
	}
	
	/**
	 * Sets up dynamic theming
	 */
	private fun setupTheme() {
		// Apply dynamic theming based on user preferences
		// This would be implemented based on user settings
	}

    private fun registerVpnPermissionReceiver() {
        val filter = IntentFilter("com.internetguard.pro.REQUEST_VPN_PERMISSION")
        vpnPermissionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    val prepareIntent = VpnService.prepare(this@MainActivity)
                    if (prepareIntent != null) {
                        startActivity(prepareIntent)
                    }
                } catch (_: Exception) {}
            }
        }
        registerReceiver(vpnPermissionReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            vpnPermissionReceiver?.let { unregisterReceiver(it) }
            vpnStatusReceiver?.let { unregisterReceiver(it) }
        } catch (_: Exception) {}
    }

    private fun registerVpnStatusReceiver() {
        val filter = IntentFilter("com.internetguard.pro.VPN_STATUS_UPDATE")
        vpnStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val isConnected = intent.getBooleanExtra("is_connected", false)
                val blockedCount = intent.getIntExtra("blocked_apps_count", 0)
                // اینجا می‌توان UI را به‌روزرسانی کرد
                // Log حذف شده برای بهینه‌سازی عملکرد
            }
        }
        registerReceiver(vpnStatusReceiver, filter)
    }
}

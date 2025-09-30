package com.internetguard.pro.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver to re-initialize services after device boot.
 */
class BootReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		// TODO: Start/restore VPN if needed
	}
}

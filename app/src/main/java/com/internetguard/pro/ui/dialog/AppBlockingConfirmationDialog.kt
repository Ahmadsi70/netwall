package com.internetguard.pro.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.internetguard.pro.R
import com.internetguard.pro.data.model.AppInfo

/**
 * Confirmation dialog for app blocking actions
 * Shows app name and blocking type before applying changes
 */
class AppBlockingConfirmationDialog : DialogFragment() {
    
    private var appInfo: AppInfo? = null
    private var isWifiBlocking: Boolean = false
    private var isCellularBlocking: Boolean = false
    private var onConfirmListener: ((AppInfo, Boolean, Boolean) -> Unit)? = null
    
    companion object {
        private const val ARG_APP_INFO = "app_info"
        private const val ARG_WIFI_BLOCKING = "wifi_blocking"
        private const val ARG_CELLULAR_BLOCKING = "cellular_blocking"
        
        fun newInstance(
            appInfo: AppInfo,
            wifiBlocking: Boolean,
            cellularBlocking: Boolean
        ): AppBlockingConfirmationDialog {
            val dialog = AppBlockingConfirmationDialog()
            val args = Bundle()
            args.putParcelable(ARG_APP_INFO, appInfo)
            args.putBoolean(ARG_WIFI_BLOCKING, wifiBlocking)
            args.putBoolean(ARG_CELLULAR_BLOCKING, cellularBlocking)
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appInfo = it.getParcelable(ARG_APP_INFO)
            isWifiBlocking = it.getBoolean(ARG_WIFI_BLOCKING)
            isCellularBlocking = it.getBoolean(ARG_CELLULAR_BLOCKING)
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val app = appInfo ?: return super.onCreateDialog(savedInstanceState)
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_blocking_confirmation, null)
        val appNameText = dialogView.findViewById<TextView>(R.id.app_name_text)
        val blockingTypeText = dialogView.findViewById<TextView>(R.id.blocking_type_text)
        val warningText = dialogView.findViewById<TextView>(R.id.warning_text)
        
        // Set app name
        appNameText.text = app.appName
        
        // Set blocking type description
        val blockingTypes = mutableListOf<String>()
        if (isWifiBlocking) blockingTypes.add("WiFi")
        if (isCellularBlocking) blockingTypes.add("Cellular")
        
        val blockingTypeDescription = when {
            blockingTypes.size == 2 -> "WiFi and Cellular"
            isWifiBlocking -> "WiFi only"
            isCellularBlocking -> "Cellular only"
            else -> "None"
        }
        
        blockingTypeText.text = "Blocking: $blockingTypeDescription"
        
        // Set warning message
        val warningMessage = if (isWifiBlocking || isCellularBlocking) {
            "This will block internet access for ${app.appName}. The app will not be able to connect to the internet until you unblock it."
        } else {
            "This will restore internet access for ${app.appName}. The app will be able to connect to the internet normally."
        }
        warningText.text = warningMessage
        
        return AlertDialog.Builder(requireContext())
            .setTitle("Confirm App Blocking")
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                onConfirmListener?.invoke(app, isWifiBlocking, isCellularBlocking)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
    }
    
    fun setOnConfirmListener(listener: (AppInfo, Boolean, Boolean) -> Unit) {
        onConfirmListener = listener
    }
}

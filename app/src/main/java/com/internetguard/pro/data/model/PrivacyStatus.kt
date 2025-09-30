package com.internetguard.pro.data.model

/**
 * Data class representing privacy status
 */
data class PrivacyStatus(
    val privateDNSEnabled: Boolean,
    val vpnPassthroughEnabled: Boolean,
    val networkMonitoringEnabled: Boolean,
    val dnsOverHTTPSEnabled: Boolean,
    val dnsOverTLSEnabled: Boolean
)
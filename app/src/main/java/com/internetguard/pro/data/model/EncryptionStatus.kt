package com.internetguard.pro.data.model

/**
 * Data class representing encryption status
 */
data class EncryptionStatus(
    val isEncrypted: Boolean,
    val passwordStrength: Int
)
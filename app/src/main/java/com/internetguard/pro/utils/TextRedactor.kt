package com.internetguard.pro.utils

object TextRedactor {
    private val emailRegex = "[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+".toRegex()
    private val phoneRegex = "(?:\\+?[0-9]{1,3}[\\s-]?)?(?:[0-9]{3}[\\s-]?[0-9]{3}[\\s-]?[0-9]{4})".toRegex()
    private val urlRegex = "https?://[A-Za-z0-9./?=&_%:-]+".toRegex()

    fun redact(input: String, maxLen: Int = 256): String {
        val masked = input
            .replace(emailRegex, "[EMAIL]")
            .replace(phoneRegex, "[PHONE]")
            .replace(urlRegex, "[URL]")
        return if (masked.length <= maxLen) masked else masked.substring(0, maxLen)
    }
}



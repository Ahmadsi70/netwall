package com.internetguard.pro.data.model

/**
 * App categories for quick blocking actions
 */
enum class AppCategory(
    val displayName: String,
    val emoji: String,
    val keywords: List<String>
) {
    SOCIAL_MEDIA(
        displayName = "شبکه‌های اجتماعی",
        emoji = "📱",
        keywords = listOf(
            // Popular social media
            "instagram", "facebook", "snapchat", "tiktok", "twitter", "x.com",
            "whatsapp", "telegram", "viber", "imo", "wechat", "line",
            "messenger", "meta", "threads",
            // Persian variations
            "اینستاگرام", "فیسبوک", "تلگرام", "واتساپ"
        )
    ),
    
    GAMES(
        displayName = "بازی‌ها",
        emoji = "🎮",
        keywords = listOf(
            // Popular games
            "game", "play", "pubg", "free.fire", "clash", "candy.crush",
            "subway", "temple.run", "angry.birds", "minecraft", "roblox",
            "fortnite", "among.us", "genshin", "call.of.duty", "fifa",
            // Game stores
            "game.store", "epic.games",
            // Persian
            "بازی", "گیم"
        )
    ),
    
    BROWSERS(
        displayName = "مرورگرها",
        emoji = "🌐",
        keywords = listOf(
            "chrome", "firefox", "opera", "brave", "edge", "safari",
            "browser", "duckduckgo", "vivaldi", "tor", "uc.browser",
            "samsung.internet", "mi.browser"
        )
    ),
    
    MESSAGING(
        displayName = "پیام‌رسان‌ها",
        emoji = "💬",
        keywords = listOf(
            "whatsapp", "telegram", "messenger", "viber", "signal",
            "imo", "wechat", "line", "skype", "discord", "slack",
            "teams", "zoom", "google.meet", "hangouts"
        )
    ),
    
    ENTERTAINMENT(
        displayName = "سرگرمی",
        emoji = "🎬",
        keywords = listOf(
            "youtube", "netflix", "spotify", "soundcloud", "tidal",
            "twitch", "vimeo", "dailymotion", "hulu", "disney",
            "amazon.prime", "hbo", "aparat", "filimo", "namava"
        )
    ),
    
    SHOPPING(
        displayName = "خرید",
        emoji = "🛒",
        keywords = listOf(
            "amazon", "ebay", "aliexpress", "alibaba", "digikala",
            "bamilo", "snapp.shop", "torob", "emalls", "shop",
            "store", "market", "mall", "bazaar"
        )
    ),
    
    DATING(
        displayName = "آشنایی و دوستیابی",
        emoji = "💝",
        keywords = listOf(
            "tinder", "bumble", "hinge", "okcupid", "match",
            "badoo", "happn", "grindr", "meetme", "skout",
            "dating", "meet", "friends"
        )
    ),
    
    NEWS(
        displayName = "اخبار",
        emoji = "📰",
        keywords = listOf(
            "news", "bbc", "cnn", "reddit", "twitter", "feedly",
            "flipboard", "google.news", "isna", "irna", "tasnim",
            "mehrnews", "farsnews", "khabaronline"
        )
    );
    
    /**
     * Check if package name matches this category
     */
    fun matches(packageName: String, appName: String): Boolean {
        val searchText = "$packageName $appName".lowercase()
        return keywords.any { keyword ->
            searchText.contains(keyword.lowercase())
        }
    }
    
    companion object {
        /**
         * Get categories for a specific app
         */
        fun getCategoriesForApp(packageName: String, appName: String): List<AppCategory> {
            return values().filter { it.matches(packageName, appName) }
        }
        
        /**
         * Get all apps in a category
         */
        fun getAppsInCategory(category: AppCategory, allApps: List<AppInfo>): List<AppInfo> {
            return allApps.filter { app ->
                category.matches(app.packageName, app.appName)
            }
        }
    }
} 
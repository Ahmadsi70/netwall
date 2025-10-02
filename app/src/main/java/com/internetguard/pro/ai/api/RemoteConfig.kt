package com.internetguard.pro.ai.api

object RemoteConfig {
    // TODO: پس از دیپلوی روی Railway این آدرس را با URL نهایی جایگزین کنید
    const val BASE_URL: String = "https://netwall-proxy-6u4ijr384-ahmad-salarizadehs-projects.vercel.app"

    val MODERATE_URL: String
        get() = "$BASE_URL/api/moderate"

    val SUGGEST_URL: String
        get() = "$BASE_URL/api/suggest"
} 
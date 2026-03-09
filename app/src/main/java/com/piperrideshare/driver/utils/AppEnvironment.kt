package com.piperrideshare.driver.utils

import com.piperrideshare.driver.BuildConfig

/**
 * AppEnvironment - Centralized environment configuration.
 *
 * Development (DEBUG builds): routes to the staging server.
 * Production (release builds): routes to the live API.
 */
enum class AppEnvironment {
    DEVELOPMENT,
    PRODUCTION;

    companion object {
        val current: AppEnvironment = if (BuildConfig.DEBUG) DEVELOPMENT else PRODUCTION
    }

    val apiBaseURL: String
        get() = when (this) {
            DEVELOPMENT -> "https://piper-main-app-staging.fly.dev"
            PRODUCTION -> "https://api.thepiper.co"
        }

    val wsBaseURL: String
        get() = when (this) {
            DEVELOPMENT -> "wss://piper-main-app-staging.fly.dev"
            PRODUCTION -> "wss://api.thepiper.co"
        }

    val adminServiceURL: String
        get() = when (this) {
            DEVELOPMENT -> "https://piper-admin-staging.fly.dev"
            PRODUCTION -> "https://admin.api.thepiper.co"
        }

    val enableDebugLogs: Boolean
        get() = when (this) {
            DEVELOPMENT -> true
            PRODUCTION -> false
        }

    val displayName: String
        get() = when (this) {
            DEVELOPMENT -> "🛠 Development"
            PRODUCTION -> "🚀 Production"
        }
}

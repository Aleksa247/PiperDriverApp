package com.piperrideshare.driver.utils

import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val PREFS_NAME = "driver_prefs_encrypted"
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_EMAIL = "email"
    const val KEY_PASSWORD = "password"

    const val NOTIFICATION_ID = 12345
    const val CHANNEL_ID = "location_service_channel"

    val TOKEN_KEY = stringPreferencesKey("token")
    val USER_ID_KEY = stringPreferencesKey("user_id")
    val NAME_KEY = stringPreferencesKey("name")
}

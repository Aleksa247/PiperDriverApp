@file:Suppress("DEPRECATION")

package com.piperrideshare.driver.data

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.piperrideshare.driver.utils.Constants

class UserPreferences(
    context: Context,
) {
    private val prefs =
        run {
            // Create or retrieve the MasterKey for encryption
            val masterKey =
                MasterKey
                    .Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

            EncryptedSharedPreferences.create(
                context,
                Constants.PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

    var rememberMe: Boolean
        get() = prefs.getBoolean(Constants.KEY_REMEMBER_ME, false)
        set(value) = prefs.edit { putBoolean(Constants.KEY_REMEMBER_ME, value) }

    var email: String?
        get() = prefs.getString(Constants.KEY_EMAIL, "")
        set(value) = prefs.edit { putString(Constants.KEY_EMAIL, value) }

    var password: String?
        get() = prefs.getString(Constants.KEY_PASSWORD, "")
        set(value) = prefs.edit { putString(Constants.KEY_PASSWORD, value) }

    fun clear() {
        prefs.edit { clear() }
    }
}

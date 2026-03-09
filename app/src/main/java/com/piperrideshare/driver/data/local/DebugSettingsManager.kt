package com.piperrideshare.driver.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.piperrideshare.driver.BuildConfig
import com.piperrideshare.driver.utils.AppEnvironment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.debugDataStore: DataStore<Preferences> by preferencesDataStore(name = "debug_settings")

/**
 * DebugSettingsManager - Manages debug-only settings like custom API URLs.
 *
 * Allows developers to override the BASE_URL at runtime for testing
 * against local/ngrok backends without rebuilding.
 */
@Singleton
class DebugSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val CUSTOM_BASE_URL_KEY = stringPreferencesKey("custom_base_url")
    }

    /**
     * Gets the effective base URL.
     * Returns custom URL if set, otherwise falls back to BuildConfig.BASE_URL.
     * This is a blocking call intended for use during DI initialization.
     */
    fun getEffectiveBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            runBlocking {
                context.debugDataStore.data.first()[CUSTOM_BASE_URL_KEY]
            }?.takeIf { it.isNotBlank() } ?: AppEnvironment.current.apiBaseURL
        } else {
            AppEnvironment.current.apiBaseURL
        }
    }

    /**
     * Flow of the current custom base URL (for UI observation).
     */
    val customBaseUrlFlow: Flow<String?> = context.debugDataStore.data.map { prefs ->
        prefs[CUSTOM_BASE_URL_KEY]
    }

    /**
     * Sets a custom base URL for debugging.
     * Pass null or empty string to clear and revert to default.
     */
    suspend fun setCustomBaseUrl(url: String?) {
        context.debugDataStore.edit { prefs ->
            if (url.isNullOrBlank()) {
                prefs.remove(CUSTOM_BASE_URL_KEY)
            } else {
                prefs[CUSTOM_BASE_URL_KEY] = url.trimEnd('/')
            }
        }
    }

    /**
     * Clears the custom base URL.
     */
    suspend fun clearCustomBaseUrl() {
        setCustomBaseUrl(null)
    }
}

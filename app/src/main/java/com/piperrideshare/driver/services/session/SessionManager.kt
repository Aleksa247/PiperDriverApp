package com.piperrideshare.driver.services.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.piperrideshare.driver.data.UserPreferences
import com.piperrideshare.driver.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class SessionManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ISessionManager {
        override suspend fun saveAuthInfo(
            token: String,
            userId: String?,
            name: String?,
        ) {
            context.dataStore.edit { prefs ->
                prefs[Constants.TOKEN_KEY] = token
                userId?.let { prefs[Constants.USER_ID_KEY] = it }
                name?.let { prefs[Constants.NAME_KEY] = it }
            }
        }

        override suspend fun saveFcmToken(token: String) {
            context.dataStore.edit { prefs ->
                prefs[Constants.FCM_TOKEN_KEY] = token
            }
        }

        override val userToken: Flow<String?> = context.dataStore.data.map { it[Constants.TOKEN_KEY] }
        override val userId: Flow<String?> = context.dataStore.data.map { it[Constants.USER_ID_KEY] }
        override val name: Flow<String?> = context.dataStore.data.map { it[Constants.NAME_KEY] }
        override val fcmToken: Flow<String?> =
            context.dataStore.data.map { it[Constants.FCM_TOKEN_KEY] }

        override suspend fun clearSession() {
            context.dataStore.edit { it.clear() }
            UserPreferences(context).clear()
        }
    }

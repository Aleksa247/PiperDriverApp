package com.piperrideshare.driver.services.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.piperrideshare.driver.data.UserPreferences
import com.piperrideshare.driver.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
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
            accessToken: String,
            refreshToken: String?,
            expiresIn: Long?,
            userId: String?,
            name: String?,
        ) {
            context.dataStore.edit { prefs ->
                prefs[Constants.TOKEN_KEY] = accessToken
                refreshToken?.let { prefs[Constants.REFRESH_TOKEN_KEY] = it }
                expiresIn?.let {
                    // Calculate expiry timestamp from now + expiresIn seconds
                    val expiresAt = Instant.now().plus(it, ChronoUnit.SECONDS).toString()
                    prefs[Constants.EXPIRES_AT_KEY] = expiresAt
                }
                userId?.let { prefs[Constants.USER_ID_KEY] = it }
                name?.let { prefs[Constants.NAME_KEY] = it }
            }
        }

        override suspend fun saveFcmToken(token: String) {
            context.dataStore.edit { prefs ->
                prefs[Constants.FCM_TOKEN_KEY] = token
            }
        }

        override suspend fun isTokenExpired(bufferSeconds: Long): Boolean {
            val expiresAtString = tokenExpiresAt.first() ?: return true // No expiry means expired
            return try {
                val expiresAt = Instant.parse(expiresAtString)
                val bufferTime = Instant.now().plus(bufferSeconds, ChronoUnit.SECONDS)
                expiresAt.isBefore(bufferTime)
            } catch (e: Exception) {
                true // If parsing fails, consider expired
            }
        }

        override val userToken: Flow<String?> = context.dataStore.data.map { it[Constants.TOKEN_KEY] }
        override val refreshToken: Flow<String?> = context.dataStore.data.map { it[Constants.REFRESH_TOKEN_KEY] }
        override val tokenExpiresAt: Flow<String?> = context.dataStore.data.map { it[Constants.EXPIRES_AT_KEY] }
        override val userId: Flow<String?> = context.dataStore.data.map { it[Constants.USER_ID_KEY] }
        override val name: Flow<String?> = context.dataStore.data.map { it[Constants.NAME_KEY] }
        override val fcmToken: Flow<String?> = context.dataStore.data.map { it[Constants.FCM_TOKEN_KEY] }

        override suspend fun clearSession() {
            context.dataStore.edit { it.clear() }
            UserPreferences(context).clear()
        }
    }


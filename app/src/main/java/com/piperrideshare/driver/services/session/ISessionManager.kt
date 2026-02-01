package com.piperrideshare.driver.services.session

import kotlinx.coroutines.flow.Flow

interface ISessionManager {
    /**
     * Save full authentication info including tokens and user details.
     * @param accessToken JWT access token
     * @param refreshToken Token used to obtain new access tokens
     * @param expiresIn Token validity duration in seconds
     * @param userId User's unique identifier
     * @param name User's display name
     */
    suspend fun saveAuthInfo(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long?,
        userId: String?,
        name: String?,
    )

    suspend fun saveFcmToken(token: String)

    suspend fun clearSession()

    /**
     * Check if the current access token is expired or about to expire.
     * @param bufferSeconds Consider expired if within this many seconds of expiry (default 5 min)
     */
    suspend fun isTokenExpired(bufferSeconds: Long = 300): Boolean

    val userToken: Flow<String?>
    val refreshToken: Flow<String?>
    val tokenExpiresAt: Flow<String?>
    val userId: Flow<String?>
    val name: Flow<String?>
    val fcmToken: Flow<String?>
}

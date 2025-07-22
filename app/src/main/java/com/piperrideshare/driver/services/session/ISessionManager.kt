package com.piperrideshare.driver.services.session

import kotlinx.coroutines.flow.Flow

interface ISessionManager {
    suspend fun saveAuthInfo(
        token: String,
        userId: String?,
        name: String?,
    )

    suspend fun saveFcmToken(token: String)

    suspend fun clearSession()

    val userToken: Flow<String?>
    val userId: Flow<String?>
    val name: Flow<String?>
    val fcmToken: Flow<String?>
}

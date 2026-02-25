package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.models.request.RegisterRequest
import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.api.models.response.RefreshTokenResponse
import com.piperrideshare.driver.api.models.response.ZonesResponse
import com.piperrideshare.driver.data.network.ApiResult

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthResponse>

    suspend fun register(
        request: RegisterRequest
    ): ApiResult<AuthResponse>

    suspend fun getZones(): ApiResult<ZonesResponse>

    suspend fun refreshToken(
        refreshToken: String,
    ): ApiResult<RefreshTokenResponse>
}

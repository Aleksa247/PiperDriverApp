package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.data.network.ApiResult

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthResponse>
}

package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.models.response.AuthResponse

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        deviceId: String,
    ): Result<AuthResponse>
}

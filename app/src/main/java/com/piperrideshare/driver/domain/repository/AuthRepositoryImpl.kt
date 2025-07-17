package com.piperrideshare.driver.repository

import com.piperrideshare.driver.api.ApiService
import com.piperrideshare.driver.api.models.request.LoginRequest
import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val api: ApiService,
    ) : AuthRepository {
        override suspend fun login(
            email: String,
            password: String,
            deviceId: String,
        ): Result<AuthResponse> =
            try {
                val response = api.login(LoginRequest(email, password, deviceId))
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }

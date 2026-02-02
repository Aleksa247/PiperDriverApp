package com.piperrideshare.driver.repository

import com.piperrideshare.driver.api.ApiService
import com.piperrideshare.driver.api.models.request.LoginRequest
import com.piperrideshare.driver.api.models.request.RegisterRequest
import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.api.models.response.ZonesResponse
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.data.network.safeApiCall
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
        ): ApiResult<AuthResponse> =
            safeApiCall {
                api.login(LoginRequest(email, password, deviceId))
            }

        override suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> =
            safeApiCall {
                api.register(request)
            }

        override suspend fun getZones(): ApiResult<ZonesResponse> =
            safeApiCall {
                api.getZones()
            }
    }

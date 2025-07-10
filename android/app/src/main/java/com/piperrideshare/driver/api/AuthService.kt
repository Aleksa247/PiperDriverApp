package com.piperrideshare.driver.api

import com.piperrideshare.driver.api.models.request.LoginRequest
import com.piperrideshare.driver.api.models.response.AuthResponse

object AuthService {

    lateinit var api: ApiService

    fun init(apiService: ApiService) {
        api = apiService
    }

    suspend fun login(
        email: String,
        password: String,
        deviceId: String,
    ): Result<AuthResponse> = try {
        val response = api.login(LoginRequest(email, password, deviceId))
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

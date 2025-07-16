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
                // @Thomas - BREAKPOINT HERE: About to make HTTP request to login endpoint
                // This is where the actual API call happens
                println("🚀 HTTP REQUEST: Calling api.login() with LoginRequest")
                println("📤 Request payload: email=$email, deviceId=$deviceId")

                val response = api.login(LoginRequest(email, password, deviceId))

                // @Thomas - BREAKPOINT HERE: HTTP response received successfully
                println("📥 HTTP RESPONSE: Login successful")
                println("🔑 Token: ${response.token}")
                println("👤 User ID: ${response.userId}")
                println("📝 Name: ${response.name}")

                Result.success(response)
            } catch (e: Exception) {
                // @Thomas - BREAKPOINT HERE: HTTP request failed
                println("💥 HTTP ERROR: Login request failed")
                println("❌ Error: ${e.message}")
                println("📋 Exception type: ${e.javaClass.simpleName}")
                Result.failure(e)
            }
    }

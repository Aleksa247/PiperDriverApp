package com.piperrideshare.driver.api

import com.piperrideshare.driver.api.models.request.LoginRequest
import com.piperrideshare.driver.api.models.response.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/drivers/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): AuthResponse
}

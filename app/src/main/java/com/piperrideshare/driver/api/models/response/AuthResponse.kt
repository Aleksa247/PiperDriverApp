package com.piperrideshare.driver.api.models.response

data class AuthResponse(
    val token: String,
    val userId: String,
    val name: String,
)

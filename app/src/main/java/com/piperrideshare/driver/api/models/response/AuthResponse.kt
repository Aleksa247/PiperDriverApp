package com.piperrideshare.driver.api.models.response

import com.google.gson.annotations.SerializedName

/**
 * Response from login and register endpoints.
 * Matches backend response from driver_handler.go.
 */
data class AuthResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("refresh_token")
    val refreshToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Long, // seconds until access token expires
    
    // Legacy field aliases for backward compatibility
    val token: String? = null, // Will be null with new backend
    val userId: String? = null,
    val name: String? = null,
)


package com.piperrideshare.driver.api.models.response

import com.google.gson.annotations.SerializedName

/**
 * Response from /api/auth/refresh endpoint.
 */
data class RefreshTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("refresh_token")
    val refreshToken: String,
    
    @SerializedName("expires_in")
    val expiresIn: Long, // seconds until access token expires
)

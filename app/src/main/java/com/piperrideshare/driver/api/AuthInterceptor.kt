package com.piperrideshare.driver.api

import com.piperrideshare.driver.services.session.ISessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that attaches the Bearer token to all authenticated API requests.
 * Similar to how WebSocketHandler manually adds the Authorization header.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: ISessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth header for login/register endpoints (they don't need auth)
        val path = originalRequest.url.encodedPath
        if (path.endsWith("/login") || path.endsWith("/register")) {
            return chain.proceed(originalRequest)
        }

        // Get the token synchronously (required for OkHttp interceptor)
        val token = runBlocking { sessionManager.userToken.first() }

        return if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

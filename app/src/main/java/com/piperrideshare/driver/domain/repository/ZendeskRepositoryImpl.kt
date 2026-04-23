package com.piperrideshare.driver.domain.repository

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.piperrideshare.driver.api.ZendeskApiService
import com.piperrideshare.driver.api.models.request.ZendeskTicketRequest
import timber.log.Timber
import javax.inject.Inject

class ZendeskRepositoryImpl @Inject constructor(
    private val apiService: ZendeskApiService
) : ZendeskRepository {

    override suspend fun createTicket(request: ZendeskTicketRequest): Result<Unit> {
        val url = "https://pipeyllc.zendesk.com/api/v2/requests.json"
        val email = "" //admin email here <--
        val apiToken = "" // API Token here <--
        val authString = "$email/token:$apiToken"
        val base64Auth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
        val authHeader = "Basic $base64Auth"

        // LOGGING REQUEST
        Log.e("ZendeskDebug", "--- START ZENDESK REQUEST ---")
        Log.e("ZendeskDebug", "URL: $url")
        Log.e("ZendeskDebug", "Auth Header: Basic ${base64Auth.take(10)}...")
        Log.e("ZendeskDebug", "Request Body: ${Gson().toJson(request)}")

        return try {
            val response = apiService.createTicket(authHeader, request)
            
            // LOGGING RESPONSE
            Log.e("ZendeskDebug", "Response Code: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.e("ZendeskDebug", "Ticket created successfully!")
                Log.e("ZendeskDebug", "--- END ZENDESK REQUEST ---")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ZendeskDebug", "Error Body: $errorBody")
                Log.e("ZendeskDebug", "--- END ZENDESK REQUEST ---")
                Result.failure(Exception("Zendesk Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ZendeskDebug", "Exception: ${e.message}")
            Log.e("ZendeskDebug", "--- END ZENDESK REQUEST ---")
            Timber.e(e, "Error creating Zendesk ticket")
            Result.failure(e)
        }
    }
}

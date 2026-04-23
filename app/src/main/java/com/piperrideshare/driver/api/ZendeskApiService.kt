package com.piperrideshare.driver.api

import com.piperrideshare.driver.api.models.request.ZendeskTicketRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ZendeskApiService {
    @POST("/api/v2/requests.json")
    suspend fun createTicket(
        @Header("Authorization") authHeader: String,
        @Body request: ZendeskTicketRequest
    ): Response<ResponseBody>
}

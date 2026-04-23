package com.piperrideshare.driver.domain.repository

import com.piperrideshare.driver.api.models.request.ZendeskTicketRequest

interface ZendeskRepository {
    suspend fun createTicket(request: ZendeskTicketRequest): Result<Unit>
}

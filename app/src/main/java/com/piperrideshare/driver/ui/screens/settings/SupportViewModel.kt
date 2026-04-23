package com.piperrideshare.driver.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piperrideshare.driver.api.models.request.RequesterInfo
import com.piperrideshare.driver.api.models.request.TicketComment
import com.piperrideshare.driver.api.models.request.TicketData
import com.piperrideshare.driver.api.models.request.ZendeskTicketRequest
import com.piperrideshare.driver.domain.repository.ZendeskRepository
import com.piperrideshare.driver.services.IDriverStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val zendeskRepository: ZendeskRepository,
    private val driverStateManager: IDriverStateManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun submitTicket(subject: String, description: String, onResult: (Boolean) -> Unit) {
        Log.e("ZendeskDebug", "SupportViewModel: submitTicket called with Subject: $subject")
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                Log.e("ZendeskDebug", "SupportViewModel: Fetching driver state from DriverStateManager...")
                val driver = driverStateManager.getCurrentState()
                Log.e("ZendeskDebug", "SupportViewModel: Driver state result: ${if (driver == null) "NULL" else "Found (${driver.email})"}")
                
                // Fallback requester info if profile is missing
                val requesterName = if (driver != null) "${driver.firstName} ${driver.lastName}" else "Android Driver"
                val requesterEmail = if (driver != null && driver.email.isNotBlank()) driver.email else "" //admin email here <--

                val request = ZendeskTicketRequest(
                    request = TicketData(
                        subject = subject,
                        requester = RequesterInfo(
                            name = requesterName,
                            email = requesterEmail
                        ),
                        comment = TicketComment(
                            body = description
                        )
                    )
                )


                Log.e("ZendeskDebug", "SupportViewModel: Calling zendeskRepository.createTicket...")
                val result = zendeskRepository.createTicket(request)
                Log.e("ZendeskDebug", "SupportViewModel: Repository returned success: ${result.isSuccess}")
                
                _isLoading.value = false
                onResult(result.isSuccess)
            } catch (e: Exception) {
                Log.e("ZendeskDebug", "SupportViewModel: Error in coroutine: ${e.message}", e)
                _isLoading.value = false
                onResult(false)
            }
        }
    }
}

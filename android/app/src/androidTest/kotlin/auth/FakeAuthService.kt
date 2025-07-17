package auth

import com.piperrideshare.driver.api.ApiService
import com.piperrideshare.driver.api.models.request.LoginRequest
import com.piperrideshare.driver.api.models.response.AuthResponse

class FakeAuthService : ApiService {
    var shouldThrow = false

    override suspend fun login(request: LoginRequest): AuthResponse {
        if (shouldThrow) throw Exception("Login failed")
        return AuthResponse(
            token = "fake-token",
            userId = "user123",
            name = "Thomas",
        )
    }
}

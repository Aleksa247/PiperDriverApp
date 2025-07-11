package test.fake

import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var shouldFail = false

    override suspend fun login(
        email: String,
        password: String,
        deviceId: String,
    ): Result<AuthResponse> =
        if (shouldFail) {
            Result.failure(Exception("Login failed"))
        } else {
            Result.success(
                AuthResponse(
                    token = "fake_token",
                    userId = "user123",
                    name = "Test User",
                ),
            )
        }
}

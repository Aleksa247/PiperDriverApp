package test.fake

import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var shouldFail = false

    override suspend fun login(
        email: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthResponse> =
        if (shouldFail) {
            ApiResult.Failure(message = "Login failed", code = 401)
        } else {
            ApiResult.Success(
                AuthResponse(
                    token = "fake_token",
                    userId = "user123",
                    name = "Test User",
                ),
            )
        }
}

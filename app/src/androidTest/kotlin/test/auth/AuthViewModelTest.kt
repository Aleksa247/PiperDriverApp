package test.auth

import com.piperrideshare.driver.api.models.response.AuthResponse
import com.piperrideshare.driver.data.network.ApiResult
import com.piperrideshare.driver.ui.screens.login.AuthViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import test.fake.FakeAuthRepository
import test.fake.FakeSessionManager

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private lateinit var viewModel: AuthViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSessionManager: FakeSessionManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() =
        runTest {
            Dispatchers.setMain(testDispatcher)

            fakeAuthRepository = FakeAuthRepository()
            fakeSessionManager = FakeSessionManager()

            // Set the FCM token in the fake session
            fakeSessionManager.saveFcmToken("device123")

            viewModel = AuthViewModel(fakeAuthRepository, fakeSessionManager)
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success_updates_loginResult_and_session() =
        runTest {
            val email = "test@example.com"
            val password = "password"

            viewModel.login(email, password)
            advanceUntilIdle()

            val result = viewModel.loginResult.value
            assertTrue(result is ApiResult.Success)
            assertEquals("user123", (result as ApiResult.Success<AuthResponse>).data.userId)
            assertEquals("user123", fakeSessionManager.userId.value)
        }

    @Test
    fun login_failure_updates_loginResult_with_failure() =
        runTest {
            fakeAuthRepository.shouldFail = true

            viewModel.login("fail@example.com", "wrongpass")
            advanceUntilIdle()

            val result = viewModel.loginResult.value
            assertTrue(result is ApiResult.Failure)
        }
}

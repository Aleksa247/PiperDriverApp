package auth

import com.piperrideshare.driver.api.AuthService
import com.piperrideshare.driver.ui.auth.AuthViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private lateinit var viewModel: AuthViewModel
    private lateinit var fakeApiService: FakeAuthService

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeApiService = FakeAuthService()

        // Initialize the singleton
        AuthService.init(fakeApiService)

        // Use the singleton
        viewModel = AuthViewModel(AuthService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success_updates_loginResult() = runTest {
        val email = "test@example.com"
        val password = "password"
        val deviceId = "device123"

        viewModel.login(email, password, deviceId)
        advanceUntilIdle()

        val result = viewModel.loginResult.value
        assertTrue(result!!.isSuccess)
        assertEquals("user123", result.getOrNull()?.userId)
    }

    @Test
    fun login_failure_updates_loginResult_with_failure() = runTest {
        fakeApiService.shouldThrow = true

        viewModel.login("fail@example.com", "badpass", "device123")
        advanceUntilIdle()

        val result = viewModel.loginResult.value
        assertTrue(result!!.isFailure)
    }
}

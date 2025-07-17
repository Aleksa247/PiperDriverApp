package test.auth

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
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeAuthRepository = FakeAuthRepository()
        fakeSessionManager = FakeSessionManager()

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
            val deviceId = "device123"

            viewModel.login(email, password, deviceId)
            advanceUntilIdle()

            val result = viewModel.loginResult.value
            assertTrue(result!!.isSuccess)
            assertEquals("user123", result.getOrNull()?.userId)
            assertEquals("user123", fakeSessionManager.userId.value)
        }

    @Test
    fun login_failure_updates_loginResult_with_failure() =
        runTest {
            fakeAuthRepository.shouldFail = true

            viewModel.login("fail@example.com", "wrongpass", "device123")
            advanceUntilIdle()

            val result = viewModel.loginResult.value
            assertTrue(result!!.isFailure)
        }
}

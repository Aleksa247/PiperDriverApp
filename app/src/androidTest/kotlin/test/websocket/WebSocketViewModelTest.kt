package test.websocket

import com.piperrideshare.driver.api.models.response.websocket.DriverModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import com.piperrideshare.driver.api.models.response.websocket.RideRequestedResponse
import com.piperrideshare.driver.ui.viewModel.WebSocketViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
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
import test.fake.FakeSessionManager
import test.fake.FakeWebSocketRepository

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WebSocketViewModel
    private lateinit var fakeSessionManager: FakeSessionManager
    private lateinit var fakeRepository: FakeWebSocketRepository

    @Before
    fun setup() =
        runTest {
            Dispatchers.setMain(testDispatcher)

            fakeSessionManager =
                FakeSessionManager().apply {
                    saveAuthInfo("fake_token", userId = "user123", name = "Tester")
                }

            fakeRepository = FakeWebSocketRepository()

            viewModel = WebSocketViewModel(fakeRepository, fakeSessionManager)
            advanceUntilIdle()
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun connect_sets_rideRequest_state_when_RideRequestedResponse_received() =
        runTest {
            assertNull(viewModel.rideRequest.value)

            val response = RideRequestedResponse("ride123")
            fakeRepository.triggerFakeResponse(response)

            advanceUntilIdle()

            assertEquals(response, viewModel.rideRequest.value)
        }

    @Test
    fun connect_sets_driverModel_state_when_DriverModelChangedResponse_received() =
        runTest {
            assertNull(viewModel.driverModel.value)

            val response = DriverModelChangedResponse("driver123")
            fakeRepository.triggerFakeResponse(response)

            advanceUntilIdle()

            assertEquals(response, viewModel.driverModel.value)
        }

    @Test
    fun connect_sets_rideModel_state_when_RideModelChangedResponse_received() =
        runTest {
            assertNull(viewModel.rideModel.value)

            val response = RideModelChangedResponse("ride123")
            fakeRepository.triggerFakeResponse(response)

            advanceUntilIdle()

            assertEquals(response, viewModel.rideModel.value)
        }

    @Test
    fun connect_uses_session_token_correctly() =
        runTest {
            assertEquals("fake_token", fakeRepository.connectedToken)
        }
}

package test.models

import com.google.gson.Gson
import com.piperrideshare.driver.api.models.response.websocket.RideModelChangedResponse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class RideModelDeserializationTest {

    private val gson = Gson()

    @Test
    fun deserialize_with_new_commission_fields() {
        val json = """
        {
            "id": "ride-1",
            "rider_id": "r-1",
            "driver_id": "d-1",
            "status": "InProgress",
            "pickup_location": {"latitude": 33.45, "longitude": -112.07},
            "dropoff_location": {"latitude": 33.46, "longitude": -112.08},
            "current_location": {"latitude": 33.455, "longitude": -112.075},
            "rider_current_location": {"latitude": 33.45, "longitude": -112.07},
            "request_time": "2025-06-01T10:00:00Z",
            "estimated_fare": 2500,
            "driver_earning": 1625,
            "actual_fare": 0,
            "subtotal": 2200,
            "driver_cut_percent": 0.65,
            "wait_time_fee": 150,
            "driver_wait_timer_end": "2025-06-01T10:10:00Z",
            "distance": 5.2,
            "duration": 720,
            "payment_method_id": "pm-1",
            "payment_status": "Pending",
            "driver_tip": 0,
            "ride_type_id": "rt-1",
            "cancellation_fee": 0,
            "cancellation_type": "",
            "rating": 0,
            "feedback": "",
            "version": 3
        }
        """.trimIndent()

        val ride = gson.fromJson(json, RideModelChangedResponse::class.java)
        assertEquals("ride-1", ride.rideId)
        assertEquals(2200, ride.subtotal)
        assertEquals(0.65, ride.driverCutPercent)
        assertEquals(1625, ride.driverEarning)
    }

    @Test
    fun deserialize_without_new_fields_defaults_to_null() {
        val json = """
        {
            "id": "ride-2",
            "rider_id": "r-1",
            "driver_id": "d-1",
            "status": "Requested",
            "estimated_fare": 2500,
            "driver_earning": 0,
            "actual_fare": 0,
            "wait_time_fee": 0,
            "distance": 5.2,
            "duration": 720,
            "payment_method_id": "pm-1",
            "payment_status": "Pending",
            "driver_tip": 0,
            "ride_type_id": "rt-1",
            "cancellation_fee": 0,
            "cancellation_type": "",
            "rating": 0,
            "feedback": "",
            "version": 1
        }
        """.trimIndent()

        val ride = gson.fromJson(json, RideModelChangedResponse::class.java)
        assertEquals("ride-2", ride.rideId)
        assertNull(ride.subtotal)
        assertNull(ride.driverCutPercent)
    }
}

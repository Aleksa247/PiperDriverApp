package com.piperrideshare.driver.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * MapView is temporarily disabled due to missing Mapbox Maps SDK dependencies.
 * This composable would display a Mapbox map for location tracking and navigation.
 */
@Composable
fun MapView(
    initialCameraPosition: Any? = null,
    onMapReady: (Any?) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Map View - Coming Soon")
    }
}

// Placeholder functions for map interactions
fun enableLocationComponent() {
    // TODO: Implement when Mapbox Maps SDK is added
}

fun flyToLocation(
    latitude: Double,
    longitude: Double,
) {
    // TODO: Implement when Mapbox Maps SDK is added
}

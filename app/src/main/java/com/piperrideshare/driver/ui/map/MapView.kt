package com.piperrideshare.driver.ui.map

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.location

// Simple marker state tracking
private var hasPickupMarker = false

fun addPickupMarker(
    mapView: MapView,
    latitude: Double,
    longitude: Double,
) {
    // For now, just fly to the pickup location
    // @Thomas - Add proper marker when annotation plugin is available
    flyToLocation(mapView, latitude = latitude, longitude = longitude)
    hasPickupMarker = true
}

fun clearPickupMarker() {
    hasPickupMarker = false
}

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {},
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                MapView(it).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )

                    getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                        mapView = this@apply
                        onMapReady(this@apply)
                    }
                }
            },
            modifier = Modifier.matchParentSize(),
        )

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.5f)),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = {
                    mapView?.let {
                        val currentZoom = it.getMapboxMap().cameraState.zoom
                        it.getMapboxMap().setCamera(
                            CameraOptions
                                .Builder()
                                .zoom(currentZoom + 1)
                                .build(),
                        )
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = Color.White,
                )
            }

            IconButton(
                onClick = {
                    mapView?.let {
                        val currentZoom = it.getMapboxMap().cameraState.zoom
                        it.getMapboxMap().setCamera(
                            CameraOptions
                                .Builder()
                                .zoom(currentZoom - 1)
                                .build(),
                        )
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color.White,
                )
            }
        }
    }
}

/**
 * Enables the location component on the map to show user's current location.
 */
fun enableLocationComponent(mapView: MapView) {
    val locationComponentPlugin = mapView.location
    locationComponentPlugin.updateSettings {
        enabled = true
        pulsingEnabled = true
    }
}

/**
 * Moves the camera to the specified location.
 * You can provide either a (latitude, longitude) pair or a nullable Pair.
 */
fun flyToLocation(
    mapView: MapView,
    latitude: Double? = null,
    longitude: Double? = null,
    location: Pair<Double, Double>? = null,
) {
    val (lat, lon) = location ?: (latitude to longitude)

    if (lat == null || lon == null) return

    val cameraOptions =
        CameraOptions
            .Builder()
            .center(Point.fromLngLat(lon, lat))
            .zoom(15.0)
            .build()
    mapView.getMapboxMap().setCamera(cameraOptions)
}

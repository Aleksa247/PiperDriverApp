package com.piperrideshare.driver.ui.components

import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.locationcomponent.location
import com.piperrideshare.driver.utils.LocationTracker
import kotlinx.coroutines.launch

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
fun PiperDriverMapView(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {},
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                MapView(it).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )

                    mapboxMap.loadStyle(
                        styleExtension = style(Style.MAPBOX_STREETS) {
                        }
                    ) { style ->
                        mapView = this@apply
                        onMapReady(this@apply)
                    }
                }
            },
            modifier = Modifier.matchParentSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = {
                    mapView?.let {
                        val currentZoom = it.mapboxMap.cameraState.zoom
                        it.mapboxMap.setCamera(
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
                        val currentZoom = it.mapboxMap.cameraState.zoom
                        it.mapboxMap.setCamera(
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

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val safeMapView = mapView ?: return@launch
                        forceRefreshLocation(
                            context = safeMapView.context,
                            mapView = safeMapView,
                            onLocationRefreshed = { latLng ->
                                latLng?.let { (lat, lon) ->
                                    flyToLocation(safeMapView, latitude = lat, longitude = lon)
                                }
                            },
                        )
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Refresh Location",
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
    mapView.mapboxMap.setCamera(cameraOptions)
}

/**
 * Force refreshes the user's location.
 *
 * Clears the location cache and gets a new location, useful to avoid "too close" errors.
 */
suspend fun forceRefreshLocation(
    context: Context,
    mapView: MapView,
    onLocationRefreshed: (location: Pair<Double, Double>?) -> Unit = {},
) {
    val locationTracker = LocationTracker(context)
    locationTracker.clearLocationCache()

    val newLocation = locationTracker.getCurrentLocation()
    if (newLocation != null) {
        onLocationRefreshed(newLocation)
        flyToLocation(mapView, location = newLocation)
    } else {
        Toast.makeText(context, "Failed to get new location", Toast.LENGTH_SHORT).show()
        onLocationRefreshed(null)
    }
}

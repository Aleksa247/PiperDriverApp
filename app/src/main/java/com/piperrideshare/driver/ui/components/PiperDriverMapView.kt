package com.piperrideshare.driver.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.piperrideshare.driver.R
import com.piperrideshare.driver.utils.LocationTracker
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

private var hasPickupMarker = false

fun addPickupMarker(
    mapView: MapView,
    destinationMarkerColor: String,
    latitude: Double,
    longitude: Double,
) {
    val annotationApi = mapView.annotations
    val pointAnnotationManager = annotationApi.createPointAnnotationManager()

    val markerSize = 40 // px
    val bitmap = createBitmap(markerSize, markerSize)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        isAntiAlias = true
        color = destinationMarkerColor.toColorInt()
        style = Paint.Style.FILL
    }

    val centerX = markerSize / 2f
    val centerY = markerSize / 2f
    val radius = markerSize / 2f

    canvas.drawCircle(centerX, centerY, radius, paint)

    val point = Point.fromLngLat(longitude, latitude)
    val pointAnnotationOptions = PointAnnotationOptions()
        .withPoint(point)
        .withIconImage(bitmap)

    pointAnnotationManager.deleteAll() // Optional: clear old markers
    pointAnnotationManager.create(pointAnnotationOptions)

    flyToLocation(mapView, latitude, longitude)
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
                .background(Color.Black.copy(alpha = 0.5f)),
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

fun drawRouteToDestination(
    context: Context,
    mapView: MapView,
    destinationMarkerColor: String,
    currentLocation: Pair<Double, Double>,
    destinationLocation: Pair<Double, Double>
) {
    val origin = Point.fromLngLat(currentLocation.second, currentLocation.first)
    val destination = Point.fromLngLat(destinationLocation.second, destinationLocation.first)

    val client = MapboxDirections.builder()
        .routeOptions(
            RouteOptions.builder()
                .coordinatesList(listOf(
                    origin,
                    destination
                ))
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .build()
        )
        .accessToken(context.getString(R.string.mapbox_access_token))
        .build()

    client.enqueueCall(object : Callback<DirectionsResponse> {
        override fun onResponse(
            call: Call<DirectionsResponse>,
            response: Response<DirectionsResponse>
        ) {
            val body = response.body()
            if (body == null || body.routes().isEmpty()) {
                Timber.d("No route found.")
                return
            }

            val route = body.routes()[0]
            val geometry = route.geometry() ?: return
            val routeLine = LineString.fromPolyline(geometry, 6)

            val polylineManager = mapView.annotations.createPolylineAnnotationManager()
            polylineManager.deleteAll()

            polylineManager.create(
                PolylineAnnotationOptions()
                    .withPoints(routeLine.coordinates())
                    .withLineColor("#3b9ddd")
                    .withLineWidth(5.0)
            )
            addPickupMarker(
                mapView,
                destinationMarkerColor,
                destinationLocation.first,
                destinationLocation.second
            )
        }

        override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
            Timber.e("Route call failed: ${t.localizedMessage}")
        }
    })
}

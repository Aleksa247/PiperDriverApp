package com.piperrideshare.driver.ui.components

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

fun updateRiderMarker(mapView: MapView, latitude: Double, longitude: Double) {
    val annotationApi = mapView.annotations
    val pointAnnotationManager = annotationApi.createPointAnnotationManager()

    // Remove previous rider marker
    pointAnnotationManager.annotations.filter { it.iconImage == "rider-marker" }.forEach {
        pointAnnotationManager.delete(it)
    }

    val markerSize = 20 // px
    val bitmap = createBitmap(markerSize, markerSize)
    val canvas = Canvas(bitmap)

    val paint =
        Paint().apply {
            isAntiAlias = true
            color = "#0000FF".toColorInt() // Blue color for rider
            style = Paint.Style.FILL
        }

    val centerX = markerSize / 2f
    val centerY = markerSize / 2f
    val radius = markerSize / 2f

    canvas.drawCircle(centerX, centerY, radius, paint)


    // Add new rider marker
    val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
        .withPoint(com.mapbox.geojson.Point.fromLngLat(longitude, latitude))
        .withIconImage(bitmap)
        .withIconImage("rider-marker") // Still using this to identify the marker

    pointAnnotationManager.create(pointAnnotationOptions)
}

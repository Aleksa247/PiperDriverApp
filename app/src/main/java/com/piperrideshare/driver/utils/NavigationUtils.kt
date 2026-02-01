package com.piperrideshare.driver.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // Handle case where Google Maps is not installed
        Toast.makeText(context, "Google Maps is not installed", Toast.LENGTH_SHORT).show()
    }
}

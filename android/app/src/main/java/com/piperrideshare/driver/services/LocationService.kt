package com.piperrideshare.driver.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    private val binder = LocationBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val NOTIFICATION_ID = 12345
        private const val CHANNEL_ID = "location_service_channel"

        val serviceRunning = MutableStateFlow(false)
        val currentLocation = MutableStateFlow<Location?>(null)
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        serviceRunning.value = true
        startLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceRunning.value = false
    }

    private fun setupLocationCallback() {
        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        currentLocation.value = location
                    }
                }
            }
    }

    private fun startLocationUpdates() {
        val locationRequest =
            LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper,
            )
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Used for location tracking"
            }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification =
        NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Driver App")
            .setContentText("Location tracking active")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Use system icon instead
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
}

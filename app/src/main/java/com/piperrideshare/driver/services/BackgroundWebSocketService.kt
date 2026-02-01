package com.piperrideshare.driver.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.piperrideshare.driver.R
import com.piperrideshare.driver.api.models.DriverAvailabilityState
import com.piperrideshare.driver.utils.Constants
import com.piperrideshare.driver.utils.LocationTracker
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundWebSocketService : Service() {

    @Inject
    lateinit var webSocketRepository: IWebSocketRepository

    @Inject
    lateinit var sessionManager: ISessionManager

    @Inject
    lateinit var driverStateManager: IDriverStateManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var locationUpdateJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_NOT_STICKY
    }

    private fun startService() {
        Timber.d("BackgroundWebSocketService started")
        startForeground(Constants.WEBSOCKET_NOTIFICATION_ID, createNotification())
        serviceScope.launch {
            sessionManager.userToken.first()?.let { token ->
                webSocketRepository.connect(token) { response: Any ->
                    Timber.d("Received message in background: $response")
                }
            }
        }

        startLocationUpdates()
    }

    private fun stopService() {
        Timber.d("BackgroundWebSocketService stopped")
        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "background_websocket_channel"
        val channelName = "Background WebSocket Service"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Piper Driver")
            .setContentText("App is running in background to receive ride requests.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun startLocationUpdates() {
        if (locationUpdateJob?.isActive == true) {
            Timber.d("Location updates already active")
            return
        }

        locationUpdateJob = serviceScope.launch {
            val locationTracker = LocationTracker(applicationContext)
            while (true) {
                val driverState = driverStateManager.getCurrentState()
                val isOnline = driverState?.availabilityState == DriverAvailabilityState.ONLINE

                if (isOnline) {
                    try {
                        val location = locationTracker.getCurrentLocation()
                        location?.let { (lat, lng) ->
                            Timber.d("Sending location update from background: $lat, $lng")
                            webSocketRepository.sendUpdateLocation(lat, lng)
                        }
                    } catch (e: Exception) {
                        Timber.e("Error getting location in background: ${e.message}")
                    }
                }
                delay(20_000)
            }
        }
    }

    private fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }
}
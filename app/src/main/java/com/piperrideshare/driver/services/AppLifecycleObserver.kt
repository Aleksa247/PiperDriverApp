package com.piperrideshare.driver.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.piperrideshare.driver.services.state.IDriverStateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driverStateManager: IDriverStateManager
) : LifecycleEventObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                // App backgrounded
                Timber.d("🔄 APP LIFECYCLE: App backgrounded")
                handleAppBackgrounded()
            }
            Lifecycle.Event.ON_START -> {
                // App foregrounded
                Timber.d("🔄 APP LIFECYCLE: App foregrounded")
                handleAppForegrounded()
            }
            else -> {
                // Ignore other lifecycle events
            }
        }
    }

    private fun handleAppBackgrounded() {
        scope.launch {
            try {
                val isOnline = driverStateManager.isOnline.first()
                val currentRideId = driverStateManager.currentRideId.first()

                Timber.d("🔄 APP LIFECYCLE: Driver online: $isOnline, Current ride: $currentRideId")

                if (isOnline) {
                    Timber.d("🔄 APP LIFECYCLE: Driver is online - need to start background service")
                    val serviceIntent = Intent(context, BackgroundWebSocketService::class.java).apply {
                        action = BackgroundWebSocketService.ACTION_START
                    }
                    context.startService(serviceIntent)
                } else {
                    Timber.d("🔄 APP LIFECYCLE: Driver is offline - no background service needed")
                }
            } catch (e: Exception) {
                Timber.e("❌ APP LIFECYCLE ERROR (background): ${e.message}")
            }
        }
    }

    private fun handleAppForegrounded() {
        scope.launch {
            try {
                Timber.d("🔄 APP LIFECYCLE: Stopping background service (if running)")
                val serviceIntent = Intent(context, BackgroundWebSocketService::class.java).apply {
                    action = BackgroundWebSocketService.ACTION_STOP
                }
                context.startService(serviceIntent)
                Timber.d("🔄 APP LIFECYCLE: ViewModel will resume WebSocket handling")
            } catch (e: Exception) {
                Timber.e("❌ APP LIFECYCLE ERROR (foreground): ${e.message}")
            }
        }
    }
}
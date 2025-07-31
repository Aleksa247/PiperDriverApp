package com.piperrideshare.driver

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.messaging
import com.google.firebase.perf.performance
import com.piperrideshare.driver.di.AppEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class PiperDriverApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Firebase Analytics collection
        FirebaseAnalytics.getInstance(this).apply {
            setAnalyticsCollectionEnabled(true)
            Timber.d("Firebase Analytics collection enabled")
        }

        // Enable Firebase Performance Monitoring
        Firebase.performance.apply {
            isPerformanceCollectionEnabled = true
            Timber.d("Firebase Performance Monitoring enabled")
        }

        fetchAndSaveFcmToken()

        initializeLogging()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized for verbose logging in debug mode")
        }
    }

    private fun fetchAndSaveFcmToken() {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("❌ Failed to fetch FCM token: ${task.exception?.message}")
                return@addOnCompleteListener
            }

            val token = task.result
            Timber.d("🔐 FCM token fetched manually: $token")

            // 👇 Use Hilt EntryPoint to get ISessionManager
            val entryPoint = EntryPointAccessors.fromApplication(this, AppEntryPoint::class.java)
            val sessionManager = entryPoint.sessionManager()

            CoroutineScope(Dispatchers.IO).launch {
                sessionManager.saveFcmToken(token)
            }
        }
    }
}

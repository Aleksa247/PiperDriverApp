package com.piperrideshare.driver

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class PiperDriverApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            // Enable verbose logging in debug builds
        }
    }
}
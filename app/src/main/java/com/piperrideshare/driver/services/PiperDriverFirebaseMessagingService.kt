package com.piperrideshare.driver.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.piperrideshare.driver.services.session.ISessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PiperDriverFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var sessionManager: ISessionManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("FCM Message received: ${remoteMessage.messageId}")

        remoteMessage.notification?.let { notification ->
            Timber.d("Notification Title: ${notification.title}, Body: ${notification.body}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Data Payload: ${remoteMessage.data}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM Token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.saveFcmToken(token)
        }
    }
}

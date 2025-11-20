package com.athreya.mathworkout.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.athreya.mathworkout.MainActivity
import com.athreya.mathworkout.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service for handling push notifications
 */
class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "challenge_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Send token to your server or store it
        // For now, we'll store it locally for testing
        getSharedPreferences("fcm", Context.MODE_PRIVATE)
            .edit()
            .putString("token", token)
            .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Check if message contains a data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataMessage(message.data)
        }

        // Check if message contains a notification payload
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "New Challenge", it.body ?: "You have a new challenge!")
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "new_challenge" -> {
                val challengerName = data["challenger_name"] ?: "Someone"
                val challengeId = data["challenge_id"]
                
                sendNotification(
                    title = getString(R.string.new_challenge_title),
                    body = getString(R.string.new_challenge_body, challengerName),
                    challengeId = challengeId
                )
            }
            "challenge_accepted" -> {
                val accepterName = data["accepter_name"] ?: "Someone"
                sendNotification(
                    title = "Challenge Accepted!",
                    body = "$accepterName accepted your challenge!"
                )
            }
            "challenge_completed" -> {
                val completedBy = data["completed_by"] ?: "Someone"
                sendNotification(
                    title = "Challenge Updated",
                    body = "$completedBy completed the challenge!"
                )
            }
        }
    }

    private fun sendNotification(
        title: String,
        body: String,
        challengeId: String? = null
    ) {
        // Create an intent to open the app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            challengeId?.let {
                putExtra("challenge_id", it)
                putExtra("open_challenges", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification channel for Android O and above
        createNotificationChannel()

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon for now
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setLights(0xFF00FF00.toInt(), 500, 500)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.challenge_notification_channel_name)
            val channelDescription = "Notifications for new challenges and updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

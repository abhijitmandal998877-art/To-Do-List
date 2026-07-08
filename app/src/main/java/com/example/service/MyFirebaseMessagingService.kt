package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "Refreshed token: $token")
        // Token can be sent to server if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM_SERVICE", "Message received from: ${remoteMessage.from}")

        // 1. Check if message contains a data payload to dynamically update admin announcement/popup settings
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM_SERVICE", "Message data payload: ${remoteMessage.data}")
            updateAdminSettingsFromPayload(remoteMessage.data)
        }

        // 2. Extract notification details
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        // If notification payload is empty, check data payload as fallback
        if (title.isNullOrEmpty()) {
            title = remoteMessage.data["title"] ?: remoteMessage.data["popup_title"] ?: "Broadcast Alert"
        }
        if (body.isNullOrEmpty()) {
            body = remoteMessage.data["body"] ?: remoteMessage.data["popup_text"] ?: "Tap to open the app"
        }

        // 3. Show notification
        showNotification(title, body)
    }

    private fun updateAdminSettingsFromPayload(data: Map<String, String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val taskDao = db.taskDao()
                val current = taskDao.getSettingsSync() ?: AppSettings()

                // Parse payload keys
                val showPopup = data["show_popup"]?.toBoolean() ?: current.adminShowPopup
                val popupTitle = data["popup_title"] ?: current.adminPopupTitle
                val popupText = data["popup_text"] ?: current.adminPopupText
                val popupMandatory = data["popup_mandatory"]?.toBoolean() ?: current.adminPopupMandatory
                val actionText = data["popup_action_text"] ?: current.adminPopupActionText
                val actionUrl = data["popup_action_url"] ?: current.adminPopupActionUrl
                val hasActionButton = data["popup_has_action_button"]?.toBoolean() ?: current.adminPopupHasActionButton

                // Update settings entity
                val updated = current.copy(
                    adminShowPopup = showPopup,
                    adminPopupTitle = popupTitle,
                    adminPopupText = popupText,
                    adminPopupMandatory = popupMandatory,
                    adminPopupActionText = actionText,
                    adminPopupActionUrl = actionUrl,
                    adminPopupHasActionButton = hasActionButton
                )

                taskDao.insertSettings(updated)
                Log.d("FCM_SERVICE", "Admin popup settings successfully updated via broadcast!")
            } catch (e: Exception) {
                Log.e("FCM_SERVICE", "Error updating settings from FCM payload", e)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "broadcast_notifications_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "System Broadcasts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency announcements, system notifications, and app updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher) // Set App Icon as Notification Icon as requested
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.delay
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("task_id", -1)
        val wishType = intent.getStringExtra("wish_type")

        if (taskId == -1 && wishType == null) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val settings = db.taskDao().getSettingsSync()

                if (wishType != null && settings != null) {
                    val enabled = if (wishType == "morning") settings.morningWishEnabled else settings.nightWishEnabled
                    if (enabled) {
                        val title = if (wishType == "morning") "Good Morning! 🌅" else "Good Night! 🌃"
                        val message = if (wishType == "morning") settings.morningWishText else settings.nightWishText
                        val id = if (wishType == "morning") -100 else -200
                        
                        showWishNotification(
                            context = context,
                            title = title,
                            message = message,
                            id = id,
                            soundEnabled = settings.isSoundEnabled,
                            vibrationEnabled = settings.isVibrationEnabled
                        )
                    }

                    // Reschedule for tomorrow
                    val hour = if (wishType == "morning") settings.morningWishHour else settings.nightWishHour
                    val minute = if (wishType == "morning") settings.morningWishMinute else settings.nightWishMinute
                    scheduleNextDailyWish(context, wishType, hour, minute)

                } else if (taskId != -1 && settings != null) {
                    val task = db.taskDao().getTaskById(taskId)
                    if (task != null && !task.isCompleted) {
                        showNotification(context, task.title, task.listName, taskId, settings.isSoundEnabled, settings.isVibrationEnabled)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNextDailyWish(context: Context, type: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("wish_type", type)
        }
        val requestCode = if (type == "morning") -100 else -200
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Schedule for tomorrow since we are rescheduling after it fired today
            add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun showWishNotification(
        context: Context,
        title: String,
        message: String,
        id: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wish_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (soundEnabled) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(channelId, "Daily Wishes", importance).apply {
                description = "Daily morning and night wishes from Admin"
                enableVibration(vibrationEnabled)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher) // Requirement 6: Use app icon as notification icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!soundEnabled) {
            builder.setSilent(true)
        }

        if (vibrationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        }

        notificationManager.notify(id, builder.build())
    }

    private fun showNotification(
        context: Context,
        title: String,
        listName: String,
        taskId: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (soundEnabled) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(channelId, "Task Reminders", importance).apply {
                description = "Reminders for scheduled tasks"
                enableVibration(vibrationEnabled)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher) // Requirement 6: Use app icon as notification icon
            .setContentTitle("Task Reminder: $title")
            .setContentText("Category: $listName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!soundEnabled) {
            builder.setSilent(true)
        } else {
            playTickingAlarmSound(context)
        }

        if (vibrationEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        }

        notificationManager.notify(taskId, builder.build())
    }

    private fun playTickingAlarmSound(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < 4000) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                    delay(250) // Tick 4 times a second
                }
                toneGenerator.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

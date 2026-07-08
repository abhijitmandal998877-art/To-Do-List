package com.example.ui

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.Task
import com.example.data.TaskList
import com.example.data.TaskRepository
import com.example.receiver.ReminderReceiver
import com.example.widget.TasksWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.TimeZone
import java.util.Calendar
import android.util.Base64

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val context = application.applicationContext

    private val _isSplashLoading = MutableStateFlow(true)
    val isSplashLoading: StateFlow<Boolean> = _isSplashLoading.asStateFlow()

    private val _selectedList = MutableStateFlow("All Lists")
    val selectedList: StateFlow<String> = _selectedList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allLists: StateFlow<List<TaskList>>
    val appSettings: StateFlow<AppSettings>

    // Dynamic tasks list filtered by list category and search query
    val currentTasks: StateFlow<List<Task>>
    val deletedTasks: StateFlow<List<Task>>
    val allTasksList: StateFlow<List<Task>>

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        allTasksList = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allLists = repository.allLists.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        appSettings = repository.appSettings.combine(MutableStateFlow(AppSettings())) { settings, default ->
            settings ?: default
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

        deletedTasks = repository.deletedTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentTasks = combine(
            repository.allTasks,
            repository.deletedTasks,
            _selectedList,
            _searchQuery
        ) { activeTasks, deletedTasksList, list, search ->
            var filtered = if (list == "All Lists") {
                activeTasks
            } else if (list == "Finished") {
                activeTasks.filter { it.isCompleted }
            } else if (list == "Trash Bin") {
                deletedTasksList
            } else {
                activeTasks.filter { it.listName == list }
            }

            if (search.isNotBlank()) {
                filtered = filtered.filter { it.title.contains(search, ignoreCase = true) }
            }
            filtered
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Run initialization
        viewModelScope.launch {
            repository.ensureDefaultsArePrepopulated()
            // Everyday Morning and Night Wish Alarms setup
            try {
                val settings = repository.getSettingsSync() ?: AppSettings()
                if (settings.morningWishEnabled) {
                    scheduleWishAlarm("morning", settings.morningWishHour, settings.morningWishMinute)
                }
                if (settings.nightWishEnabled) {
                    scheduleWishAlarm("night", settings.nightWishHour, settings.nightWishMinute)
                }
            } catch (e: Exception) {
                // Keep app from crashing on start if scheduling alarms fails
            }
            // Splash loading animation duration: 2 seconds
            kotlinx.coroutines.delay(2000)
            _isSplashLoading.value = false
        }
    }

    fun selectList(listName: String) {
        _selectedList.value = listName
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Push Notifications Helper
    fun sendPushNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_updates_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Task Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Updates when tasks are added, edited, deleted, or completed"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Title and description are both optional
        val finalTitle = if (title.trim().isEmpty()) "Broadcast Alert" else title.trim()
        val finalMessage = if (message.trim().isEmpty()) "Tap to view update details" else message.trim()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher) // Requirement 6: Use app icon as notification icon
            .setContentTitle(finalTitle)
            .setContentText(finalMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Task Actions
    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            if (updated.isCompleted) {
                cancelReminderAlarm(updated.id)
                sendPushNotification("Task Finished", "Completed: ${task.title}")
            } else {
                if (updated.hasReminder && updated.reminderTime > System.currentTimeMillis()) {
                    scheduleReminderAlarm(updated.id, updated.reminderTime)
                }
                sendPushNotification("Task Unfinished", "Reopened: ${task.title}")
            }
            updateWidget()
        }
    }

    fun addTask(
        title: String,
        dueDate: Long,
        listName: String,
        repeatInterval: String,
        hasReminder: Boolean,
        reminderTime: Long,
        syncToDeviceCalendar: Boolean
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                dueDate = dueDate,
                listName = listName,
                repeatInterval = repeatInterval,
                hasReminder = hasReminder,
                reminderTime = reminderTime
            )
            val taskId = repository.insertTask(task).toInt()

            if (hasReminder && reminderTime > System.currentTimeMillis()) {
                scheduleReminderAlarm(taskId, reminderTime)
            }

            if (syncToDeviceCalendar && dueDate > 0) {
                syncToCalendar(task.copy(id = taskId))
            }

            sendPushNotification("Task Added", "Created task: $title")
            updateWidget()
        }
    }

    fun editTask(
        task: Task,
        title: String,
        dueDate: Long,
        listName: String,
        repeatInterval: String,
        hasReminder: Boolean,
        reminderTime: Long
    ) {
        viewModelScope.launch {
            val updated = task.copy(
                title = title,
                dueDate = dueDate,
                listName = listName,
                repeatInterval = repeatInterval,
                hasReminder = hasReminder,
                reminderTime = reminderTime
            )
            repository.updateTask(updated)

            cancelReminderAlarm(task.id)
            if (hasReminder && reminderTime > System.currentTimeMillis() && !updated.isCompleted) {
                scheduleReminderAlarm(task.id, reminderTime)
            }

            sendPushNotification("Task Edited", "Updated: $title")
            updateWidget()
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isDeleted = false)
            repository.updateTask(updated)
            if (updated.hasReminder && updated.reminderTime > System.currentTimeMillis() && !updated.isCompleted) {
                scheduleReminderAlarm(updated.id, updated.reminderTime)
            }
            sendPushNotification("Task Restored", "Restored: ${task.title}")
            updateWidget()
        }
    }

    fun deletePermanent(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            sendPushNotification("Task Deleted Permanently", "Removed from Trash: ${task.title}")
            updateWidget()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isDeleted = true)
            repository.updateTask(updated)
            cancelReminderAlarm(task.id)
            sendPushNotification("Task Moved to Trash", "Moved to Trash: ${task.title}")
            updateWidget()
        }
    }

    // List Actions
    fun addList(name: String) {
        viewModelScope.launch {
            repository.insertList(TaskList(name = name))
        }
    }

    fun deleteList(name: String) {
        viewModelScope.launch {
            repository.deleteListByName(name)
            if (_selectedList.value == name) {
                _selectedList.value = "All Lists"
            }
        }
    }

    fun renameList(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.renameList(oldName, newName)
            if (_selectedList.value == oldName) {
                _selectedList.value = newName
            }
        }
    }

    // Settings Actions
    fun updateSettings(
        theme: String,
        isVibrationEnabled: Boolean,
        isSoundEnabled: Boolean,
        timeFormat24Hour: Boolean,
        firstDayOfWeek: String
    ) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            repository.insertSettings(
                current.copy(
                    theme = theme,
                    isVibrationEnabled = isVibrationEnabled,
                    isSoundEnabled = isSoundEnabled,
                    timeFormat24Hour = timeFormat24Hour,
                    firstDayOfWeek = firstDayOfWeek
                )
            )
        }
    }

    fun updateTimezone(timezone: String) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            repository.insertSettings(current.copy(timezone = timezone))
        }
    }

    fun updateAdminPassword(password: String) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            // Store password as base64-encoded string for security
            val encodedPassword = Base64.encodeToString(password.trim().toByteArray(), Base64.NO_WRAP)
            repository.insertSettings(current.copy(adminPassword = encodedPassword))
        }
    }

    fun updateAdminPopupSettings(
        show: Boolean,
        title: String,
        text: String,
        mandatory: Boolean,
        actionText: String = "Learn More",
        actionUrl: String = "https://google.com",
        hasActionButton: Boolean = false
    ) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            repository.insertSettings(
                current.copy(
                    adminShowPopup = show,
                    adminPopupTitle = title,
                    adminPopupText = text,
                    adminPopupMandatory = mandatory,
                    adminPopupActionText = actionText,
                    adminPopupActionUrl = actionUrl,
                    adminPopupHasActionButton = hasActionButton
                )
            )
        }
    }

    fun updateFirstTimeUser(isFirstTime: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            repository.insertSettings(current.copy(isFirstTimeUser = isFirstTime))
        }
    }

    fun updateMorningWishSettings(hour: Int, minute: Int, message: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            repository.insertSettings(
                current.copy(
                    morningWishHour = hour,
                    morningWishMinute = minute,
                    morningWishText = message,
                    morningWishEnabled = enabled
                )
            )
            if (enabled) {
                scheduleWishAlarm("morning", hour, minute)
            } else {
                cancelWishAlarm("morning")
            }
        }
    }

    fun updateNightWishSettings(hour: Int, minute: Int, message: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettingsSync() ?: AppSettings()
            repository.insertSettings(
                current.copy(
                    nightWishHour = hour,
                    nightWishMinute = minute,
                    nightWishText = message,
                    nightWishEnabled = enabled
                )
            )
            if (enabled) {
                scheduleWishAlarm("night", hour, minute)
            } else {
                cancelWishAlarm("night")
            }
        }
    }

    // Everyday Morning and Night Wish Alarms Helpers
    fun scheduleWishAlarm(type: String, hour: Int, minute: Int) {
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
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
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

    fun cancelWishAlarm(type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestCode = if (type == "morning") -100 else -200
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun decodeBase64(input: String): String {
        return try {
            String(Base64.decode(input, Base64.NO_WRAP))
        } catch (e: Exception) {
            input
        }
    }

    // Reminder Alarms Helper
    private fun scheduleReminderAlarm(taskId: Int, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("task_id", taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelReminderAlarm(taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    // Calendar Synchronization (Android ContentProvider integration)
    fun syncToCalendar(task: Task): Boolean {
        if (task.dueDate == 0L) return false
        try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, task.dueDate)
                put(CalendarContract.Events.DTEND, task.dueDate + 3600000) // 1 hour duration
                put(CalendarContract.Events.TITLE, task.title)
                put(CalendarContract.Events.DESCRIPTION, "Deadline synchronized from To Do List App")
                put(CalendarContract.Events.CALENDAR_ID, 1) // default system calendar id
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                viewModelScope.launch {
                    repository.updateTask(task.copy(isSyncWithCalendar = true))
                }
                return true
            }
        } catch (e: SecurityException) {
            // Write Calendar permission is required
        } catch (e: Exception) {
            // Other exceptions
        }
        return false
    }

    private fun updateWidget() {
        TasksWidgetProvider.triggerUpdate(context)
    }
}

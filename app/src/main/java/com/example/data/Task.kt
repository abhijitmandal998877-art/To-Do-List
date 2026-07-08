package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dueDate: Long = 0, // timestamp, 0 if none
    val listName: String = "Default",
    val isCompleted: Boolean = false,
    val repeatInterval: String = "Once", // Once, Daily, Weekly, Monthly
    val hasReminder: Boolean = false,
    val reminderTime: Long = 0, // timestamp, 0 if none
    val isSyncWithCalendar: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(tableName = "task_lists")
data class TaskList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isSystemList: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val theme: String = "System", // Light, Dark, System
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val timeFormat24Hour: Boolean = false,
    val firstDayOfWeek: String = "Sunday",
    val timezone: String = "GMT+5:30",
    val adminPassword: String = "QUJISTkyNDI=", // Base64 encoded ABHI9242
    val adminShowPopup: Boolean = false,
    val adminPopupTitle: String = "Welcome Note",
    val adminPopupText: String = "Welcome to our Todo & Productivity App!",
    val adminPopupMandatory: Boolean = false,
    val isFirstTimeUser: Boolean = true,
    val morningWishHour: Int = 8,
    val morningWishMinute: Int = 0,
    val morningWishText: String = "Good morning! Start your day with a positive mindset.",
    val morningWishEnabled: Boolean = true,
    val nightWishHour: Int = 21,
    val nightWishMinute: Int = 0,
    val nightWishText: String = "Good night! Review your completed tasks and rest well.",
    val nightWishEnabled: Boolean = true,
    val adminPopupActionText: String = "Learn More",
    val adminPopupActionUrl: String = "https://google.com",
    val adminPopupHasActionButton: Boolean = false
)

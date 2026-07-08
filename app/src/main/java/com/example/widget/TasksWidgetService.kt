package com.example.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Task
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksWidgetFactory(this.applicationContext)
    }
}

class TasksWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var taskList: List<Task> = emptyList()

    override fun onCreate() {
        // Initial setup
    }

    override fun onDataSetChanged() {
        // Fetch tasks synchronously
        runBlocking {
            try {
                val db = AppDatabase.getDatabase(context)
                // Fetch only incomplete tasks
                val allTasksFlow = db.taskDao().getAllTasks()
                val tasks = allTasksFlow.firstOrNull() ?: emptyList()
                taskList = tasks.filter { !it.isCompleted }
            } catch (e: Exception) {
                taskList = emptyList()
            }
        }
    }

    override fun onDestroy() {
        taskList = emptyList()
    }

    override fun getCount(): Int = taskList.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position >= count || position < 0) return null

        val task = taskList[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item)

        views.setTextViewText(R.id.widget_item_title, task.title)

        // Set status icon
        if (task.isCompleted) {
            views.setImageViewResource(R.id.widget_item_status, android.R.drawable.checkbox_on_background)
        } else {
            views.setImageViewResource(R.id.widget_item_status, android.R.drawable.checkbox_off_background)
        }

        // FillInIntent to pass task details on item click
        val fillInIntent = Intent().apply {
            val extras = Bundle().apply {
                putInt("task_id", task.id)
            }
            putExtras(extras)
        }
        views.setOnClickFillInIntent(R.id.widget_item_status, fillInIntent)
        views.setOnClickFillInIntent(R.id.widget_item_title, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = taskList.getOrNull(position)?.id?.toLong() ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}

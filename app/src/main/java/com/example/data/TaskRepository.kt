package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    // Tasks API
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val deletedTasks: Flow<List<Task>> = taskDao.getDeletedTasks()

    fun getTasksByList(listName: String): Flow<List<Task>> {
        return if (listName == "All Lists") {
            taskDao.getAllTasks()
        } else {
            taskDao.getTasksByListName(listName)
        }
    }

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun updateTaskCompletion(id: Int, isCompleted: Boolean) {
        taskDao.updateTaskCompletion(id, isCompleted)
    }

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Int) = taskDao.deleteTaskById(id)

    // Lists API
    val allLists: Flow<List<TaskList>> = taskDao.getAllLists()

    suspend fun insertList(list: TaskList) = taskDao.insertList(list)

    suspend fun deleteListByName(name: String) {
        taskDao.trashTasksByListName(name)
        taskDao.deleteListByName(name)
    }

    suspend fun renameList(oldName: String, newName: String) {
        taskDao.insertList(TaskList(name = newName, isSystemList = false))
        taskDao.updateTasksListName(oldName, newName)
        taskDao.deleteListByName(oldName)
    }

    // Settings API
    val appSettings: Flow<AppSettings?> = taskDao.getSettings()

    suspend fun getSettingsSync(): AppSettings? = taskDao.getSettingsSync()

    suspend fun insertSettings(settings: AppSettings) = taskDao.insertSettings(settings)

    // Prepopulation
    suspend fun ensureDefaultsArePrepopulated() {
        if (taskDao.getListsCount() == 0) {
            val defaultLists = listOf(
                TaskList(name = "Default", isSystemList = true),
                TaskList(name = "Personal", isSystemList = false),
                TaskList(name = "Shopping", isSystemList = false),
                TaskList(name = "Wishlist", isSystemList = false),
                TaskList(name = "Work", isSystemList = false)
            )
            for (list in defaultLists) {
                taskDao.insertList(list)
            }
        }

        if (taskDao.getSettingsSync() == null) {
            taskDao.insertSettings(AppSettings())
        }
    }
}

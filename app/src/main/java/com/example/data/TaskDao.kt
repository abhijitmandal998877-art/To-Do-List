package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Tasks Queries
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY dueDate ASC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND listName = :listName ORDER BY dueDate ASC, id DESC")
    fun getTasksByListName(listName: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDeleted = 1 ORDER BY id DESC")
    fun getDeletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskCompletion(id: Int, isCompleted: Boolean)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    // Task Lists Queries
    @Query("SELECT * FROM task_lists ORDER BY id ASC")
    fun getAllLists(): Flow<List<TaskList>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertList(list: TaskList)

    @Query("DELETE FROM task_lists WHERE name = :name AND isSystemList = 0")
    suspend fun deleteListByName(name: String)

    @Query("UPDATE tasks SET listName = :newName WHERE listName = :oldName")
    suspend fun updateTasksListName(oldName: String, newName: String)

    @Query("UPDATE tasks SET isDeleted = 1 WHERE listName = :listName")
    suspend fun trashTasksByListName(listName: String)

    @Query("SELECT COUNT(*) FROM task_lists")
    suspend fun getListsCount(): Int

    // App Settings Queries
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
}

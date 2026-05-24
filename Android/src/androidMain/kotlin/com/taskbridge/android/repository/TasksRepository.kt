package com.taskbridge.android.repository

import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.usecases.tasks.TasksState
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
    val tasksState: Flow<TasksState>
    suspend fun loadTasks()
    suspend fun createTask(task: TaskItem)
    suspend fun replaceTask(task: TaskItem)
    suspend fun deleteTaskTree(taskId: TaskId)
}

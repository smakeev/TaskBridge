package com.taskbridge.core.services.tasks

import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.services.common.ServiceData

internal data class TasksServiceData(
    val tasks: List<TaskItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : ServiceData {

    /**
     * Recursively searches for a task by its ID in the task tree.
     */
    fun findTask(id: TaskId): TaskItem? {
        fun search(items: List<TaskItem>): TaskItem? {
            for (item in items) {
                if (item.id == id) return item
                val found = search(item.children)
                if (found != null) return found
            }
            return null
        }
        return search(tasks)
    }
}

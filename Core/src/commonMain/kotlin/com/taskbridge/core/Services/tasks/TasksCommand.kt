package com.taskbridge.core.services.tasks

import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.services.common.ServiceCommand

internal sealed interface TasksCommand : ServiceCommand {
    data object LoadTasks : TasksCommand

    data class CreateTask(
        val task: TaskItem
    ) : TasksCommand

    data class ReplaceTask(
        val task: TaskItem
    ) : TasksCommand

    data class DeleteTaskTree(
        val taskId: TaskId
    ) : TasksCommand
}

package com.taskbridge.core.stories.tasks

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.services.tasks.TasksCommand

/**
 * Internal story for creating a new task and persisting its tree.
 */
internal class CreateTaskStory(
    private val assembler: CoreAssembler
) {
    suspend fun createTask(task: TaskItem) {
        assembler.services.tasksService().sendCommand(TasksCommand.CreateTask(task))
    }
}

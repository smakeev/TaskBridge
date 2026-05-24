package com.taskbridge.core.stories.tasks

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.tasks.TasksCommand

/**
 * Internal story for triggering a load of all tasks from storage.
 */
internal class LoadTasksStory(
    private val assembler: CoreAssembler
) {
    suspend fun loadTasks() {
        assembler.services.tasksService().sendCommand(TasksCommand.LoadTasks)
    }
}

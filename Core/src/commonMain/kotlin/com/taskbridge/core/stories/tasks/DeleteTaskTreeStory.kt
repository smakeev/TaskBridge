package com.taskbridge.core.stories.tasks

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.services.tasks.TasksCommand

/**
 * Internal story for deleting an entire task tree starting from a given ID.
 */
internal class DeleteTaskTreeStory(
    private val assembler: CoreAssembler
) {
    suspend fun deleteTaskTree(taskId: TaskId) {
        assembler.services.tasksService().sendCommand(TasksCommand.DeleteTaskTree(taskId))
    }
}

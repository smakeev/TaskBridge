package com.taskbridge.core.stories.tasks

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.services.tasks.TasksCommand

/**
 * Internal story for replacing an existing task's subtree.
 */
internal class ReplaceTaskStory(
    private val assembler: CoreAssembler
) {
    suspend fun replaceTask(task: TaskItem) {
        assembler.services.tasksService().sendCommand(TasksCommand.ReplaceTask(task))
    }
}

package com.taskbridge.core.stories.tasks

import com.taskbridge.core.composition.CoreAssembler
import com.taskbridge.core.services.tasks.TasksServiceData
import kotlinx.coroutines.flow.Flow

/**
 * Internal story for observing the current tasks state.
 */
internal class ObserveTasksStory(
    private val assembler: CoreAssembler
) {
    fun observeTasks(): Flow<TasksServiceData> {
        return assembler.services.tasksService().data
    }
}

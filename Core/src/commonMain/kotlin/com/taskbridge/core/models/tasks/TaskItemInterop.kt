package com.taskbridge.core.models.tasks

/**
 * Small Swift interop helpers for models that include Kotlin value classes.
 */
public object TaskItemInterop {
    public fun progressTask(
        id: TaskId,
        parentId: TaskId?,
        title: String
    ): TaskItem {
        return TaskItem(
            id = id,
            parentId = parentId,
            title = title,
            type = TaskType.PROGRESS,
            isDone = null,
            progress = TaskProgress(TaskProgress.MIN_VALUE),
            children = emptyList()
        )
    }

    public fun withProgress(task: TaskItem, progressValue: Int): TaskItem {
        return task.copy(
            progress = TaskProgress(progressValue.coerceIn(TaskProgress.MIN_VALUE, TaskProgress.MAX_VALUE))
        )
    }

    public fun progressValue(task: TaskItem): Int {
        return task.progress?.value ?: TaskProgress.MIN_VALUE
    }
}

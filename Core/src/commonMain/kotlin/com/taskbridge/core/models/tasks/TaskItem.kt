package com.taskbridge.core.models.tasks

/**
 * Domain model representing a Task item.
 *
 * Support three mutually exclusive task kinds:
 * - CHECKBOX: Uses [isDone].
 * - PROGRESS: Uses [progress].
 * - CONTAINER: Uses [children], completion is derived from children.
 */
public data class TaskItem(
    val id: TaskId,
    val title: String,
    val type: TaskType,
    val isDone: Boolean?,
    val progress: TaskProgress?,
    val children: List<TaskItem>
) {
    /**
     * Derived property indicating if the task is fully completed.
     * - For CHECKBOX: true if [isDone] is true.
     * - For PROGRESS: true if [progress] is 100%.
     * - For CONTAINER: true if it has children and all children are completed.
     */
    val isCompleted: Boolean
        get() = when (type) {
            TaskType.CHECKBOX -> isDone == true
            TaskType.PROGRESS -> progress?.value == TaskProgress.MAX_VALUE
            TaskType.CONTAINER -> children.isNotEmpty() && children.all { it.isCompleted }
        }
}

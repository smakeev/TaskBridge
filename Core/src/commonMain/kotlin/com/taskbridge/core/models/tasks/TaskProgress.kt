package com.taskbridge.core.models.tasks

import kotlin.jvm.JvmInline

/**
 * Value class representing the progress of a task.
 * Constrained between 0 and 100.
 */
@JvmInline
public value class TaskProgress(
    val value: Int
) {
    init {
        require(value in MIN_VALUE..MAX_VALUE) {
            "Task progress must be between $MIN_VALUE and $MAX_VALUE."
        }
    }

    public companion object {
        public const val MIN_VALUE: Int = 0
        public const val MAX_VALUE: Int = 100
    }
}

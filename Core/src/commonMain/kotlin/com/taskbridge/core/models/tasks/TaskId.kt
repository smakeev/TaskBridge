package com.taskbridge.core.models.tasks

import kotlin.jvm.JvmInline

/**
 * Value class representing a unique identifier for a Task.
 */
@JvmInline
public value class TaskId(
    val value: String
)

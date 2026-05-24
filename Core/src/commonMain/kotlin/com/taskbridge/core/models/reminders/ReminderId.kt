package com.taskbridge.core.models.reminders

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Value class representing a unique identifier for a Reminder.
 */
@JvmInline
@Serializable
public value class ReminderId(
    val value: String
)

package com.taskbridge.core.models.reminders

import kotlinx.serialization.Serializable

/**
 * Data class representing a unique identifier for a Reminder.
 * Using data class instead of value class for better Swift interoperability.
 */
@Serializable
public data class ReminderId(
    val value: String
)

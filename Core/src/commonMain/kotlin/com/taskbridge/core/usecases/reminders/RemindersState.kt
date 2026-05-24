package com.taskbridge.core.usecases.reminders

import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId

/**
 * Public state model for reminders.
 */
public data class RemindersState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Finds a reminder by its ID.
     */
    fun findReminder(id: ReminderId): Reminder? {
        return reminders.find { it.id == id }
    }
}

package com.taskbridge.core.services.reminders

import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.services.common.ServiceData

/**
 * Internal state model for the Reminders service.
 */
internal data class RemindersServiceData(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : ServiceData {
    /**
     * Finds a reminder by its ID.
     */
    fun findReminder(id: ReminderId): Reminder? {
        return reminders.find { it.id == id }
    }
}

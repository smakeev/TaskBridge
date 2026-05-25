package com.taskbridge.android.repository

import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.usecases.reminders.RemindersState
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for managing reminders on Android.
 */
interface RemindersRepository {
    val remindersState: StateFlow<RemindersState>
    
    suspend fun loadReminders()
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(reminderId: ReminderId)
}

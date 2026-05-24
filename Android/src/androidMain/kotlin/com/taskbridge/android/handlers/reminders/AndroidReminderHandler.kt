package com.taskbridge.android.handlers.reminders

import android.content.Context
import com.taskbridge.core.events.CoreEventEmitter
import com.taskbridge.core.handlers.reminders.ReminderHandler
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.models.reminders.ReminderType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Android implementation of [ReminderHandler].
 * Stores reminders in SharedPreferences and emits updates to Core.
 * TODO: Implement real AlarmManager scheduling for production.
 */
class AndroidReminderHandler(
    private val context: Context
) : ReminderHandler {
    private var eventEmitter: CoreEventEmitter? = null
    private val sharedPrefs = context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun setEventEmitter(emitter: CoreEventEmitter) {
        this.eventEmitter = emitter
    }

    override suspend fun getAllReminders(): List<Reminder> {
        val serialized = sharedPrefs.getString("scheduled_reminders", "[]") ?: "[]"
        return try {
            // Simplified mapping since we don't have DTOs for reminders yet
            // In production, use explicit DTOs and mappers.
            json.decodeFromString<List<Reminder>>(serialized)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun scheduleReminder(reminder: Reminder) {
        val current = getAllReminders().toMutableList()
        current.removeAll { it.id == reminder.id }
        current.add(reminder)
        saveReminders(current)
        
        // TODO: Schedule real Android notification via WorkManager or AlarmManager
        
        emitUpdate()
    }

    override suspend fun cancelReminder(reminderId: ReminderId) {
        val current = getAllReminders().toMutableList()
        if (current.removeAll { it.id == reminderId }) {
            saveReminders(current)
            // TODO: Cancel real Android alarm/notification
            emitUpdate()
        }
    }

    private fun saveReminders(reminders: List<Reminder>) {
        val serialized = json.encodeToString(reminders)
        sharedPrefs.edit().putString("scheduled_reminders", serialized).apply()
    }

    private suspend fun emitUpdate() {
        eventEmitter?.remindersUpdated(getAllReminders())
    }
}

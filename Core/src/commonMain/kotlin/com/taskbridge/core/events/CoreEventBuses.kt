package com.taskbridge.core.events

import com.taskbridge.core.events.common.CoreEventBus
import com.taskbridge.core.events.reminders.ReminderEvent

/**
 * Internal container for all application event buses.
 */
internal class CoreEventBuses {
    val reminderEvents = CoreEventBus<ReminderEvent>()
}

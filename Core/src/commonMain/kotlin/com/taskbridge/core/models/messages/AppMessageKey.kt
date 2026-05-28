package com.taskbridge.core.models.messages

import kotlin.reflect.KClass

public class AppMessageKey internal constructor(
    internal val type: KClass<out AppMessage>
) {
    override fun equals(other: Any?): Boolean {
        return other is AppMessageKey && other.type == type
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}

public object AppMessageKeys {
    public val reminderCreated: AppMessageKey = AppMessageKey(AppMessage.ReminderCreated::class)
    public val taskAdded: AppMessageKey = AppMessageKey(AppMessage.TaskAdded::class)
}

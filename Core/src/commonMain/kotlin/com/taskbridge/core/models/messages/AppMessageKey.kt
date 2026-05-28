package com.taskbridge.core.models.messages

import kotlin.reflect.KClass

public class AppMessageKey internal constructor(
    internal val type: KClass<out AppMessage>
)

public object AppMessageKeys {
    public val reminderCreated: AppMessageKey = AppMessageKey(AppMessage.ReminderCreated::class)
}

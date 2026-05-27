package com.taskbridge.core.models.messages

import kotlin.reflect.KClass

public object AppMessageTypes {
    public val reminderCreated: KClass<out AppMessage> = AppMessage.ReminderCreated::class
}

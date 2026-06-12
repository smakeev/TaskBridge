package com.taskbridge.core.composition

import com.taskbridge.core.events.common.CoreEventBus
import com.taskbridge.core.events.reminders.ReminderEvent
import com.taskbridge.core.handlers.reminders.ReminderHandler
import com.taskbridge.core.messages.MessageCenter
import com.taskbridge.core.network.HttpJsonClient
import com.taskbridge.core.network.JsonRequestManager
import com.taskbridge.core.services.appstate.AppStateService
import com.taskbridge.core.services.messages.MessagesService
import com.taskbridge.core.services.network.NetworkService
import com.taskbridge.core.services.remote.RemoteResourceService
import com.taskbridge.core.services.reminders.RemindersService
import com.taskbridge.core.services.tasks.TasksService
import com.taskbridge.core.storage.tasks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock

/**
 * Locator for internal services.
 * Creates and keeps service instances lazily.
 */
internal class CoreServiceLocator(
    private val platformDependencies: PlatformDependencies,
    private val reminderHandler: ReminderHandler,
    private val reminderEvents: CoreEventBus<ReminderEvent>,
    // Back-reference to the owning assembler. Only dereferenced inside the `by lazy`
    // service builders below — i.e. after the assembler is fully constructed — so the
    // forward reference is safe (see CoreAssembler).
    private val assembler: CoreAssembler
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val appStateServiceInstance: AppStateService by lazy {
        AppStateService(scope)
    }

    private val messageCenterInstance: MessageCenter by lazy {
        MessageCenter()
    }

    private val messagesServiceInstance: MessagesService by lazy {
        MessagesService(
            messageCenter = messageCenterInstance,
            scope = scope
        )
    }

    private val httpJsonClientInstance: HttpJsonClient by lazy {
        HttpJsonClient()
    }

    private val jsonRequestManagerInstance: JsonRequestManager by lazy {
        JsonRequestManager(httpJsonClientInstance, scope)
    }

    private val networkServiceInstance: NetworkService by lazy {
        NetworkService(jsonRequestManagerInstance, scope)
    }

    private val remoteResourceServiceInstance: RemoteResourceService by lazy {
        RemoteResourceService(
            scope = scope,
            timeProvider = { Clock.System.now().toEpochMilliseconds() }
        )
    }

    private val taskDatabaseInstance: TaskDatabase by lazy {
        getDatabaseBuilder(platformDependencies).build()
    }

    private val taskStorageManagerInstance: TaskStorageManager by lazy {
        TaskStorageManager(taskDatabaseInstance)
    }

    private val tasksServiceInstance: TasksService by lazy {
        TasksService(
            scope = scope,
            storageManager = taskStorageManagerInstance,
            publishMessageStory = assembler.stories.publishMessage(assembler)
        )
    }

    private val remindersServiceInstance: RemindersService by lazy {
        RemindersService(
            scope = scope,
            reminderHandler = reminderHandler,
            reminderEvents = reminderEvents,
            publishMessageStory = assembler.stories.publishMessage(assembler)
        )
    }

    fun appStateService(): AppStateService = appStateServiceInstance

    fun messagesService(): MessagesService = messagesServiceInstance

    fun networkService(): NetworkService = networkServiceInstance

    fun remoteResourceService(): RemoteResourceService = remoteResourceServiceInstance

    fun taskStorageManager(): TaskStorageManager = taskStorageManagerInstance

    fun tasksService(): TasksService = tasksServiceInstance

    fun remindersService(): RemindersService = remindersServiceInstance
}

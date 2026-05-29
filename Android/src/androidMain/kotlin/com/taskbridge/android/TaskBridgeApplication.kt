package com.taskbridge.android

import android.app.Application
import com.taskbridge.android.handlers.reminders.AndroidReminderHandler
import com.taskbridge.android.repository.RepositoriesStorage
import com.taskbridge.core.TaskBridge
import com.taskbridge.core.handlers.CorePlatformHandlers
import com.taskbridge.core.storage.tasks.PlatformDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Process-lifetime owner of the Core dependency graph.
 *
 * The graph (TaskBridge + repositories) used to be built in [MainActivity.onCreate],
 * which rebuilt it — and cancelled its state-sharing scope — on every configuration
 * change (rotation). Building it here, once per process, lets it survive Activity
 * recreation. The Activity now only *reads* [repositoriesStorage].
 */
class TaskBridgeApplication : Application() {

    /**
     * Application-scoped scope used by repositories for state sharing
     * (`stateIn` / `WhileSubscribed`). It lives for the whole process, matching the
     * single [TaskBridge] instance, instead of the previous per-Activity
     * `lifecycleScope` that was torn down on rotation.
     */
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val repositoriesStorage: RepositoriesStorage by lazy {
        val platformDependencies = PlatformDependencies(this)
        val platformHandlers = CorePlatformHandlers(
            reminderHandler = AndroidReminderHandler(this)
        )
        val taskBridge = TaskBridge(platformDependencies, platformHandlers)
        RepositoriesStorage.create(taskBridge, appScope)
    }
}

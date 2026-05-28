package com.taskbridge.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import com.taskbridge.android.handlers.reminders.AndroidReminderHandler
import com.taskbridge.android.repository.MessagesRepository
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.android.repository.impl.MessagesRepositoryImpl
import com.taskbridge.android.repository.impl.NavigationRepositoryImpl
import com.taskbridge.android.repository.impl.RemindersRepositoryImpl
import com.taskbridge.android.repository.impl.TaskTemplatesRepositoryImpl
import com.taskbridge.android.repository.impl.TasksRepositoryImpl
import com.taskbridge.android.ui.navigation.RemindersNavigationScreen
import com.taskbridge.android.ui.navigation.TasksNavigationScreen
import com.taskbridge.android.ui.navigation.TemplatesNavigationScreen
import com.taskbridge.core.TaskBridge
import com.taskbridge.core.handlers.CorePlatformHandlers
import com.taskbridge.core.models.messages.AppMessageKeys
import com.taskbridge.core.models.messages.Toastable
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.storage.tasks.PlatformDependencies
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val platformDependencies = PlatformDependencies(this)
        val platformHandlers = CorePlatformHandlers(
            reminderHandler = AndroidReminderHandler(this)
        )
        val taskBridge = TaskBridge(platformDependencies, platformHandlers)
        
        val navigationInteractor = taskBridge.navigationInteractor()
        val templatesInteractor = taskBridge.templatesInteractor()
        val tasksInteractor = taskBridge.tasksInteractor()
        val remindersInteractor = taskBridge.remindersInteractor()
        val messagesInteractor = taskBridge.messagesInteractor()
        
        val navigationRepository: NavigationRepository = NavigationRepositoryImpl(navigationInteractor)
        val templatesRepository: TaskTemplatesRepository = TaskTemplatesRepositoryImpl(templatesInteractor)
        val tasksRepository: TasksRepository = TasksRepositoryImpl(tasksInteractor)
        val remindersRepository: RemindersRepository = RemindersRepositoryImpl(remindersInteractor, lifecycleScope)
        val messagesRepository: MessagesRepository = MessagesRepositoryImpl(messagesInteractor)

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val currentTab by navigationRepository.currentTab.collectAsState(initial = AppTab.TASKS)

            LaunchedEffect(messagesRepository, snackbarHostState) {
                messagesRepository.observe(
                    listOf(
                        AppMessageKeys.reminderCreated,
                        AppMessageKeys.taskAdded
                    )
                ).collect { message ->
                    (message as? Toastable)?.let { toastable ->
                        snackbarHostState.showSnackbar(toastable.text)
                    }
                }
            }

            MaterialTheme {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    bottomBar = {
                        NavigationBar {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = {
                                        scope.launch {
                                            navigationRepository.selectTab(tab)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = getIconForTab(tab),
                                            contentDescription = tab.titleKey
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (currentTab) {
                            AppTab.TASKS -> TasksNavigationScreen(
                                navigationRepository = navigationRepository,
                                tasksRepository = tasksRepository,
                                remindersRepository = remindersRepository
                            )
                            AppTab.TEMPLATES -> TemplatesNavigationScreen(
                                navigationRepository = navigationRepository,
                                templatesRepository = templatesRepository,
                                tasksRepository = tasksRepository
                            )
                            AppTab.REMINDERS -> RemindersNavigationScreen(
                                navigationRepository = navigationRepository,
                                remindersRepository = remindersRepository
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getIconForTab(tab: AppTab): ImageVector {
        return when (tab) {
            AppTab.TASKS -> Icons.Default.List
            AppTab.TEMPLATES -> Icons.Default.DateRange
            AppTab.REMINDERS -> Icons.Default.Notifications
        }
    }
}

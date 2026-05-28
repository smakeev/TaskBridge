package com.taskbridge.android.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.MessagesRepository
import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.android.ui.screens.*
import com.taskbridge.android.ui.screens.reminders.RemindersRootScreen
import com.taskbridge.android.ui.screens.tasks.TaskDetailsScreen
import com.taskbridge.android.ui.screens.tasks.TasksRootScreen
import com.taskbridge.android.ui.screens.templates.TemplatesRootScreen
import com.taskbridge.android.ui.templates.TaskTemplatesViewModel
import com.taskbridge.core.models.navigation.NavigationDestination
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun TasksNavigationScreen(
    navigationRepository: NavigationRepository,
    tasksRepository: TasksRepository,
    remindersRepository: RemindersRepository,
    messagesRepository: MessagesRepository
) {
    val currentDestination by remember(navigationRepository) {
        navigationRepository.activePath
            .filter { path -> path?.root is NavigationDestination.TasksRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.TasksRoot)

    DestinationMapper(currentDestination, null, tasksRepository, remindersRepository, messagesRepository, navigationRepository)
}

@Composable
fun TemplatesNavigationScreen(
    navigationRepository: NavigationRepository,
    templatesRepository: TaskTemplatesRepository,
    tasksRepository: TasksRepository
) {
    val templatesViewModel: TaskTemplatesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TaskTemplatesViewModel(templatesRepository, tasksRepository) as T
            }
        }
    )
    val currentDestination by remember(navigationRepository) {
        navigationRepository.activePath
            .filter { path -> path?.root is NavigationDestination.TemplatesRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.TemplatesRoot)

    DestinationMapper(currentDestination, templatesViewModel, null, null, null, navigationRepository)
}

@Composable
fun RemindersNavigationScreen(
    navigationRepository: NavigationRepository,
    remindersRepository: RemindersRepository,
    messagesRepository: MessagesRepository
) {
    val currentDestination by remember(navigationRepository) {
        navigationRepository.activePath
            .filter { path -> path?.root is NavigationDestination.RemindersRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.RemindersRoot)

    DestinationMapper(currentDestination, null, null, remindersRepository, messagesRepository, navigationRepository)
}

@Composable
private fun DestinationMapper(
    destination: NavigationDestination?,
    templatesViewModel: TaskTemplatesViewModel?,
    tasksRepository: TasksRepository?,
    remindersRepository: RemindersRepository?,
    messagesRepository: MessagesRepository?,
    navigationRepository: NavigationRepository
) {
    when (destination) {
        is NavigationDestination.TasksRoot -> {
            if (tasksRepository != null && remindersRepository != null && messagesRepository != null) {
                TasksRootScreen(tasksRepository, remindersRepository, navigationRepository, messagesRepository)
            }
        }
        is NavigationDestination.TemplatesRoot -> {
            if (templatesViewModel != null) {
                TemplatesRootScreen(templatesViewModel)
            }
        }
        is NavigationDestination.RemindersRoot -> {
            if (remindersRepository != null && messagesRepository != null) {
                RemindersRootScreen(remindersRepository, navigationRepository, messagesRepository)
            }
        }
        is NavigationDestination.TaskDetails -> {
            if (tasksRepository != null && remindersRepository != null && messagesRepository != null) {
                TaskDetailsScreen(destination.taskId, tasksRepository, remindersRepository, navigationRepository, messagesRepository)
            }
        }
        is NavigationDestination.CreateTask -> CreateTaskScreen(destination.parentTaskId)
        is NavigationDestination.TemplateNameInput -> TemplateNameInputScreen(destination.templateId)
        is NavigationDestination.ReminderDetails -> ReminderDetailsScreen(destination.reminderId)
        null -> { /* Render nothing or a loading state */ }
    }
}

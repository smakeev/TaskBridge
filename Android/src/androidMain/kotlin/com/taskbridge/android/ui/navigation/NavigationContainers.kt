package com.taskbridge.android.ui.navigation

import androidx.compose.runtime.*
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.android.ui.screens.*
import com.taskbridge.android.ui.screens.reminders.RemindersRootScreen
import com.taskbridge.android.ui.screens.tasks.TaskDetailsScreen
import com.taskbridge.android.ui.screens.tasks.TasksRootScreen
import com.taskbridge.android.ui.screens.templates.TemplatesRootScreen
import com.taskbridge.core.models.navigation.NavigationDestination
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun TasksNavigationScreen(
    navigationRepository: NavigationRepository,
    tasksRepository: TasksRepository
) {
    val currentDestination by remember(navigationRepository) {
        navigationRepository.activePath
            .filter { path -> path?.root is NavigationDestination.TasksRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.TasksRoot)

    val scope = rememberCoroutineScope()
    DestinationMapper(currentDestination, null, tasksRepository, navigationRepository, scope)
}

@Composable
fun TemplatesNavigationScreen(
    navigationRepository: NavigationRepository,
    templatesRepository: TaskTemplatesRepository,
    tasksRepository: TasksRepository
) {
    val currentDestination by remember(navigationRepository) {
        navigationRepository.activePath
            .filter { path -> path?.root is NavigationDestination.TemplatesRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.TemplatesRoot)

    val scope = rememberCoroutineScope()
    DestinationMapper(currentDestination, templatesRepository, tasksRepository, navigationRepository, scope)
}

@Composable
fun RemindersNavigationScreen(repository: NavigationRepository) {
    val currentDestination by remember(repository) {
        repository.activePath
            .filter { path -> path?.root is NavigationDestination.RemindersRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.RemindersRoot)

    val scope = rememberCoroutineScope()
    DestinationMapper(currentDestination, null, null, repository, scope)
}

@Composable
private fun DestinationMapper(
    destination: NavigationDestination?,
    templatesRepository: TaskTemplatesRepository?,
    tasksRepository: TasksRepository?,
    navigationRepository: NavigationRepository,
    scope: kotlinx.coroutines.CoroutineScope
) {
    when (destination) {
        is NavigationDestination.TasksRoot -> {
            if (tasksRepository != null) {
                TasksRootScreen(tasksRepository, navigationRepository, scope)
            }
        }
        is NavigationDestination.TemplatesRoot -> {
            if (templatesRepository != null && tasksRepository != null) {
                TemplatesRootScreen(templatesRepository, tasksRepository, navigationRepository)
            }
        }
        is NavigationDestination.RemindersRoot -> RemindersRootScreen()
        is NavigationDestination.TaskDetails -> {
            if (tasksRepository != null) {
                TaskDetailsScreen(destination.taskId, tasksRepository, navigationRepository, scope)
            }
        }
        is NavigationDestination.CreateTask -> CreateTaskScreen(destination.parentTaskId)
        is NavigationDestination.TemplateNameInput -> TemplateNameInputScreen(destination.templateId)
        is NavigationDestination.ReminderDetails -> ReminderDetailsScreen(destination.reminderId)
        null -> { /* Render nothing or a loading state */ }
    }
}

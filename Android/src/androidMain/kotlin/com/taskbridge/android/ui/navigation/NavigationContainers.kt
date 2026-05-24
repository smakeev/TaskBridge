package com.taskbridge.android.ui.navigation

import androidx.compose.runtime.*
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.ui.screens.*
import com.taskbridge.core.models.navigation.NavigationDestination
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun TasksNavigationScreen(repository: NavigationRepository) {
    val currentDestination by remember(repository) {
        repository.activePath
            .filter { path -> path?.root is NavigationDestination.TasksRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.TasksRoot)

    DestinationMapper(currentDestination)
}

@Composable
fun TemplatesNavigationScreen(repository: NavigationRepository) {
    val currentDestination by remember(repository) {
        repository.activePath
            .filter { path -> path?.root is NavigationDestination.TemplatesRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.TemplatesRoot)

    DestinationMapper(currentDestination)
}

@Composable
fun RemindersNavigationScreen(repository: NavigationRepository) {
    val currentDestination by remember(repository) {
        repository.activePath
            .filter { path -> path?.root is NavigationDestination.RemindersRoot }
            .map { path -> path?.current }
    }.collectAsState(initial = NavigationDestination.RemindersRoot)

    DestinationMapper(currentDestination)
}

@Composable
private fun DestinationMapper(destination: NavigationDestination?) {
    when (destination) {
        is NavigationDestination.TasksRoot -> TasksRootScreen()
        is NavigationDestination.TemplatesRoot -> TemplatesRootScreen()
        is NavigationDestination.RemindersRoot -> RemindersRootScreen()
        is NavigationDestination.TaskDetails -> TaskDetailsScreen(destination.taskId)
        is NavigationDestination.CreateTask -> CreateTaskScreen(destination.parentTaskId)
        is NavigationDestination.TemplateNameInput -> TemplateNameInputScreen(destination.templateId)
        is NavigationDestination.ReminderDetails -> ReminderDetailsScreen(destination.reminderId)
        null -> { /* Render nothing or a loading state */ }
    }
}

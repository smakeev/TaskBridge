package com.taskbridge.android.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskbridge.android.ui.LocalRepositoriesStorage
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
fun TasksNavigationScreen() {
    NavigationScreen(
        rootDestination = NavigationDestination.TasksRoot,
        rootMatches = { it is NavigationDestination.TasksRoot }
    )
}

@Composable
fun TemplatesNavigationScreen() {
    NavigationScreen(
        rootDestination = NavigationDestination.TemplatesRoot,
        rootMatches = { it is NavigationDestination.TemplatesRoot }
    )
}

@Composable
fun RemindersNavigationScreen() {
    NavigationScreen(
        rootDestination = NavigationDestination.RemindersRoot,
        rootMatches = { it is NavigationDestination.RemindersRoot }
    )
}

@Composable
private fun NavigationScreen(
    rootDestination: NavigationDestination,
    rootMatches: (NavigationDestination?) -> Boolean
) {
    val repositoriesStorage = LocalRepositoriesStorage.current
    val navigationRepository = remember(repositoriesStorage) {
        repositoriesStorage.navigationRepository()
    }
    val currentDestination by remember(navigationRepository, rootDestination) {
        navigationRepository.activePath
            .filter { path -> rootMatches(path?.root) }
            .map { path -> path?.current }
    }.collectAsState(initial = rootDestination)

    DestinationMapper(currentDestination)
}

@Composable
private fun DestinationMapper(
    destination: NavigationDestination?
) {
    val repositoriesStorage = LocalRepositoriesStorage.current
    val navigationRepository = remember(repositoriesStorage) {
        repositoriesStorage.navigationRepository()
    }
    val tasksRepository = remember(repositoriesStorage) {
        repositoriesStorage.tasksRepository()
    }
    val remindersRepository = remember(repositoriesStorage) {
        repositoriesStorage.remindersRepository()
    }
    val messagesRepository = remember(repositoriesStorage) {
        repositoriesStorage.messagesRepository()
    }

    when (destination) {
        is NavigationDestination.TasksRoot ->
            TasksRootScreen(tasksRepository, remindersRepository, navigationRepository, messagesRepository)
        is NavigationDestination.TemplatesRoot -> TemplatesRootDestination()
        is NavigationDestination.RemindersRoot ->
            RemindersRootScreen(remindersRepository, navigationRepository, messagesRepository)
        is NavigationDestination.TaskDetails ->
            TaskDetailsScreen(destination.taskId, tasksRepository, remindersRepository, navigationRepository, messagesRepository)
        is NavigationDestination.CreateTask -> CreateTaskScreen(destination.parentTaskId)
        is NavigationDestination.TemplateNameInput -> TemplateNameInputScreen(destination.templateId)
        is NavigationDestination.ReminderDetails -> ReminderDetailsScreen(destination.reminderId)
        null -> { /* Render nothing or a loading state */ }
    }
}

@Composable
private fun TemplatesRootDestination() {
    val repositoriesStorage = LocalRepositoriesStorage.current
    val templatesRepository = remember(repositoriesStorage) {
        repositoriesStorage.taskTemplatesRepository()
    }
    val tasksRepository = remember(repositoriesStorage) {
        repositoriesStorage.tasksRepository()
    }
    val templatesViewModel: TaskTemplatesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TaskTemplatesViewModel(templatesRepository, tasksRepository) as T
            }
        }
    )

    TemplatesRootScreen(templatesViewModel)
}

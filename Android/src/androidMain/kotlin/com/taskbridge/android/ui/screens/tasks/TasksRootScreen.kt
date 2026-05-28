package com.taskbridge.android.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.MessagesRepository
import com.taskbridge.android.repository.NavigationDestinationMessageScopeId
import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.android.ui.tasks.TasksViewModel
import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.models.tasks.TaskItem
import kotlinx.coroutines.delay

@Composable
fun TasksRootScreen(
    tasksRepository: TasksRepository,
    remindersRepository: RemindersRepository,
    navigationRepository: NavigationRepository,
    messagesRepository: MessagesRepository
) {
    val viewModel: TasksViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TasksViewModel(
                    tasksRepository,
                    remindersRepository,
                    navigationRepository,
                    messagesRepository
                ) as T
            }
        }
    )
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToRename by remember { mutableStateOf<TaskItem?>(null) }
    var taskForReminder by remember { mutableStateOf<TaskItem?>(null) }
    var highlightedTaskId by remember { mutableStateOf<String?>(null) }
    val highlightAlpha = remember { Animatable(0f) }
    val listState = rememberLazyListState()
    val currentState by rememberUpdatedState(state)

    suspend fun scrollToAndBlink(taskId: String) {
        // The task may not be in the list yet when this runs from a cross-tab navigation
        // (the underlying StateFlow has WhileSubscribed(5000) and needs a moment to deliver
        // the latest snapshot to the freshly-mounted screen). Wait briefly for it.
        var waited = 0
        while (waited < 2000 && currentState.tasks.none { task -> task.id.value == taskId }) {
            delay(50)
            waited += 50
        }
        val index = currentState.tasks.indexOfFirst { task -> task.id.value == taskId }
        if (index < 0) return

        highlightedTaskId = taskId
        listState.animateScrollToItem(index + tasksListHeaderOffset(currentState))
        repeat(2) {
            highlightAlpha.animateTo(1f, animationSpec = tween(durationMillis = 1000))
            highlightAlpha.animateTo(0f, animationSpec = tween(durationMillis = 1000))
        }
        if (highlightedTaskId == taskId) {
            highlightedTaskId = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    LaunchedEffect(viewModel) {
        viewModel.observeTaskCreatedMessages().collect { message ->
            val taskAdded = message as? AppMessage.TaskAdded ?: return@collect
            if (taskAdded.parentPath.isNotEmpty()) return@collect
            val taskId = taskAdded.id.value
            scrollToAndBlink(taskId)
        }
    }

    LaunchedEffect(Unit) {
        val message = viewModel.consumeNavigationDestinationMessage(
            NavigationDestinationMessageScopeId.TasksRoot.scopeId
        )
        val taskId = when (message) {
            is NavigationDestinationMessage.ElementId -> message.value
            is NavigationDestinationMessage.TaskElement -> {
                if (message.parentPath.isNotEmpty()) return@LaunchedEffect
                message.taskId
            }
            null -> return@LaunchedEffect
        }
        scrollToAndBlink(taskId)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Tasks", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "Organize work into checklists, progress tasks, and containers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.isLoading && state.tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            state.errorMessage?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(14.dp)
                    )
                }
            }

            if (!state.isLoading && state.tasks.isEmpty()) {
                item {
                    EmptyTasksState()
                }
            }

            items(state.tasks, key = { it.id.value }) { task ->
                TaskTreeRows(
                    task = task,
                    depth = 0,
                    onOpenTask = viewModel::openTaskDetails,
                    onToggleCheckbox = viewModel::toggleCheckbox,
                    onProgressChanged = viewModel::updateProgress,
                    onAddReminder = { taskForReminder = it },
                    onRename = { taskToRename = it },
                    onDelete = { viewModel.deleteTaskTree(it.id) },
                    highlightedTaskId = highlightedTaskId,
                    highlightAlpha = highlightAlpha.value
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showCreateDialog) {
        TaskEditDialog(
            title = "New task",
            onDismiss = { showCreateDialog = false },
            onSave = { taskTitle, taskType ->
                viewModel.createTask(taskTitle, taskType)
                showCreateDialog = false
            }
        )
    }

    taskToRename?.let { task ->
        TaskRenameDialog(
            task = task,
            onDismiss = { taskToRename = null },
            onSave = { newTitle ->
                viewModel.renameTask(task, newTitle)
                taskToRename = null
            }
        )
    }

    taskForReminder?.let { task ->
        TaskReminderDialog(
            task = task,
            onDismiss = { taskForReminder = null },
            onSave = { title, body, type, minutesFromNow ->
                viewModel.createReminder(task, title, body, type, minutesFromNow)
                taskForReminder = null
            }
        )
    }
}

private fun tasksListHeaderOffset(state: com.taskbridge.core.usecases.tasks.TasksState): Int {
    var offset = 1
    if (state.isLoading && state.tasks.isEmpty()) offset += 1
    if (state.errorMessage != null) offset += 1
    if (!state.isLoading && state.tasks.isEmpty()) offset += 1
    return offset
}

@Composable
private fun EmptyTasksState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "No tasks yet", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Create a root task to start building your tree.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

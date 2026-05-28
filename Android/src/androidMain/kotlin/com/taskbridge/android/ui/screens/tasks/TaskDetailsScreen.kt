package com.taskbridge.android.ui.screens.tasks

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.taskbridge.core.models.tasks.TaskType
import kotlinx.coroutines.delay

@Composable
fun TaskDetailsScreen(
    taskId: String,
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
    val task = state.findTask(com.taskbridge.core.models.tasks.TaskId(taskId))
    val currentTask by rememberUpdatedState(task)
    var showSubtaskDialog by remember { mutableStateOf(false) }
    var taskToRename by remember { mutableStateOf<TaskItem?>(null) }
    var taskForReminder by remember { mutableStateOf<TaskItem?>(null) }
    var highlightedTaskId by remember { mutableStateOf<String?>(null) }
    val highlightAlpha = remember { Animatable(0f) }
    val listState = rememberLazyListState()

    suspend fun scrollToAndBlink(childTaskId: String) {
        // The subtask may not be in the parent's children yet when this fires from a
        // cross-tab navigation (the StateFlow is WhileSubscribed(5000) and needs a moment
        // to deliver the latest snapshot to the freshly-mounted screen). Wait briefly.
        var waited = 0
        while (waited < 2000 && currentTask?.children?.none { it.id.value == childTaskId } != false) {
            delay(50)
            waited += 50
        }
        val childIndex = currentTask?.children?.indexOfFirst { child -> child.id.value == childTaskId } ?: -1
        if (childIndex < 0) return

        highlightedTaskId = childTaskId
        listState.animateScrollToItem(childIndex + taskDetailsChildrenOffset)
        repeat(2) {
            highlightAlpha.animateTo(1f, animationSpec = tween(durationMillis = 1000))
            highlightAlpha.animateTo(0f, animationSpec = tween(durationMillis = 1000))
        }
        if (highlightedTaskId == childTaskId) {
            highlightedTaskId = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    LaunchedEffect(viewModel, taskId) {
        viewModel.observeTaskCreatedMessages().collect { message ->
            val taskAdded = message as? AppMessage.TaskAdded ?: return@collect
            if (taskAdded.parentPath.lastOrNull()?.value != taskId) return@collect
            scrollToAndBlink(taskAdded.id.value)
        }
    }

    LaunchedEffect(Unit) {
        val message = viewModel.consumeNavigationDestinationMessage(
            NavigationDestinationMessageScopeId.TaskDetails(taskId).scopeId
        )
        val taskMessage = message as? NavigationDestinationMessage.TaskElement ?: return@LaunchedEffect
        if (taskMessage.parentPath.lastOrNull() != taskId) return@LaunchedEffect
        scrollToAndBlink(taskMessage.taskId)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::popDestination) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(text = "Task details", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { paddingValues ->
        if (state.isLoading && task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Task not found")
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = task.title, style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    text = taskStatus(task),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            IconButton(onClick = { taskForReminder = task }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Add reminder")
                            }
                            IconButton(onClick = { taskToRename = task }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename")
                            }
                            IconButton(onClick = {
                                viewModel.deleteTaskTree(task.id)
                                viewModel.popDestination()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }

                        if (task.type == TaskType.CHECKBOX) {
                            Button(
                                onClick = { viewModel.toggleCheckbox(task) },
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text(if (task.isDone == true) "Mark unchecked" else "Mark checked")
                            }
                        }

                        if (task.type == TaskType.PROGRESS) {
                            Slider(
                                value = (task.progress?.value ?: 0).toFloat(),
                                onValueChange = { viewModel.updateProgress(task, it.toInt()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }

                if (task.type == TaskType.CONTAINER) {
                    item {
                        Button(onClick = { showSubtaskDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add subtask", modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    if (task.children.isEmpty()) {
                        item {
                            Text(
                                text = "No subtasks yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        task.children.forEach { child ->
                            item(key = child.id.value) {
                                TaskTreeRows(
                                    task = child,
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
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showSubtaskDialog && task != null) {
        TaskEditDialog(
            title = "New subtask",
            onDismiss = { showSubtaskDialog = false },
            onSave = { title, type ->
                viewModel.createSubtask(task, title, type)
                showSubtaskDialog = false
            }
        )
    }

    taskToRename?.let { renameTask ->
        TaskRenameDialog(
            task = renameTask,
            onDismiss = { taskToRename = null },
            onSave = { title ->
                viewModel.renameTask(renameTask, title)
                taskToRename = null
            }
        )
    }

    taskForReminder?.let { reminderTask ->
        TaskReminderDialog(
            task = reminderTask,
            onDismiss = { taskForReminder = null },
            onSave = { title, body, type, minutesFromNow ->
                viewModel.createReminder(reminderTask, title, body, type, minutesFromNow)
                taskForReminder = null
            }
        )
    }
}

private const val taskDetailsChildrenOffset = 2

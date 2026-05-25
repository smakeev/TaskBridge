package com.taskbridge.android.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.models.reminders.ReminderType
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.models.tasks.TaskProgress
import com.taskbridge.core.models.tasks.TaskType
import com.taskbridge.core.usecases.tasks.TasksState
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TasksRepository,
    private val remindersRepository: RemindersRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    val state: StateFlow<TasksState> = repository.tasksState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TasksState()
        )

    private var hasLoaded = false

    fun loadTasks() {
        if (hasLoaded) return
        hasLoaded = true
        viewModelScope.launch {
            runCatching { repository.loadTasks() }
        }
    }

    fun createTask(title: String, type: TaskType, parentId: TaskId? = null) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                repository.createTask(newTask(trimmedTitle, type, parentId))
            }
        }
    }

    fun createSubtask(parentTask: TaskItem, title: String, type: TaskType) {
        createTask(title = title, type = type, parentId = parentTask.id)
    }

    fun deleteTaskTree(taskId: TaskId) {
        viewModelScope.launch {
            runCatching { repository.deleteTaskTree(taskId) }
        }
    }

    fun toggleCheckbox(task: TaskItem) {
        if (task.type != TaskType.CHECKBOX) return
        viewModelScope.launch {
            runCatching {
                repository.replaceTask(task.copy(isDone = task.isDone != true))
            }
        }
    }

    fun updateProgress(task: TaskItem, progress: Int) {
        if (task.type != TaskType.PROGRESS) return
        viewModelScope.launch {
            runCatching {
                repository.replaceTask(
                    task.copy(progress = TaskProgress(progress.coerceIn(TaskProgress.MIN_VALUE, TaskProgress.MAX_VALUE)))
                )
            }
        }
    }

    fun renameTask(task: TaskItem, newTitle: String) {
        val trimmedTitle = newTitle.trim()
        if (trimmedTitle.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                repository.replaceTask(task.copy(title = trimmedTitle))
            }
        }
    }

    fun createReminder(task: TaskItem, title: String, body: String, type: ReminderType, minutesFromNow: Int) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        val now = System.currentTimeMillis()
        val safeMinutes = minutesFromNow.coerceAtLeast(1)
        val reminder = Reminder(
            id = ReminderId(UUID.randomUUID().toString()),
            title = trimmedTitle,
            body = body.trim().ifBlank { "Reminder for ${task.title}" },
            type = type,
            triggerAtMillis = now + safeMinutes * 60_000L,
            createdAtMillis = now
        )

        viewModelScope.launch {
            runCatching {
                remindersRepository.scheduleReminder(reminder)
                navigationRepository.pullToRoot(AppTab.REMINDERS)
                navigationRepository.selectTab(AppTab.REMINDERS)
            }
        }
    }

    private fun newTask(title: String, type: TaskType, parentId: TaskId?): TaskItem {
        return when (type) {
            TaskType.CHECKBOX -> TaskItem(
                id = TaskId(UUID.randomUUID().toString()),
                parentId = parentId,
                title = title,
                type = type,
                isDone = false,
                progress = null,
                children = emptyList()
            )
            TaskType.PROGRESS -> TaskItem(
                id = TaskId(UUID.randomUUID().toString()),
                parentId = parentId,
                title = title,
                type = type,
                isDone = null,
                progress = TaskProgress(0),
                children = emptyList()
            )
            TaskType.CONTAINER -> TaskItem(
                id = TaskId(UUID.randomUUID().toString()),
                parentId = parentId,
                title = title,
                type = type,
                isDone = null,
                progress = null,
                children = emptyList()
            )
        }
    }
}

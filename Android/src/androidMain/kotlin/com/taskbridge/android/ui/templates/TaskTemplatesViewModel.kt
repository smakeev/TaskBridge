package com.taskbridge.android.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.repository.TasksRepository
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.templates.TaskTemplatesState
import com.taskbridge.core.models.templates.TaskTemplate
import com.taskbridge.core.models.templates.TemplateTaskItem
import com.taskbridge.core.models.templates.TemplateTaskType
import com.taskbridge.core.models.tasks.TaskId
import com.taskbridge.core.models.tasks.TaskItem
import com.taskbridge.core.models.tasks.TaskProgress
import com.taskbridge.core.models.tasks.TaskType
import java.util.UUID
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskTemplatesViewModel(
    private val repository: TaskTemplatesRepository,
    private val tasksRepository: TasksRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    val state: StateFlow<TaskTemplatesState> = repository.templatesState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskTemplatesState()
        )

    fun loadTemplates() {
        viewModelScope.launch {
            try {
                repository.loadTemplates()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Throwable) {
            }
        }
    }

    fun refreshTemplates() {
        viewModelScope.launch {
            try {
                repository.loadTemplates()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Throwable) {
            }
        }
    }

    fun addTaskFromTemplate(template: TaskTemplate, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        viewModelScope.launch {
            runCatching {
                tasksRepository.createTask(template.rootTask.toTaskItem(parentId = null, titleOverride = trimmedTitle))
                navigationRepository.pullToRoot(AppTab.TASKS)
                navigationRepository.selectTab(AppTab.TASKS)
            }
        }
    }

    private fun TemplateTaskItem.toTaskItem(parentId: TaskId?, titleOverride: String? = null): TaskItem {
        val taskId = TaskId(UUID.randomUUID().toString())
        val taskTitle = titleOverride ?: title
        return when (type) {
            TemplateTaskType.CHECKBOX -> TaskItem(
                id = taskId,
                parentId = parentId,
                title = taskTitle,
                type = TaskType.CHECKBOX,
                isDone = false,
                progress = null,
                children = emptyList()
            )
            TemplateTaskType.PROGRESS -> TaskItem(
                id = taskId,
                parentId = parentId,
                title = taskTitle,
                type = TaskType.PROGRESS,
                isDone = null,
                progress = TaskProgress((initialProgress ?: 0).coerceIn(TaskProgress.MIN_VALUE, TaskProgress.MAX_VALUE)),
                children = emptyList()
            )
            TemplateTaskType.CONTAINER -> TaskItem(
                id = taskId,
                parentId = parentId,
                title = taskTitle,
                type = TaskType.CONTAINER,
                isDone = null,
                progress = null,
                children = children.orEmpty().map { it.toTaskItem(parentId = taskId) }
            )
        }
    }
}

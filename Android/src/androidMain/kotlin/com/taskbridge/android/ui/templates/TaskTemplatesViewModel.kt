package com.taskbridge.android.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.repository.impl.TaskTemplatesRepositoryImpl
import com.taskbridge.core.interactors.templates.TemplatesInteractor
import com.taskbridge.core.models.templates.TaskTemplatesState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskTemplatesViewModel(
    private val repository: TaskTemplatesRepository
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
}

package com.taskbridge.android.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.core.models.reminders.ReminderId
import com.taskbridge.core.usecases.reminders.RemindersState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RemindersViewModel(
    private val repository: RemindersRepository
) : ViewModel() {

    val state: StateFlow<RemindersState> = repository.remindersState

    private var hasLoaded = false

    fun loadReminders() {
        if (hasLoaded) return
        hasLoaded = true
        viewModelScope.launch {
            runCatching { repository.loadReminders() }
        }
    }

    fun cancelReminder(reminderId: ReminderId) {
        viewModelScope.launch {
            runCatching { repository.cancelReminder(reminderId) }
        }
    }
}

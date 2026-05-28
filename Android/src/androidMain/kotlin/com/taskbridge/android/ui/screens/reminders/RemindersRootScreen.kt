package com.taskbridge.android.ui.screens.reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskbridge.android.repository.MessagesRepository
import com.taskbridge.android.repository.NavigationDestinationMessageScopeId
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.RemindersRepository
import com.taskbridge.android.ui.components.rememberScrollBlinkHighlighter
import com.taskbridge.android.ui.reminders.RemindersViewModel
import com.taskbridge.core.models.messages.AppMessage
import com.taskbridge.core.models.navigation.NavigationDestinationMessage
import com.taskbridge.core.models.reminders.Reminder
import com.taskbridge.core.models.reminders.ReminderType
import java.text.DateFormat
import java.util.Date

@Composable
fun RemindersRootScreen(
    remindersRepository: RemindersRepository,
    navigationRepository: NavigationRepository,
    messagesRepository: MessagesRepository
) {
    val viewModel: RemindersViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RemindersViewModel(
                    remindersRepository,
                    navigationRepository,
                    messagesRepository
                ) as T
            }
        }
    )
    val state by viewModel.state.collectAsState()
    val highlighter = rememberScrollBlinkHighlighter()
    val listState = rememberLazyListState()
    val currentState by rememberUpdatedState(state)

    suspend fun scrollToAndBlink(reminderId: String) {
        highlighter.scrollToAndBlink(
            id = reminderId,
            listState = listState,
            findItemIndex = {
                currentState.reminders.indexOfFirst { reminder -> reminder.id.value == reminderId }
                    .takeIf { index -> index >= 0 }
                    ?.let { index -> index + remindersListHeaderOffset(currentState) }
                    ?: -1
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadReminders()
    }

    LaunchedEffect(viewModel) {
        viewModel.observeReminderCreatedMessages().collect { message ->
            val reminderId = (message as? AppMessage.ReminderCreated)?.id?.value ?: return@collect
            scrollToAndBlink(reminderId)
        }
    }

    LaunchedEffect(Unit) {
        val message = viewModel.consumeNavigationDestinationMessage(
            NavigationDestinationMessageScopeId.RemindersRoot.scopeId
        )
        val reminderId = (message as? NavigationDestinationMessage.ElementId)?.value ?: return@LaunchedEffect
        scrollToAndBlink(reminderId)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Reminders", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Add reminders from the Tasks tab. A task can have multiple reminders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.isLoading && state.reminders.isEmpty()) {
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
                        .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                        .padding(12.dp)
                )
            }
        }

        if (!state.isLoading && state.reminders.isEmpty()) {
            item {
                EmptyRemindersState()
            }
        }

        items(state.reminders, key = { it.id.value }) { reminder ->
            ReminderCard(
                reminder = reminder,
                onDelete = { viewModel.cancelReminder(reminder.id) },
                isHighlighted = highlighter.highlightedId == reminder.id.value,
                highlightAlpha = highlighter.alpha.value
            )
        }
    }
}

private fun remindersListHeaderOffset(state: com.taskbridge.core.usecases.reminders.RemindersState): Int {
    var offset = 1
    if (state.isLoading && state.reminders.isEmpty()) offset += 1
    if (state.errorMessage != null) offset += 1
    if (!state.isLoading && state.reminders.isEmpty()) offset += 1
    return offset
}

@Composable
private fun EmptyRemindersState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text("No reminders yet", style = MaterialTheme.typography.titleMedium)
        Text(
            "Open a task and use the reminder button to add one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onDelete: () -> Unit,
    isHighlighted: Boolean,
    highlightAlpha: Float
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                androidx.compose.ui.graphics.lerp(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f * highlightAlpha),
                    highlightAlpha
                )
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isHighlighted) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f * highlightAlpha))
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (reminder.body.isNotBlank()) {
                    Text(reminder.body, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "${reminder.type.label()} • ${formatReminderTime(reminder.triggerAtMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove reminder")
            }
        }
    }
}

private fun ReminderType.label(): String = when (this) {
    ReminderType.START -> "Start"
    ReminderType.DEADLINE -> "Deadline"
}

private fun formatReminderTime(triggerAtMillis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(triggerAtMillis))
}

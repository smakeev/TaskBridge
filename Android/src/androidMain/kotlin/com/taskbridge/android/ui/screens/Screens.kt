package com.taskbridge.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.ui.templates.TaskTemplatesViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun TasksRootScreen() {
    ScreenPlaceholder("Tasks Root")
}

@Composable
fun TemplatesRootScreen(repository: TaskTemplatesRepository) {
    val viewModel: TaskTemplatesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskTemplatesViewModel(repository) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Templates", modifier = Modifier.padding(bottom = 16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        state.errorMessage?.let { error ->
            Text(text = error, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = { viewModel.refreshTemplates() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Refresh")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.templates) { template ->
                Text(
                    text = template.title,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun RemindersRootScreen() {
    ScreenPlaceholder("Reminders Root")
}

@Composable
fun TaskDetailsScreen(taskId: String) {
    ScreenPlaceholder("Task Details", "ID: $taskId")
}

@Composable
fun CreateTaskScreen(parentTaskId: String?) {
    ScreenPlaceholder("Create Task", "Parent ID: ${parentTaskId ?: "None"}")
}

@Composable
fun TemplateNameInputScreen(templateId: String) {
    ScreenPlaceholder("Template Name Input", "ID: $templateId")
}

@Composable
fun ReminderDetailsScreen(reminderId: String) {
    ScreenPlaceholder("Reminder Details", "ID: $reminderId")
}

@Composable
private fun ScreenPlaceholder(title: String, subtitle: String? = null) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = subtitle)
            }
        }
    }
}

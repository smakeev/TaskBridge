package com.taskbridge.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taskbridge.core.models.navigation.NavigationDestination

@Composable
fun TasksRootScreen() {
    ScreenPlaceholder("Tasks Root")
}

@Composable
fun TemplatesRootScreen() {
    ScreenPlaceholder("Templates Root")
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

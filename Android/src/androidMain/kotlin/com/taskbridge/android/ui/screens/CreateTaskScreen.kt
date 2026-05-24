package com.taskbridge.android.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun CreateTaskScreen(parentTaskId: String?) {
    ScreenPlaceholder("Create Task", "Parent ID: ${parentTaskId ?: "None"}")
}

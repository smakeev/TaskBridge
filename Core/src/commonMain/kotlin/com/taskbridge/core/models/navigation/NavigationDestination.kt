package com.taskbridge.core.models.navigation

sealed interface NavigationDestination {

    data object TasksRoot : NavigationDestination

    data object TemplatesRoot : NavigationDestination

    data object RemindersRoot : NavigationDestination

    data class TaskDetails(
        val taskId: String
    ) : NavigationDestination

    data class CreateTask(
        val parentTaskId: String?
    ) : NavigationDestination

    data class TemplateNameInput(
        val templateId: String
    ) : NavigationDestination

    data class ReminderDetails(
        val reminderId: String
    ) : NavigationDestination

}

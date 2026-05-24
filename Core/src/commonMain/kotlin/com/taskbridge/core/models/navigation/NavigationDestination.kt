package com.taskbridge.core.models.navigation

public sealed interface NavigationDestination {

    data object TasksRoot : NavigationDestination

    data object TemplatesRoot : NavigationDestination

    data object RemindersRoot : NavigationDestination

    public data class TaskDetails(
        val taskId: String
    ) : NavigationDestination

    public data class CreateTask(
        val parentTaskId: String?
    ) : NavigationDestination

    public data class TemplateNameInput(
        val templateId: String
    ) : NavigationDestination

    public data class ReminderDetails(
        val reminderId: String
    ) : NavigationDestination

}

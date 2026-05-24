package com.taskbridge.core.models.navigation

public enum class AppTab(
    val titleKey: String,
    val iconKey: String
) {
    TASKS(
        titleKey = "tab_tasks",
        iconKey = "checklist"
    ),
    TEMPLATES(
        titleKey = "tab_templates",
        iconKey = "library_books"
    ),
    REMINDERS(
        titleKey = "tab_reminders",
        iconKey = "notifications"
    );

    companion object {
        val allCases: List<AppTab> = entries
    }
}

package com.taskbridge.android.utils

import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.models.navigation.NavigationDestination

public suspend fun NavigationRepository.isCurrentRoot(tab: AppTab): Boolean {
    if (fetchCurrentTab() != tab) return false
    val current = fetchActivePath()?.current ?: return false
    return when (tab) {
        AppTab.TASKS -> current is NavigationDestination.TasksRoot
        AppTab.REMINDERS -> current is NavigationDestination.RemindersRoot
        AppTab.TEMPLATES -> current is NavigationDestination.TemplatesRoot
    }
}

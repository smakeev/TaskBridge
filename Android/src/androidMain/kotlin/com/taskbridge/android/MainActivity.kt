package com.taskbridge.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.taskbridge.android.repository.NavigationRepository
import com.taskbridge.android.repository.TaskTemplatesRepository
import com.taskbridge.android.repository.impl.NavigationRepositoryImpl
import com.taskbridge.android.repository.impl.TaskTemplatesRepositoryImpl
import com.taskbridge.android.ui.navigation.RemindersNavigationScreen
import com.taskbridge.android.ui.navigation.TasksNavigationScreen
import com.taskbridge.android.ui.navigation.TemplatesNavigationScreen
import com.taskbridge.core.TaskBridge
import com.taskbridge.core.models.navigation.AppTab
import com.taskbridge.core.storage.tasks.PlatformDependencies
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val platformDependencies = PlatformDependencies(this)
        val taskBridge = TaskBridge(platformDependencies)
        val navigationInteractor = taskBridge.navigationInteractor()
        val templatesInteractor = taskBridge.templatesInteractor()
        
        val navigationRepository: NavigationRepository = NavigationRepositoryImpl(navigationInteractor)
        val templatesRepository: TaskTemplatesRepository = TaskTemplatesRepositoryImpl(templatesInteractor)

        setContent {
            val scope = rememberCoroutineScope()
            val currentTab by navigationRepository.currentTab.collectAsState(initial = AppTab.TASKS)

            MaterialTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = {
                                        scope.launch {
                                            navigationRepository.selectTab(tab)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = getIconForTab(tab),
                                            contentDescription = tab.titleKey
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (currentTab) {
                            AppTab.TASKS -> TasksNavigationScreen(navigationRepository)
                            AppTab.TEMPLATES -> TemplatesNavigationScreen(
                                navigationRepository = navigationRepository,
                                templatesRepository = templatesRepository
                            )
                            AppTab.REMINDERS -> RemindersNavigationScreen(navigationRepository)
                        }
                    }
                }
            }
        }
    }

    private fun getIconForTab(tab: AppTab): ImageVector {
        return when (tab) {
            AppTab.TASKS -> Icons.Default.List
            AppTab.TEMPLATES -> Icons.Default.DateRange
            AppTab.REMINDERS -> Icons.Default.Notifications
        }
    }
}

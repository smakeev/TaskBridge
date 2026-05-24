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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.taskbridge.android.repository.impl.NavigationRepositoryImpl
import com.taskbridge.core.TaskBridge
import com.taskbridge.core.models.navigation.AppTab
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val taskBridge = TaskBridge()
        val navigationInteractor = taskBridge.navigationInteractor()
        val navigationRepository = NavigationRepositoryImpl(navigationInteractor)

        setContent {
            val scope = rememberCoroutineScope()
            var currentTab by remember { mutableStateOf(AppTab.TASKS) }

            MaterialTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = {
                                        currentTab = tab
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
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Current Tab: ${currentTab.name}")
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

package com.taskbridge.android.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.taskbridge.android.repository.RepositoriesStorage

val LocalRepositoriesStorage = staticCompositionLocalOf<RepositoriesStorage> {
    error("RepositoriesStorage is not provided")
}

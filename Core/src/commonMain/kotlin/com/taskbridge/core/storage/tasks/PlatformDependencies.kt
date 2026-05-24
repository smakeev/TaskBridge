package com.taskbridge.core.storage.tasks

import androidx.room.RoomDatabase

internal expect fun getDatabaseBuilder(platformDependencies: PlatformDependencies): RoomDatabase.Builder<TaskDatabase>

/**
 * Marker interface or container for platform-specific dependencies (e.g., Context on Android).
 */
public expect class PlatformDependencies

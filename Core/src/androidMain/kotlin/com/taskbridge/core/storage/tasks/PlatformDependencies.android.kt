package com.taskbridge.core.storage.tasks

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

public actual class PlatformDependencies(
    public val context: Context
)

internal actual fun getDatabaseBuilder(platformDependencies: PlatformDependencies): RoomDatabase.Builder<TaskDatabase> {
    val appContext = platformDependencies.context.applicationContext
    val dbFile = appContext.getDatabasePath("task_bridge.db")
    return Room.databaseBuilder<TaskDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

package com.taskbridge.core.storage.tasks

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

public actual class PlatformDependencies

@OptIn(ExperimentalForeignApi::class)
internal actual fun getDatabaseBuilder(platformDependencies: PlatformDependencies): RoomDatabase.Builder<TaskDatabase> {
    val dbFilePath = documentDirectory() + "/task_bridge.db"
    return Room.databaseBuilder<TaskDatabase>(name = dbFilePath)
        .setDriver(BundledSQLiteDriver())
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return documentDirectory?.path ?: ""
}

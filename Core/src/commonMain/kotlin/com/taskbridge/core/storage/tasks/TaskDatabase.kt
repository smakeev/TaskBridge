package com.taskbridge.core.storage.tasks

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
internal abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

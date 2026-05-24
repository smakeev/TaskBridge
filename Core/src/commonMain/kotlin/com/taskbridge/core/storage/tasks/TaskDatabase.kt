package com.taskbridge.core.storage.tasks

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
@ConstructedBy(TaskDatabaseConstructor::class)
internal abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

@Suppress("KotlinNoActualForExpect")
internal expect object TaskDatabaseConstructor : RoomDatabaseConstructor<TaskDatabase> {
    override fun initialize(): TaskDatabase
}

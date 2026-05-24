package com.taskbridge.core.storage.tasks

import com.taskbridge.core.models.tasks.*
import kotlinx.datetime.Clock

/**
 * Mapper for converting between [TaskItem] domain models and [TaskEntity] persistence entities.
 * Handles flattening of the task tree and its reconstruction.
 */
internal object TaskEntityMapper {

    /**
     * Recursively flattens a [TaskItem] tree into a list of [TaskEntity].
     */
    fun TaskItem.toEntities(
        now: Long = Clock.System.now().toEpochMilliseconds()
    ): List<TaskEntity> {
        val result = mutableListOf<TaskEntity>()

        fun flatten(item: TaskItem, index: Int) {
            // TODO: In production, createdAt/updatedAt should be managed by the storage/service layer
            result.add(
                TaskEntity(
                    id = item.id.value,
                    parentId = item.parentId?.value,
                    title = item.title,
                    type = item.type.name,
                    isDone = item.isDone,
                    progress = item.progress?.value,
                    sortOrder = index,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
            )
            item.children.forEachIndexed { childIndex, child ->
                flatten(child, childIndex)
            }
        }

        flatten(this, 0)
        return result
    }

    /**
     * Reconstructs a recursive [TaskItem] tree from a flat list of [TaskEntity].
     */
    fun List<TaskEntity>.toTaskTree(): List<TaskItem> {
        val groupedByParent = this.groupBy { it.parentId }

        fun buildTask(entity: TaskEntity): TaskItem {
            val childrenEntities = groupedByParent[entity.id]
                ?.sortedBy { it.sortOrder }
                .orEmpty()
            
            val children = childrenEntities.map { buildTask(it) }

            val type = try {
                TaskType.valueOf(entity.type)
            } catch (e: Exception) {
                // TODO: Safe fallback or skip invalid entities in production
                TaskType.CHECKBOX
            }

            // Apply domain reconstruction rules
            return when (type) {
                TaskType.CHECKBOX -> TaskItem(
                    id = TaskId(entity.id),
                    parentId = entity.parentId?.let { TaskId(it) },
                    title = entity.title,
                    type = type,
                    isDone = entity.isDone ?: false,
                    progress = null,
                    children = emptyList()
                )
                TaskType.PROGRESS -> TaskItem(
                    id = TaskId(entity.id),
                    parentId = entity.parentId?.let { TaskId(it) },
                    title = entity.title,
                    type = type,
                    isDone = null,
                    progress = entity.progress?.let { TaskProgress(it) } ?: TaskProgress(0),
                    children = emptyList()
                )
                TaskType.CONTAINER -> TaskItem(
                    id = TaskId(entity.id),
                    parentId = entity.parentId?.let { TaskId(it) },
                    title = entity.title,
                    type = type,
                    isDone = null,
                    progress = null,
                    children = children
                )
            }
        }

        return (groupedByParent[null] ?: emptyList())
            .sortedBy { it.sortOrder }
            .map { buildTask(it) }
    }
}

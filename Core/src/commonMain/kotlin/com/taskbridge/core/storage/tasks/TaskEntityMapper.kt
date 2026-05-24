package com.taskbridge.core.storage.tasks

import com.taskbridge.core.models.tasks.*
import kotlinx.datetime.Clock

/**
 * Recursively flattens a [TaskItem] tree into a list of [TaskEntity].
 * This function treats the receiver as the root of the tree (parentId = null).
 * The tree structure defines the parent-child relationships in the resulting entities.
 */
internal fun TaskItem.toEntities(
    now: Long = Clock.System.now().toEpochMilliseconds()
): List<TaskEntity> {
    val result = mutableListOf<TaskEntity>()

    fun flatten(item: TaskItem, parentId: String?, index: Int) {
        // TODO: In production, createdAt/updatedAt should be managed by the storage/service layer
        result.add(
            TaskEntity(
                id = item.id.value,
                parentId = parentId,
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
            flatten(child, item.id.value, childIndex)
        }
    }

    flatten(this, null, 0)
    return result
}

/**
 * Reconstructs a recursive [TaskItem] tree from a flat list of [TaskEntity].
 */
internal fun List<TaskEntity>.toTaskTree(): List<TaskItem> {
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

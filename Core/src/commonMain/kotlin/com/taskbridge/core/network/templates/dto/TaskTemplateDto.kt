package com.taskbridge.core.network.templates.dto

import com.taskbridge.core.models.templates.TaskTemplate
import com.taskbridge.core.models.templates.TemplateId
import com.taskbridge.core.models.templates.TemplateTaskItem
import com.taskbridge.core.models.templates.TemplateTaskType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskTemplateDto(
    val id: String,
    val title: String,
    val description: String,
    val rootTask: TemplateTaskItemDto
)

@Serializable
data class TemplateTaskItemDto(
    val id: String,
    val title: String,
    val type: TemplateTaskTypeDto,
    val initialProgress: Int? = null,
    val children: List<TemplateTaskItemDto>? = null
)

@Serializable
enum class TemplateTaskTypeDto {
    @SerialName("checkbox")
    CHECKBOX,

    @SerialName("progress")
    PROGRESS,

    @SerialName("container")
    CONTAINER
}

fun TaskTemplateDto.toDomain(): TaskTemplate {
    return TaskTemplate(
        id = TemplateId(id),
        title = title,
        description = description,
        rootTask = rootTask.toDomain()
    )
}

fun TemplateTaskItemDto.toDomain(): TemplateTaskItem {
    return TemplateTaskItem(
        id = id,
        title = title,
        type = type.toDomain(),
        initialProgress = when (type) {
            TemplateTaskTypeDto.PROGRESS -> initialProgress
            TemplateTaskTypeDto.CHECKBOX,
            TemplateTaskTypeDto.CONTAINER -> null
        },
        children = when (type) {
            TemplateTaskTypeDto.CONTAINER -> children.orEmpty().map { it.toDomain() }
            TemplateTaskTypeDto.CHECKBOX,
            TemplateTaskTypeDto.PROGRESS -> null
        }
    )
}

private fun TemplateTaskTypeDto.toDomain(): TemplateTaskType {
    return when (this) {
        TemplateTaskTypeDto.CHECKBOX -> TemplateTaskType.CHECKBOX
        TemplateTaskTypeDto.PROGRESS -> TemplateTaskType.PROGRESS
        TemplateTaskTypeDto.CONTAINER -> TemplateTaskType.CONTAINER
    }
}

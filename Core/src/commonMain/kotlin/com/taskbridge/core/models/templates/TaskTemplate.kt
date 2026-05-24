package com.taskbridge.core.models.templates

public data class TaskTemplate(
    val id: TemplateId,
    val title: String,
    val description: String,
    val rootTask: TemplateTaskItem
)

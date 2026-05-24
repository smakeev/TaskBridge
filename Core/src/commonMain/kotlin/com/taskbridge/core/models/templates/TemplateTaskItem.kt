package com.taskbridge.core.models.templates

data class TemplateTaskItem(
    val id: String,
    val title: String,
    val type: TemplateTaskType,
    val initialProgress: Int?,
    val children: List<TemplateTaskItem>?
)

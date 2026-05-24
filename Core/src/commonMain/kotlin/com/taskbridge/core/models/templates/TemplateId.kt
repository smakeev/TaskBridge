package com.taskbridge.core.models.templates

import kotlin.jvm.JvmInline

/**
 * Data class representing a unique identifier for a Template.
 * Using data class instead of value class for better Swift interoperability.
 */
public data class TemplateId(
    val value: String
)

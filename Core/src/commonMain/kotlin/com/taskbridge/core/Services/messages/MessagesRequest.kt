package com.taskbridge.core.services.messages

import com.taskbridge.core.messages.internal.CoreMessage
import com.taskbridge.core.services.common.ServiceRequest

internal sealed interface MessagesRequest : ServiceRequest {
    data class Publish(
        val message: CoreMessage
    ) : MessagesRequest
}

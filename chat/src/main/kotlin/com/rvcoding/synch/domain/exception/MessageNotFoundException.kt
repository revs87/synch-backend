package com.rvcoding.synch.domain.exception

import com.rvcoding.synch.domain.type.ChatMessageId

class MessageNotFoundException(
    private val id: ChatMessageId
) : RuntimeException(
    "Message with ID $id not found"
)
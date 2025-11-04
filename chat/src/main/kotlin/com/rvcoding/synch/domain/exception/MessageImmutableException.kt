package com.rvcoding.synch.domain.exception

import com.rvcoding.synch.domain.type.ChatMessageId

class MessageImmutableException(
    private val id: ChatMessageId
) : RuntimeException(
    "Message with ID $id cannot be changed."
)
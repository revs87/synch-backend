package com.rvcoding.synch.domain.exception

import com.rvcoding.synch.domain.type.ChatMessageId

class MessageNotUpdatableException(
    private val id: ChatMessageId
) : RuntimeException(
    "Message with ID $id can not be updated"
)
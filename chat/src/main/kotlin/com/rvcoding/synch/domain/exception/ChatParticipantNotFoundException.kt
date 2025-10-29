package com.rvcoding.synch.domain.exception

import com.rvcoding.synch.domain.type.UserId

class ChatParticipantNotFoundException(
    private val id: UserId
) : RuntimeException(
    "The chat participant with the ID $id was not found."
)
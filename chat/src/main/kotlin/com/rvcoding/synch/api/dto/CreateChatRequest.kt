package com.rvcoding.synch.api.dto

import com.rvcoding.synch.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest(
    @field:Size(
        min = 1,
        message = "Chats must have at least 2 unique participants"
    )
    val otherUserIds: List<UserId>
)
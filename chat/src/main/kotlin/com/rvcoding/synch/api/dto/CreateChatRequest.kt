package com.rvcoding.synch.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.rvcoding.synch.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest @JsonCreator constructor(
    @field:Size(
        min = 1,
        message = "Chats must have at least 2 unique participants"
    )
    @JsonProperty("otherUserIds")
    val otherUserIds: List<UserId>
)
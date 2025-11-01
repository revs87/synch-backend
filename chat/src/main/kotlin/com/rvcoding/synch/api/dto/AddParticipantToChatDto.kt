package com.rvcoding.synch.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.rvcoding.synch.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatDto @JsonCreator constructor(
    @field:Size(min = 1)
    @JsonProperty("userIds")
    val userIds: List<UserId>
)

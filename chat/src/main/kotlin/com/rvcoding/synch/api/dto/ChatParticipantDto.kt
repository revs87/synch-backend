package com.rvcoding.synch.api.dto

import com.rvcoding.synch.domain.type.UserId


data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
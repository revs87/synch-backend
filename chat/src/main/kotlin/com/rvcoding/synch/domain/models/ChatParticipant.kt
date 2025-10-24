package com.rvcoding.synch.domain.models

import com.rvcoding.synch.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
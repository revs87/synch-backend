package com.rvcoding.synch.domain.event

import com.rvcoding.synch.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?
)

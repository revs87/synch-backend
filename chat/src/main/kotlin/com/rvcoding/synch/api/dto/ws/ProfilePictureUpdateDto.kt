package com.rvcoding.synch.api.dto.ws

import com.rvcoding.synch.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?
)
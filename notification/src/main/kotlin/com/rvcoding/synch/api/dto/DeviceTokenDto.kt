package com.rvcoding.synch.api.dto

import com.rvcoding.synch.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)
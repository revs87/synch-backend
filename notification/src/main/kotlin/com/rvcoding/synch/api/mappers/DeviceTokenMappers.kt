package com.rvcoding.synch.api.mappers

import com.rvcoding.synch.api.dto.DeviceTokenDto
import com.rvcoding.synch.api.dto.PlatformDto
import com.rvcoding.synch.domain.model.DeviceToken

fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto {
    return DeviceTokenDto(
        userId = userId,
        token = token,
        createdAt = createdAt
    )
}

fun PlatformDto.toPlatformDto(): DeviceToken.Platform {
    return when (this) {
        PlatformDto.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformDto.IOS -> DeviceToken.Platform.IOS
    }
}
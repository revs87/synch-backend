package com.rvcoding.synch.infra.mappers

import com.rvcoding.synch.domain.model.DeviceToken
import com.rvcoding.synch.infra.database.DeviceTokenEntity


fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        userId = userId,
        token = token,
        platform = platform.toPlatform(),
        createdAt = createdAt,
        id = id
    )
}
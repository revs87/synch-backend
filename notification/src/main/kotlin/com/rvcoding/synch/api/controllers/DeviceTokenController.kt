package com.rvcoding.synch.api.controllers

import com.rvcoding.synch.api.dto.DeviceTokenDto
import com.rvcoding.synch.api.dto.RegisterDeviceRequest
import com.rvcoding.synch.api.mappers.toDeviceTokenDto
import com.rvcoding.synch.api.mappers.toPlatformDto
import com.rvcoding.synch.api.util.requestUserId
import com.rvcoding.synch.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(private val pushNotificationService: PushNotificationService) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatformDto()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @PathVariable("token") token: String
    ) {
        pushNotificationService.unregisterDevice(token)
    }
}
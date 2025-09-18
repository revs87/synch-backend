package com.rvcoding.synch.api.mappers

import com.rvcoding.synch.api.dto.AuthenticatedUserDto
import com.rvcoding.synch.api.dto.UserDto
import com.rvcoding.synch.domain.model.AuthenticatedUser
import com.rvcoding.synch.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasEmailVerified
    )
}
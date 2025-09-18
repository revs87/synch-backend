package com.rvcoding.synch.infra.database.mappers

import com.rvcoding.synch.domain.model.User
import com.rvcoding.synch.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasEmailVerified = hasVerifiedEmail
    )
}
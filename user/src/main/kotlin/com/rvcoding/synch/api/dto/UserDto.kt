package com.rvcoding.synch.api.dto

import com.rvcoding.synch.domain.model.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean
)
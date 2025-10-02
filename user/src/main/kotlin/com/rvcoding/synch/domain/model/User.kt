package com.rvcoding.synch.domain.model

import com.rvcoding.synch.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean
)
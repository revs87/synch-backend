package com.rvcoding.synch.infra.database.mappers

import com.rvcoding.synch.domain.model.EmailVerificationToken
import com.rvcoding.synch.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}
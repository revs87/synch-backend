package com.rvcoding.synch.infra.security

import com.rvcoding.synch.domain.model.PasswordHash
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component


@Component
class PasswordEncoder {
    private val bcrypt = BCryptPasswordEncoder()

    fun encode(rawPassword: String): PasswordHash = when (val res = bcrypt.encode(rawPassword)) {
        null -> PasswordHash.Null
        else -> PasswordHash.Encoded(hash = res)
    }

    fun matches(rawPassword: String, hashedPassword: String): Boolean {
        return bcrypt.matches(rawPassword, hashedPassword)
    }
}
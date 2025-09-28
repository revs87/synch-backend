package com.rvcoding.synch.domain.exception

class InvalidTokenException(
    override val message: String?
): RuntimeException(message ?: "Invalid token.")
package com.rvcoding.synch.api.utils

import com.rvcoding.synch.domain.exception.UnauthorizedException
import com.rvcoding.synch.domain.model.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()
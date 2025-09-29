package com.rvcoding.synch.domain.exception

class RateLimitException(
    val resetsInSeconds: Long
) : RuntimeException(
    "Rate limit exceeded. Please try again in $resetsInSeconds seconds."
)

package com.rvcoding.synch.api.config

import java.util.concurrent.TimeUnit

annotation class IpRateLimit(
    val requests: Int = 60,
    val duration: Long = 1L,
    val unit: TimeUnit = TimeUnit.MINUTES,
    val endpoint: String = ""
)

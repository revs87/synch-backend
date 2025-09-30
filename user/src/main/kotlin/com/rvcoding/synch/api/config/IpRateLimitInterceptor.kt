package com.rvcoding.synch.api.config

import com.rvcoding.synch.domain.exception.RateLimitException
import com.rvcoding.synch.infra.api.rate_limiting.IpRateLimiter
import com.rvcoding.synch.infra.api.rate_limiting.IpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class IpRateLimitInterceptor(
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${synch.rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IpRateLimit::class.java)
            if (annotation != null) {
                val ipAddress = ipResolver.getClientIp(request)

                return try {
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = ipAddress,
                        resetsIn = Duration.of(
                            annotation.duration,
                            annotation.unit.toChronoUnit()
                        ),
                        maxRequestsPerIp = annotation.requests,
                        action = { true }
                    )
                } catch (e: RateLimitException) {
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
                    false
                }
            }
        }
        return true
    }
}
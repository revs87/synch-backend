package com.rvcoding.synch.api.controller

import com.rvcoding.synch.api.config.IpRateLimit
import com.rvcoding.synch.api.dto.AuthenticatedUserDto
import com.rvcoding.synch.api.dto.ChangePasswordRequest
import com.rvcoding.synch.api.dto.EmailRequest
import com.rvcoding.synch.api.dto.LoginRequest
import com.rvcoding.synch.api.dto.RefreshRequest
import com.rvcoding.synch.api.dto.RegisterRequest
import com.rvcoding.synch.api.dto.ResetPasswordRequest
import com.rvcoding.synch.api.dto.UserDto
import com.rvcoding.synch.api.mappers.toAuthenticatedUserDto
import com.rvcoding.synch.api.mappers.toUserDto
import com.rvcoding.synch.api.utils.requestUserId
import com.rvcoding.synch.infra.api.rate_limiting.EmailRateLimiter
import com.rvcoding.synch.service.auth.AuthService
import com.rvcoding.synch.service.auth.EmailVerificationService
import com.rvcoding.synch.service.auth.PasswordResetService
import jakarta.validation.Valid
import java.util.concurrent.TimeUnit
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter
) {

    @PostMapping("/register")
    @IpRateLimit(
        requests = 5,
        duration = 1L,
        unit = TimeUnit.HOURS,
        endpoint = "/auth"
    )
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }

    @PostMapping("/login")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
        endpoint = "/auth"
    )
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password,
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
        endpoint = "/auth/refresh"
    )
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService
            .refresh(body.refreshToken)
            .toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody body: RefreshRequest
    ) {
        authService.logout(body.refreshToken)
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/resend-verification")
    @IpRateLimit(
        requests = 5,
        duration = 1L,
        unit = TimeUnit.HOURS,
        endpoint = "/auth/resend-verification"
    )
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest
    ) {
        emailRateLimiter.withRateLimit(
            email = body.email
        ) {
            emailVerificationService.resendVerificationToken(body.email)
        }
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        requests = 5,
        duration = 1L,
        unit = TimeUnit.HOURS,
        endpoint = "/auth/forgot-password"
    )
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(
            email = body.email.trim()
        )
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword.trim()
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = body.oldPassword.trim(),
            newPassword = body.newPassword.trim()
        )
    }
}
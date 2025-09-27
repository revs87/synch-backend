package com.rvcoding.synch.api.controller

import com.rvcoding.synch.api.dto.AuthenticatedUserDto
import com.rvcoding.synch.api.dto.LoginRequest
import com.rvcoding.synch.api.dto.RegisterRequest
import com.rvcoding.synch.api.dto.UserDto
import com.rvcoding.synch.api.mappers.toAuthenticatedUserDto
import com.rvcoding.synch.api.mappers.toUserDto
import com.rvcoding.synch.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
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
    fun register(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password,
        ).toAuthenticatedUserDto()
    }
}
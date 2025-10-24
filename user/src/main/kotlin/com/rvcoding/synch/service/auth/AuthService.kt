package com.rvcoding.synch.service.auth

import com.rvcoding.synch.domain.events.user.UserEvent
import com.rvcoding.synch.domain.exception.EmailNotVerifiedException
import com.rvcoding.synch.domain.exception.InvalidCredentialsException
import com.rvcoding.synch.domain.exception.InvalidTokenException
import com.rvcoding.synch.domain.exception.NullPasswordException
import com.rvcoding.synch.domain.exception.UserAlreadyExistsException
import com.rvcoding.synch.domain.exception.UserNotFoundException
import com.rvcoding.synch.infra.message_queue.EventPublisher
import com.rvcoding.synch.domain.model.AuthenticatedUser
import com.rvcoding.synch.domain.model.PasswordHash.Encoded
import com.rvcoding.synch.domain.model.PasswordHash.Null
import com.rvcoding.synch.domain.model.User
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.infra.database.entities.RefreshTokenEntity
import com.rvcoding.synch.infra.database.entities.UserEntity
import com.rvcoding.synch.infra.database.mappers.toUser
import com.rvcoding.synch.infra.database.repositories.RefreshTokenRepository
import com.rvcoding.synch.infra.database.repositories.UserRepository
import com.rvcoding.synch.infra.security.PasswordEncoder
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun register(
        email: String,
        username: String,
        password: String
    ): User {
        val trimmedEmail = email.trim()
        val trimmedUsername = username.trim()
        val user = userRepository.findByEmailOrUsername(
            email = trimmedEmail,
            username = trimmedUsername
        )
        if (user != null) throw UserAlreadyExistsException()

        when (val hashedPassword = passwordEncoder.encode(password)) {
            Null -> throw NullPasswordException()
            is Encoded -> {
                val savedUser = userRepository.saveAndFlush(
                    UserEntity(
                        email = trimmedEmail,
                        username = trimmedUsername,
                        hashedPassword = hashedPassword.hash
                    )
                ).toUser()

                val verificationToken = emailVerificationService.createVerificationToken(trimmedEmail)

                eventPublisher.publish(
                    event = UserEvent.Created(
                        userId = savedUser.id,
                        email = savedUser.email,
                        username = savedUser.username,
                        verificationToken = verificationToken.token
                    )
                )

                return savedUser
            }
        }
    }

    fun login(
        email: String,
        password: String
    ): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }
        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException("Invalid refresh token.")
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)

        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            ) ?: throw InvalidTokenException("Invalid refresh token.")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, newRefreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashed)
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                hashedToken = hashed,
                expiresAt = expiresAt
            )
        )
    }

    /**
     * No need for expensive hash generation with bcrypt.
     * */
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
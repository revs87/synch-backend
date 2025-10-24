package com.rvcoding.synch.service.auth

import com.rvcoding.synch.domain.events.user.UserEvent
import com.rvcoding.synch.domain.exception.InvalidCredentialsException
import com.rvcoding.synch.domain.exception.InvalidTokenException
import com.rvcoding.synch.domain.exception.NullPasswordException
import com.rvcoding.synch.domain.exception.SamePasswordException
import com.rvcoding.synch.domain.exception.UserNotFoundException
import com.rvcoding.synch.infra.message_queue.EventPublisher
import com.rvcoding.synch.domain.model.PasswordHash.Encoded
import com.rvcoding.synch.domain.model.PasswordHash.Null
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.infra.database.entities.PasswordResetTokenEntity
import com.rvcoding.synch.infra.database.repositories.PasswordResetTokenRepository
import com.rvcoding.synch.infra.database.repositories.RefreshTokenRepository
import com.rvcoding.synch.infra.database.repositories.UserRepository
import com.rvcoding.synch.infra.security.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${synch.rate-limit.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
    private val eventPublisher: EventPublisher
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        /**
         * If user not found:
         * - Do not throw UserNotFoundException here.
         * - Do not give context to a possible attacker.
         * - Just simply @return
         * */
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES),
        )
        passwordResetTokenRepository.save(token)

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = user.email,
                username = user.username,
                passwordResetToken = token.token,
                expiresInMinutes = expiryMinutes
            )
        )
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Password reset token is invalid.")
        if (resetToken.isUsed) {
            throw InvalidTokenException("Password reset token is already used.")
        }
        if (resetToken.isExpired) {
            throw InvalidTokenException("Password reset token has already expired.")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }

        when (val hashedNewPassword = passwordEncoder.encode(newPassword)) {
            Null -> NullPasswordException()
            is Encoded -> {
                userRepository.save(
                    user.apply {
                        this.hashedPassword = hashedNewPassword.hash
                    }
                )

                passwordResetTokenRepository.save(
                    resetToken.apply {
                        this.usedAt = Instant.now()
                    }
                )

                refreshTokenRepository.deleteByUserId(user.id!!)
            }
        }
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()
        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }
        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        when (val newHashedPassword = passwordEncoder.encode(newPassword)) {
            Null -> NullPasswordException()
            is Encoded -> {
                userRepository.save(
                    user.apply {
                        this.hashedPassword = newHashedPassword.hash
                    }
                )
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            date = Instant.now()
        )
    }
}
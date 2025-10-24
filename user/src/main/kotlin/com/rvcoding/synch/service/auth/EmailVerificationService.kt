package com.rvcoding.synch.service.auth

import com.rvcoding.synch.domain.events.user.UserEvent
import com.rvcoding.synch.domain.exception.InvalidTokenException
import com.rvcoding.synch.domain.exception.UserNotFoundException
import com.rvcoding.synch.infra.message_queue.EventPublisher
import com.rvcoding.synch.domain.model.EmailVerificationToken
import com.rvcoding.synch.infra.database.entities.EmailVerificationTokenEntity
import com.rvcoding.synch.infra.database.mappers.toEmailVerificationToken
import com.rvcoding.synch.infra.database.repositories.EmailVerificationTokenRepository
import com.rvcoding.synch.infra.database.repositories.UserRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: EventPublisher,
    @param:Value("\${synch.rate-limit.email.verification.expiry-hours}") private val expiryHours: Long
) {

    @Transactional
    fun resendVerificationToken(email: String) {
        val verificationToken = createVerificationToken(email)

        if (verificationToken.user.hasEmailVerified) {
            return
        }

        eventPublisher.publish(
            event = UserEvent.RequestResendVerification(
                userId = verificationToken.user.id,
                email = verificationToken.user.email,
                username = verificationToken.user.username,
                verificationToken = verificationToken.token
            )
        )
    }

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository
            .save(token)
            .toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid.")
        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used.")
        }
        if (verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token is expired.")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply { usedAt = Instant.now() }
        )
        userRepository.save(
            verificationToken.user.apply { hasVerifiedEmail = true }
        )
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "UTC") // Every day at 3am UTC
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            date = Instant.now()
        )
    }
}
package com.rvcoding.synch.infra.database.repositories

import com.rvcoding.synch.infra.database.entities.EmailVerificationTokenEntity
import com.rvcoding.synch.infra.database.entities.UserEntity
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, Long> { // (id: Long)
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(date: Instant)
    fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>
}
package com.rvcoding.synch.infra.database.repositories

import com.rvcoding.synch.infra.database.entities.PasswordResetTokenEntity
import com.rvcoding.synch.infra.database.entities.UserEntity
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, Long> { // (id: Long)
    fun findByToken(token: String): PasswordResetTokenEntity?
    fun deleteByExpiresAtLessThan(date: Instant)

    @Modifying
    @Query("""
        UPDATE PasswordResetTokenEntity p
        SET p.usedAt = CURRENT_TIMESTAMP
        WHERE p.user = :user
    """) //  AND p.usedAt IS NULL AND p.expiresAt > CURRENT_TIMESTAMP
    fun invalidateActiveTokensForUser(user: UserEntity)
}
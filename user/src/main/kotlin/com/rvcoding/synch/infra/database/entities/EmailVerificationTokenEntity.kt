package com.rvcoding.synch.infra.database.entities

import com.rvcoding.synch.infra.security.TokenGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(
    name = "email_verification_tokens",
    schema = "user_service"
)
class EmailVerificationTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var token: String = TokenGenerator.generateSecureToken(),
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column
    var usedAt: Instant? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY) // One user can have Many tokens
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity
) {
    val isUsed: Boolean
        get() = usedAt != null

    val isExpired: Boolean
        get() = Instant.now().isAfter(expiresAt)
}
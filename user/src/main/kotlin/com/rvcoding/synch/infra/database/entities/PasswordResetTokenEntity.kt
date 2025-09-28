package com.rvcoding.synch.infra.database.entities

import com.rvcoding.synch.infra.security.TokenGenerator
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import org.hibernate.annotations.CreationTimestamp


@Entity
@Table(
    name = "password_reset_tokens",
    schema = "user_service",
    indexes = [
        Index(name = "idx_password_reset_tokens_token", columnList = "token")
    ]
)
class PasswordResetTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true)
    var token: String = TokenGenerator.generateSecureToken(),
    @Column(nullable = false)
    var expiresAt: Instant,
    @Column(nullable = true)
    var usedAt: Instant? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity
)
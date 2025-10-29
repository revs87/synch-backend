package com.rvcoding.synch.infra.database.entities

import com.rvcoding.synch.domain.type.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import org.hibernate.annotations.CreationTimestamp


@Entity
@Table(
    name = "chat_participants",
    schema = "chat_service",
    indexes = [
        Index(name = "idx_chat_participant_username", columnList = "username"),
        Index(name = "idx_chat_participant_email", columnList = "email")
    ]
)
class ChatParticipantEntity(
    @Id
    var userId: UserId,
    @Column(nullable = false, unique = true)
    var username: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = true)
    var profilePictureUrl: String? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)
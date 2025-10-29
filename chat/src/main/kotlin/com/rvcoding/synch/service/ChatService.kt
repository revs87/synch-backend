package com.rvcoding.synch.service

import com.rvcoding.synch.domain.exception.ChatParticipantNotFoundException
import com.rvcoding.synch.domain.exception.InvalidChatSizeException
import com.rvcoding.synch.domain.models.Chat
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.infra.database.entities.ChatEntity
import com.rvcoding.synch.infra.database.mappers.toChat
import com.rvcoding.synch.infra.database.repositories.ChatParticipantRepository
import com.rvcoding.synch.infra.database.repositories.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
) {

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null)
    }
}
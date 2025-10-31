package com.rvcoding.synch.service

import com.rvcoding.synch.domain.models.ChatParticipant
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.infra.database.mappers.toChatParticipant
import com.rvcoding.synch.infra.database.mappers.toChatParticipantEntity
import com.rvcoding.synch.infra.database.repositories.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository,
) {

    fun createChatParticipant(
        chatParticipant: ChatParticipant
    ) {
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(
        query: String
    ): ChatParticipant? {
        return chatParticipantRepository.findByEmailOrUsername(
            query = query.lowercase().trim()
        )?.toChatParticipant()
    }
}
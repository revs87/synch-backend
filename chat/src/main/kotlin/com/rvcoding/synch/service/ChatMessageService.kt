package com.rvcoding.synch.service

import com.rvcoding.synch.api.dto.ChatMessageDto
import com.rvcoding.synch.api.mappers.toChatMessageDto
import com.rvcoding.synch.domain.exception.ChatNotFoundException
import com.rvcoding.synch.domain.exception.ChatParticipantNotFoundException
import com.rvcoding.synch.domain.exception.ForbiddenException
import com.rvcoding.synch.domain.exception.MessageNotFoundException
import com.rvcoding.synch.domain.models.ChatMessage
import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.ChatMessageId
import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.infra.database.entities.ChatMessageEntity
import com.rvcoding.synch.infra.database.mappers.toChatMessage
import com.rvcoding.synch.infra.database.repositories.ChatMessageRepository
import com.rvcoding.synch.infra.database.repositories.ChatParticipantRepository
import com.rvcoding.synch.infra.database.repositories.ChatRepository
import java.time.Instant
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    @Transactional
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null  // Send as null. Client side will generate a new ID.
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)
    }
}
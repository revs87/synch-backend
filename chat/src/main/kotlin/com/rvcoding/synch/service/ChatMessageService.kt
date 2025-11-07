package com.rvcoding.synch.service

import com.rvcoding.synch.domain.event.MessageDeletedEvent
import com.rvcoding.synch.domain.events.chat.ChatEvent
import com.rvcoding.synch.domain.exception.ChatNotFoundException
import com.rvcoding.synch.domain.exception.ChatParticipantNotFoundException
import com.rvcoding.synch.domain.exception.ForbiddenException
import com.rvcoding.synch.domain.exception.MessageImmutableException
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
import com.rvcoding.synch.infra.message_queue.EventPublisher
import java.time.Instant
import java.util.UUID
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher, // Internal events
    private val eventPublisher: EventPublisher, // RabbitMQ events,
    private val messageCacheEvictionHelper: MessageCacheEvictionHelper
) {

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
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

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId ?: UUID.randomUUID(), // Can't be null - StaleObjectException fix
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        )

        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#messageId"
    )
    fun updateMessage(
        messageId: ChatMessageId,
        senderId: UserId,
        content: String
    ): ChatMessage {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)
        if (message.sender.userId != senderId) {
            throw ForbiddenException()
        }
        val updatable = Instant.now()
            .minusSeconds(MESSAGE_MUTABILITY_TIME_IN_SECONDS)
            .isBefore(message.createdAt)
        if (!updatable) {
            throw MessageImmutableException(messageId)
        }

        message.content = content.trim()

        val savedMessage = chatMessageRepository.saveAndFlush(message)

        eventPublisher.publish(
            event = ChatEvent.UpdatedMessage(
                senderId = sender.userId,
                chatMessageId = savedMessage.id!!,
                message = savedMessage.content
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
        val deletable = Instant.now()
            .minusSeconds(MESSAGE_MUTABILITY_TIME_IN_SECONDS)
            .isBefore(message.createdAt)
        if (!deletable) {
            throw MessageImmutableException(messageId)
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            /* event = */ MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId
            )
        )

        messageCacheEvictionHelper.evictMessagesCache(message.chatId)
    }


    companion object {
        const val MESSAGE_MUTABILITY_TIME_IN_SECONDS = 15 * 60L // 15 minutes to update the message since its creation datetime
    }
}
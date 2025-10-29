package com.rvcoding.synch.api.mappers

import com.rvcoding.synch.api.dto.ChatDto
import com.rvcoding.synch.api.dto.ChatMessageDto
import com.rvcoding.synch.api.dto.ChatParticipantDto
import com.rvcoding.synch.domain.models.Chat
import com.rvcoding.synch.domain.models.ChatMessage
import com.rvcoding.synch.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map {
            it.toChatParticipantDto()
        },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}
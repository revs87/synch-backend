package com.rvcoding.synch.api.dto

import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.ChatMessageId
import com.rvcoding.synch.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val createdAt: Instant,
    val senderId: UserId
)
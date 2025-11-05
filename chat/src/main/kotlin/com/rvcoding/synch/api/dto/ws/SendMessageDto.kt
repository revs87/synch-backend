package com.rvcoding.synch.api.dto.ws

import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.ChatMessageId

data class SendMessageDto(
    val chatId: ChatId,
    val content: String,
    val messageId: ChatMessageId? = null
)
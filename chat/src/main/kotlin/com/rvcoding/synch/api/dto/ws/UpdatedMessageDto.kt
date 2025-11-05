package com.rvcoding.synch.api.dto.ws

import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.ChatMessageId

data class UpdatedMessageDto(
    val chatId: ChatId,
    val messageId: ChatMessageId,
    val content: String
)
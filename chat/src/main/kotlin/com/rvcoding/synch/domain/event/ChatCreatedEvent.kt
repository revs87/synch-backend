package com.rvcoding.synch.domain.event

import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.UserId

data class ChatCreatedEvent(
    val chatId: ChatId,
    val participantIds: List<UserId>
)

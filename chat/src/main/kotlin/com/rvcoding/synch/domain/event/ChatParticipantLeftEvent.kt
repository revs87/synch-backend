package com.rvcoding.synch.domain.event

import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
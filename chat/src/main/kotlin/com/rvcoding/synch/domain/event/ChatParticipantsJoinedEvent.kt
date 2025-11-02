package com.rvcoding.synch.domain.event

import com.rvcoding.synch.domain.type.ChatId
import com.rvcoding.synch.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
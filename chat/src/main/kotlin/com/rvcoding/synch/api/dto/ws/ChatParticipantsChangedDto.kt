package com.rvcoding.synch.api.dto.ws

import com.rvcoding.synch.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId
)

package com.rvcoding.synch.api.dto.ws


enum class IncomingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_UPDATED,
    MESSAGE_DELETED
}

enum class OutgoingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_UPDATED,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    ERROR
}

data class IncomingWebSocketMessage(
    val type: IncomingWebSocketMessageType,
    val payload: String
)

data class OutgoingWebSocketMessage(
    val type: OutgoingWebSocketMessageType,
    val payload: String
)
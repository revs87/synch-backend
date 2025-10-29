package com.rvcoding.synch.api.controller


import com.rvcoding.synch.api.dto.ChatDto
import com.rvcoding.synch.api.dto.CreateChatRequest
import com.rvcoding.synch.api.mappers.toChatDto
import com.rvcoding.synch.api.requestUserId
import com.rvcoding.synch.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
}
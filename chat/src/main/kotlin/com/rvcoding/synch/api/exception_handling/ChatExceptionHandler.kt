package com.rvcoding.synch.api.exception_handling

import com.rvcoding.synch.domain.exception.ChatNotFoundException
import com.rvcoding.synch.domain.exception.ChatParticipantNotFoundException
import com.rvcoding.synch.domain.exception.InvalidChatSizeException
import com.rvcoding.synch.domain.exception.MessageNotFoundException
import com.rvcoding.synch.domain.exception.MessageImmutableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        MessageNotFoundException::class,
        ChatParticipantNotFoundException::class,
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onChatNotFound(e: Exception) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onBadRequest(e: InvalidChatSizeException) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )

    @ExceptionHandler(MessageImmutableException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onForbidden(e: MessageImmutableException) = mapOf(
        "code" to "FORBIDDEN_UPDATE",
        "message" to e.message
    )
}
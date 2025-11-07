package com.rvcoding.synch.api.exception_handling

import com.rvcoding.synch.domain.exception.ChatNotFoundException
import com.rvcoding.synch.domain.exception.ChatParticipantNotFoundException
import com.rvcoding.synch.domain.exception.InvalidChatSizeException
import com.rvcoding.synch.domain.exception.InvalidProfilePictureException
import com.rvcoding.synch.domain.exception.MessageImmutableException
import com.rvcoding.synch.domain.exception.MessageNotFoundException
import com.rvcoding.synch.domain.exception.StorageException
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
        "code" to "MESSAGE_IS_IMMUTABLE",
        "message" to e.message
    )

    @ExceptionHandler(InvalidProfilePictureException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfilePicture(e: InvalidProfilePictureException) = mapOf(
        "code" to "INVALID_PROFILE_PICTURE",
        "message" to e.message
    )

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStorageFailure(e: StorageException) = mapOf(
        "code" to "STORAGE_ERROR",
        "message" to e.message
    )
}
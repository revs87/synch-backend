package com.rvcoding.synch.api.exception

import com.rvcoding.synch.domain.exception.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun onUserAlreadyExists(e: UserAlreadyExistsException) = mapOf(
        "code" to "USER_EXISTS",
        "message" to e.message
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "code" to "VALIDATION_ERROR",
                    "message" to errors
                )
            )
    }
}
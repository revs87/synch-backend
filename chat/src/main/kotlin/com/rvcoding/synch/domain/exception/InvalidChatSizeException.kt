package com.rvcoding.synch.domain.exception

class InvalidChatSizeException : RuntimeException(
    "There must be at least 2 unique participants to create a chat."
)
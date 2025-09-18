package com.rvcoding.synch.domain.model

sealed class PasswordHash {
    class Encoded(val hash: String) : PasswordHash()
    object Null : PasswordHash()
}
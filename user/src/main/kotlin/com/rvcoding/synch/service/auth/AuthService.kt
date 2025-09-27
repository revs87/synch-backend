package com.rvcoding.synch.service.auth

import com.rvcoding.synch.domain.exception.NullPasswordException
import com.rvcoding.synch.domain.exception.UserAlreadyExistsException
import com.rvcoding.synch.domain.model.PasswordHash.Encoded
import com.rvcoding.synch.domain.model.PasswordHash.Null
import com.rvcoding.synch.domain.model.User
import com.rvcoding.synch.infra.database.entities.UserEntity
import com.rvcoding.synch.infra.database.mappers.toUser
import com.rvcoding.synch.infra.database.repositories.UserRepository
import com.rvcoding.synch.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(
        email: String,
        username: String,
        password: String
    ): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim()
        )
        if (user != null) throw UserAlreadyExistsException()

        when (val hashedPassword = passwordEncoder.encode(password)) {
            Null -> throw NullPasswordException()
            is Encoded -> {
                val savedUser = userRepository.save(
                    UserEntity(
                        email = email.trim(),
                        username = username.trim(),
                        hashedPassword = hashedPassword.hash
                    )
                ).toUser()
                return savedUser
            }
        }
    }
}
package com.rvcoding.synch

import com.rvcoding.synch.infra.database.entities.UserEntity
import com.rvcoding.synch.infra.database.repositories.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class SynchApplication

fun main(args: Array<String>) {
    runApplication<SynchApplication>(*args)
}

@Component
class Demo(
    private val repository: UserRepository
) {

    @PostConstruct
    fun init() {
        repository.save(
            UserEntity(
                email = "test@test.com",
                username = "test",
                hashedPassword = "123"
            )
        )
    }
}
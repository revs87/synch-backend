package com.rvcoding.synch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SynchApplication

fun main(args: Array<String>) {
    runApplication<SynchApplication>(*args)
}

//@Component
//class Demo(
//    private val repository: UserRepository
//) {
//
//    @PostConstruct
//    fun init() {
//        repository.save(
//            UserEntity(
//                email = "test@test.com",
//                username = "test",
//                hashedPassword = "123"
//            )
//        )
//    }
//}
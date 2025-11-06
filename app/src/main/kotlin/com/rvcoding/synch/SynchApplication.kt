package com.rvcoding.synch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
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
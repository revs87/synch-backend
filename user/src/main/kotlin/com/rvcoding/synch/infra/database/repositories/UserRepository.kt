package com.rvcoding.synch.infra.database.repositories

import com.rvcoding.synch.domain.type.UserId
import com.rvcoding.synch.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}
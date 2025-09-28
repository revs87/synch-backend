package com.rvcoding.synch.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.rvcoding.synch.api.utils.Password
import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length

data class RegisterRequest @JsonCreator constructor(
    @field:Email(message = "Must be a valid email address.")
    @JsonProperty("email")
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username lenght must be between 3 and 20 characters.")
    @JsonProperty("username")
    val username: String,
    @field:Password
    @JsonProperty("password")
    val password: String
)

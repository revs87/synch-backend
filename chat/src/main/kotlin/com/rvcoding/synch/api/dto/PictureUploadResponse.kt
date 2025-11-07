package com.rvcoding.synch.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class PictureUploadResponse @JsonCreator constructor(
    @JsonProperty("uploadUrl")
    val uploadUrl: String,
    @JsonProperty("publicUrl")
    val publicUrl: String,
    @JsonProperty("headers")
    val headers: Map<String, String>,
    @JsonProperty("expiresAt")
    val expiresAt: Instant
)

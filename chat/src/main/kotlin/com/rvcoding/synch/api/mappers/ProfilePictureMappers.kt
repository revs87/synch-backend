package com.rvcoding.synch.api.mappers

import com.rvcoding.synch.api.dto.PictureUploadResponse
import com.rvcoding.synch.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse(): PictureUploadResponse {
    return PictureUploadResponse(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers,
        expiresAt = expiresAt
    )
}
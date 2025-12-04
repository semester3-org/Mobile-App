package com.apk.koshub.models

data class UpdateProfileResponse(
    val status: String,
    val message: String,
    val profile_picture: String? = null
)

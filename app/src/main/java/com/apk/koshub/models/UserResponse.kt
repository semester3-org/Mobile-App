package com.apk.koshub.models

data class UserResponse(
    val status: String,
    val message: String,
    val user: User? = null
)

package com.apk.koshub.models

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val full_name: String,
    val phone: String,
    val user_type: String = "user"
)

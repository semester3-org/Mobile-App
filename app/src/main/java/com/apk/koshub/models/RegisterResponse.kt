package com.apk.koshub.models

data class RegisterResponse(
    val status: String,
    val message: String,
    val data: UserData?
)

data class UserData(
    val username: String,
    val email: String,
    val full_name: String,
    val phone: String,
    val user_type: String
)

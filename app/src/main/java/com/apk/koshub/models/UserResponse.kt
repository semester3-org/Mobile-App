package com.apk.koshub.models

data class UserResponse(
    val status: String,
    val code: Int,
    val message: String,
    val user: User? = null

)

package com.apk.koshub.models

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val full_name: String,
    val phone: String,
    val user_type: String,
    val profile_image: String? = null
)

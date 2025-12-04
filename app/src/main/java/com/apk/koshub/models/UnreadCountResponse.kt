package com.apk.koshub.models

data class UnreadCountResponse(
    val success: Boolean,
    val unread_count: Int = 0,
    val message: String? = null
)

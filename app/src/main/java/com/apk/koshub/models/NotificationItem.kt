package com.apk.koshub.models

data class NotificationItem(
    val id: Int,
    val user_id: Int?,
    val kos_id: Int?,
    val type: String?,
    val title: String?,
    val message: String?,
    val wishlist_count: Int?,
    val review_count: Int?,
    val related_id: Int?,
    val is_read: Int = 0,
    val created_at: String?
)

data class NotificationResponse(
    val success: Boolean,
    val data: List<NotificationItem>?
)

package com.apk.koshub.models

import com.google.gson.annotations.SerializedName
data class NotificationItem(
    val id: Int,
    @SerializedName("user_id") val user_id: Int?,
    @SerializedName("kos_id") val kos_id: Int?,
    val type: String?,
    val title: String?,
    val message: String?,
    val wishlist_count: Int?,
    val review_count: Int?,
    val related_id: Int?,
    @SerializedName("is_read") val is_read: Int = 0,
    val created_at: String?
)

data class NotificationResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    val message: String? = null,
    val data: List<NotificationItem>? = null
) {
    fun isOk(): Boolean = (success == true) || (status == "success")
}
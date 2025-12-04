package com.apk.koshub.models

import com.google.gson.annotations.SerializedName

data class BookingListResponse(
    val status: String,
    val message: String? = null,
    val data: List<BookingItem> = emptyList()
)

data class BookingItem(
    val id: Int,

    @SerializedName("kos_id")
    val kosId: Int,

    @SerializedName("kos_name")
    val kosName: String,

    @SerializedName("location_name")
    val locationName: String,

    val address: String,
    val image: String?,

    @SerializedName("check_in_date")
    val checkInDate: String,

    @SerializedName("check_out_date")
    val checkOutDate: String?,

    @SerializedName("booking_type")
    val bookingType: String,          // "monthly" / "daily"

    @SerializedName("duration_months")
    val durationMonths: Int?,

    @SerializedName("total_price")
    val totalPrice: Int,

    // pending / confirmed / rejected / ...
    val status: String,

    // unpaid / pending / paid / ...
    @SerializedName("payment_status")
    val paymentStatus: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("kos_type")
    val kosType: String?,
)
data class BookingCreateResponse(
    val status: String,
    val message: String,
    val data: BookingItem?
)

data class BookingDetailResponse(
    val status: String,
    val message: String? = null,
    val data: BookingDetail?
)

data class BookingDetail(
    val id: Int,
    val kos_id: Int,
    val user_id: Int,
    val check_in_date: String,
    val check_out_date: String?,
    val booking_type: String,
    val duration_months: Int?,
    val total_price: Int,
    val status: String,
    val payment_status: String,
    val created_at: String?,
    val paid_at: String?,

    val kos_name: String,
    val location_name: String,
    val address: String,
    val kos_type: String,
    val price_monthly: Int,
    val price_daily: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val facilities: String?,
    val rating: Float,
    val images: List<String>
)


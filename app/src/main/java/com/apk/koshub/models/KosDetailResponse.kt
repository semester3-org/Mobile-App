package com.apk.koshub.models

import com.google.gson.annotations.SerializedName

data class KosDetailResponse(
    val status: String? = null,
    val message: String? = null,
    val data: KosDetailDto? = null
) {
    val isSuccess: Boolean
        get() = status?.lowercase() == "success"
}

data class KosDetailDto(
    val id: Int,
    val name: String,
    val description: String? = null,

    @SerializedName("location_name") val locationName: String? = null,

    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    @SerializedName("price_monthly") val priceMonthly: Int? = null,

    @SerializedName("price_daily")
    val priceDaily: Int?,


    // JSON lu STRING: "AC, Lemari, ..."
    val facilities: String? = null,
    val rating: Double? = 0.0,
    val images: List<String>? = emptyList(),

    @SerializedName("kos_type") val kosType: String? = null,
    @SerializedName("rating_count") val ratingCount: Int? = null,
    @SerializedName("facilities_list") val facilitiesList: List<FacilityDto>? = null
)

data class FacilityDto(
    val id: Int,
    val name: String,
    val icon: String?
)



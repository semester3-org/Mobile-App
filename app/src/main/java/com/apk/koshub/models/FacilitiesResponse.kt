package com.apk.koshub.models

data class FacilitiesResponse(
    val status: String,
    val message: String?,
    val data: List<FacilityDto>
)
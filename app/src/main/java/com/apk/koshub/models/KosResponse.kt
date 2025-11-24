package com.apk.koshub.models

import com.google.gson.annotations.SerializedName

// ------------------- KOS LIST (UNTUK Explore & Home) -------------------
data class KosResponse(
    val status: String? = null,
    val data: List<KosDto> = emptyList(),
    val success: Boolean? = null,   // kalau server kadang pakai success
    val message: String? = null
) {
    val isSuccess: Boolean
        get() =
            success == true ||
                    status?.lowercase() in listOf("success", "true", "1") ||
                    (status == null && data.isNotEmpty())
}

// ------------------- KOS DTO (Mapping Data Kos) -------------------
data class KosDto(
    val id: Int,
    val name: String,
    val description: String,

    @SerializedName("location_name")
    val locationName: String,

    val address: String,
    val latitude: Double,
    val longitude: Double,

    @SerializedName("price_monthly")
    val priceMonthly: Int,

    val facilities: String,

    val rating: Double? = null,

    val images: List<String>,

    val jenisKos: String? = null,

    @SerializedName("jumlah_kamar")
    val jumlahKamar: Int? = null
)

// ---------- MAPPING KOS LIST → KOS ITEM CARD ----------
fun KosDto.toKosItemCard(): KosItemCard {
    return KosItemCard(
        id = id,
        user_id = 0,   // Kos list tidak punya user_id, aman di-set 0
        nama = name,
        lokasi = locationName,
        fasilitas = facilities,
        harga = "Rp ${String.format("%,d", priceMonthly).replace(",", ".")}/bulan",
        rating = rating ?: 0.0,
        latitude = latitude,
        longitude = longitude,
        gambar = images.firstOrNull() ?: "",
        isFavorite = false
    )
}

// ---------- MAPPING UNTUK HOME LIST ----------
fun KosDto.toKosItem(): KosItem {
    return KosItem(
        id = id,
        nama = name,
        lokasi = locationName,
        harga = "Rp ${String.format("%,d", priceMonthly).replace(",", ".")}/bulan",
        gambar = images.firstOrNull() ?: "",
        deskripsi = description,
        fasilitas = facilities.split(",").map { it.trim() },
        jenisKos = jenisKos ?: "Unknown",
        jumlahKamar = jumlahKamar ?: 0,
        rating = rating ?: 0.0,
        latitude = latitude,
        longitude = longitude
    )
}

fun KosDto.toKosItemCardForFavorite(): KosItemCard {
    return KosItemCard(
        id = id,
        user_id = 0,
        nama = name ?: "-",
        lokasi = locationName ?: "-",
        fasilitas = facilities ?: "-",
        harga = "Rp ${String.format("%,d", priceMonthly ?: 0).replace(",", ".")}/bulan",
        rating = rating ?: 0.0,
        latitude = latitude,
        longitude = longitude,
        gambar = images.orEmpty().firstOrNull() ?: "",
        isFavorite = true
    )
}


// ------------------- FAVORITE RESPONSE (MAPPING FAVORITE DATA) -------------------
data class FavoriteResponse(
    val success: Boolean,
    val message: String?,
    val data: List<FavoriteDto>?
)

data class FavoriteDto(
    val id: Int,

    @SerializedName("name")
    val nama: String,

    @SerializedName("price_monthly")
    val harga: Int,

    @SerializedName("location_name")
    val lokasi: String,

    val fasilitas: String,

    @SerializedName("images")
    val images: List<String>,

    val latitude: Double,
    val longitude: Double,

    @SerializedName("isFavorite")
    val isFavorite: Int
)

// ---------- MAPPING FAVORITE DTO → KOS ITEM CARD ----------
fun FavoriteDto.toKosItemCard(): KosItemCard {
    return KosItemCard(
        id = id,
        user_id = 0,
        nama = nama,
        lokasi = lokasi,
        fasilitas = fasilitas,
        harga = "Rp ${String.format("%,d", harga).replace(",", ".")}/bulan",
        rating = 0.0, // Biasanya rating default di-set ke 0 untuk favorite
        gambar = images.firstOrNull() ?: "",
        latitude = latitude,
        longitude = longitude,
        isFavorite = true
    )
}


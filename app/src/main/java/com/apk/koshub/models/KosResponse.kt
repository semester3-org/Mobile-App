package com.apk.koshub.models

import com.google.gson.annotations.SerializedName

data class KosResponse(
    val status: String,
    val data: List<KosDto>
)

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
    val rating: Double,
    val images: List<String>,

    // Tambahkan jenisKos di sini
    val jenisKos: String? = null,

    @SerializedName("jumlah_kamar")
    val jumlahKamar: Int? = null
)


// -------------- Explore (card grid) --------------
fun KosDto.toKosItemCard(): KosItemCard {
    return KosItemCard(
        id = id,
        nama = name,
        lokasi = locationName,
        fasilitas = facilities,
        harga = "Rp ${String.format("%,d", priceMonthly).replace(",", ".")}/bulan",
        rating = rating,
        gambar = images.firstOrNull() ?: "",
        isFavorite = false
    )
}

// -------------- Home (horizontal list) --------------
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
        jumlahKamar = jumlahKamar ?: 0  // ⬅ wajib ada
    )
}
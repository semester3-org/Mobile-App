package com.apk.koshub.models

data class KosItemCard(
    val id: Int,
    val user_id: Int,
    val nama: String,
    val lokasi: String,
    val fasilitas: String,
    val harga: String,
    val rating: Double,
    val gambar: String,
    val latitude: Double,
    val longitude: Double,
    var isFavorite: Boolean = false
)

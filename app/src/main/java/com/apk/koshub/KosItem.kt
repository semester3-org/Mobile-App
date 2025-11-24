package com.apk.koshub.models

data class KosItem(
    val id: Int,
    val nama: String,
    val lokasi: String,
    val harga: String,
    val gambar: String,
    val deskripsi: String,
    val fasilitas: List<String>,
    val jenisKos: String,
    val jumlahKamar: Int,
    val rating: Double?,
    val latitude: Double,
    val longitude: Double
)
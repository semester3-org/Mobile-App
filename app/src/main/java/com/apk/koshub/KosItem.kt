package com.apk.koshub.models

data class KosItem(
    val id: Int,
    val nama: String,
    val lokasi: String,
    val harga: String,
    val gambar: Int = android.R.drawable.ic_menu_gallery // Default placeholder
)
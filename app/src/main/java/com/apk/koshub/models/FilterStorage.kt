package com.apk.koshub.models

import android.content.Context

data class FilterState(
    val minHarga: Int = 0,
    val maxHarga: Int = 0,
    val fasilitas: Set<String> = emptySet(),
    val jenisKos: String = "",
    val jumlahKamar: Int = 0
)

class FilterStorage(context: Context) {

    private val pref = context.getSharedPreferences("FilterPreferences", Context.MODE_PRIVATE)

    fun save(state: FilterState) {
        pref.edit().apply {
            putInt("minHarga", state.minHarga)
            putInt("maxHarga", state.maxHarga)
            putStringSet("fasilitas", state.fasilitas)
            putString("jenisKos", state.jenisKos)
            putInt("jumlahKamar", state.jumlahKamar)  // <-- SAVE INT
        }.apply()
    }

    fun load(): FilterState {
        return FilterState(
            minHarga = pref.getInt("minHarga", 0),
            maxHarga = pref.getInt("maxHarga", 0),
            fasilitas = pref.getStringSet("fasilitas", emptySet()) ?: emptySet(),
            jenisKos = pref.getString("jenisKos", "") ?: "",
            jumlahKamar = pref.getInt("jumlahKamar", 0)  // <-- LOAD INT
        )
    }

    fun clear() {
        pref.edit().clear().apply()
    }
}

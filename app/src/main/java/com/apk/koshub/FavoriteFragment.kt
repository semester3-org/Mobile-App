package com.apk.koshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.models.KosItem

class FavoriteFragment : Fragment() {

    private lateinit var rvFavorite: RecyclerView
    private lateinit var tvEmptyFavorite: TextView
    private lateinit var adapter: KosAdapter
    private var favoriteKos = mutableListOf<KosItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        rvFavorite = view.findViewById(R.id.rvFavorite)
        tvEmptyFavorite = view.findViewById(R.id.tvEmptyFavorite)

        // Setup RecyclerView
        rvFavorite.layoutManager = LinearLayoutManager(context)
        adapter = KosAdapter(emptyList()) { kos ->
            android.widget.Toast.makeText(
                context,
                "Favorite: ${kos.nama}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        rvFavorite.adapter = adapter

        // Dummy data (kosong dulu, nanti dari SharedPrefs/DB)
        favoriteKos.addAll(getDummyFavorites(1)) // 1 item dummy
        updateUI()

        return view
    }

    private fun updateUI() {
        if (favoriteKos.isEmpty()) {
            rvFavorite.visibility = View.GONE
            tvEmptyFavorite.visibility = View.VISIBLE
        } else {
            rvFavorite.visibility = View.VISIBLE
            tvEmptyFavorite.visibility = View.GONE
            adapter.updateList(favoriteKos)
        }
    }

    private fun getDummyFavorites(count: Int): List<KosItem> {
        val list = mutableListOf<KosItem>()

        for (i in 1..count) {
            list.add(
                KosItem(
                    id = i,
                    nama = "Kos Favorit $i",
                    lokasi = "Jember City Center",
                    harga = "Rp ${800_000 + (i * 50_000)}/bulan",
                    gambar = "https://picsum.photos/300/200?random=${1000 + i}",
                    deskripsi = "Kos favorit $i",
                    fasilitas = listOf("WiFi", "AC", "Parking"),
                    jenisKos = if (i % 2 == 0) "Putra" else "Putri",
                    jumlahKamar = (1..5).random()   // ⬅ tambahin ini biar dummy valid
                )
            )
        }

        return list
    }
}
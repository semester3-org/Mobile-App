package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosCardAdapter
import com.apk.koshub.models.KosItemCard

class ExploreFragment : Fragment() {

    private lateinit var rvExplore: RecyclerView
    private lateinit var etSearchExplore: EditText
    private lateinit var adapter: KosCardAdapter
    private var allKos = mutableListOf<KosItemCard>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        etSearchExplore = view.findViewById(R.id.etSearchExplore)
        rvExplore = view.findViewById(R.id.rvExplore)

        // 🔹 Tampilkan grid 2 kolom
        rvExplore.layoutManager = GridLayoutManager(context, 2)

        // 🔹 Adapter
        adapter = KosCardAdapter(mutableListOf()) { kos ->
            openDetailFromKosCard(kos)
        }
        rvExplore.adapter = adapter

        // 🔹 Dummy data
        allKos.addAll(getDummyKos(12))
        adapter.updateList(allKos)

        // 🔹 Pencarian live
        etSearchExplore.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterKos(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun filterKos(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(allKos)
            return
        }
        val filtered = allKos.filter {
            it.nama.contains(query, ignoreCase = true) ||
                    it.lokasi.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }
    private fun openDetailFromKosCard(kos: KosItemCard) {
        val detailFragment = DetailKosFragment.newInstance(
            nama = kos.nama,
            lokasi = kos.lokasi,
            harga = kos.harga,
            kategori = "Kos",
            deskripsi = ""
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }


    // 🔹 Dummy data (bisa ganti nanti dari API / database)
    private fun getDummyKos(count: Int): List<KosItemCard> {
        val list = mutableListOf<KosItemCard>()
        for (i in 1..count) {
            list.add(
                KosItemCard(
                    id = i,
                    nama = "Kos Nyaman $i",
                    lokasi = "Tawangmangu, Area $i",
                    fasilitas = "WiFi, Lemari, Meja",
                    harga = "Rp ${600 + i * 100}.000/bulan",
                    rating = 4.5 + (i % 5) * 0.1,
                    gambar = "https://picsum.photos/300/200?random=$i",
                    isFavorite = false
                )
            )
        }
        return list
    }
}

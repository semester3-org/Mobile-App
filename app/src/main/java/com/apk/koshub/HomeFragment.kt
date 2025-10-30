package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.models.KosItem

class HomeFragment : Fragment() {

    private lateinit var rvRekomendasi: RecyclerView
    private lateinit var rvFavoritHome: RecyclerView
    private lateinit var etSearchHome: EditText
    private lateinit var adapterRekomendasi: KosAdapter
    private lateinit var adapterFavorit: KosAdapter
    private var allRekomendasi = mutableListOf<KosItem>()
    private var allFavorit = mutableListOf<KosItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi view
        etSearchHome = view.findViewById(R.id.etSearchHome)
        rvRekomendasi = view.findViewById(R.id.rvRekomendasi)
        rvFavoritHome = view.findViewById(R.id.rvFavoritHome)
        val ibSearchHome: ImageButton = view.findViewById(R.id.ibSearchHome)

        // Setup RecyclerViews (horizontal scroll)
        setupRecyclerViews()

        // Dummy data
        allRekomendasi.addAll(getDummyKos(5))
        allFavorit.addAll(getDummyKos(4))
        adapterRekomendasi.updateList(allRekomendasi)
        adapterFavorit.updateList(allFavorit)

        // Fitur search
        etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterKos(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Tombol search → buka halaman lain (opsional)
        ibSearchHome.setOnClickListener {
            // Ganti dengan fragment/activity yang sesuai
            // contoh:
            // requireActivity().supportFragmentManager.beginTransaction()
            //    .replace(R.id.fragment_container, SearchFragment())
            //    .addToBackStack(null)
            //    .commit()
        }

        // Filter buttons
        setupFilters(view)

        return view
    }

    private fun setupRecyclerViews() {
        // 🔹 Horizontal untuk tiap kategori
        rvRekomendasi.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFavoritHome.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Adapter
        adapterRekomendasi = KosAdapter(emptyList()) { kos ->
            android.widget.Toast.makeText(context, "Detail: ${kos.nama}", android.widget.Toast.LENGTH_SHORT).show()
        }
        adapterFavorit = KosAdapter(emptyList()) { kos ->
            android.widget.Toast.makeText(context, "Favorit: ${kos.nama}", android.widget.Toast.LENGTH_SHORT).show()
        }

        rvRekomendasi.adapter = adapterRekomendasi
        rvFavoritHome.adapter = adapterFavorit
    }

    private fun setupFilters(view: View) {
        val btnAll: Button = view.findViewById(R.id.btnFilterAll)
        val btnPutra: Button = view.findViewById(R.id.btnFilterPutra)
        val btnPutri: Button = view.findViewById(R.id.btnFilterPutri)
        val btnAC: Button = view.findViewById(R.id.btnFilterAC)

        btnAll.setOnClickListener { resetFilters(); btnAll.setBackgroundResource(R.drawable.bg_filter_selected) }
        btnPutra.setOnClickListener { resetFilters(); btnPutra.setBackgroundResource(R.drawable.bg_filter_selected); filterByCategory("Putra") }
        btnPutri.setOnClickListener { resetFilters(); btnPutri.setBackgroundResource(R.drawable.bg_filter_selected); filterByCategory("Putri") }
        btnAC.setOnClickListener { resetFilters(); btnAC.setBackgroundResource(R.drawable.bg_filter_selected); filterByCategory("AC") }
    }

    private fun resetFilters() {
        val view = view ?: return
        view.findViewById<Button>(R.id.btnFilterAll).setBackgroundResource(R.drawable.bg_filter)
        view.findViewById<Button>(R.id.btnFilterPutra).setBackgroundResource(R.drawable.bg_filter)
        view.findViewById<Button>(R.id.btnFilterPutri).setBackgroundResource(R.drawable.bg_filter)
        view.findViewById<Button>(R.id.btnFilterAC).setBackgroundResource(R.drawable.bg_filter)
        adapterRekomendasi.updateList(allRekomendasi)
        adapterFavorit.updateList(allFavorit)
    }

    private fun filterByCategory(category: String) {
        val filteredRekomendasi = allRekomendasi.filter { it.nama.contains(category, true) }
        val filteredFavorit = allFavorit.filter { it.nama.contains(category, true) }
        adapterRekomendasi.updateList(filteredRekomendasi)
        adapterFavorit.updateList(filteredFavorit)
    }

    private fun filterKos(query: String) {
        if (query.isEmpty()) {
            adapterRekomendasi.updateList(allRekomendasi)
            adapterFavorit.updateList(allFavorit)
            return
        }
        val filteredRekomendasi = allRekomendasi.filter { it.nama.contains(query, true) || it.lokasi.contains(query, true) }
        val filteredFavorit = allFavorit.filter { it.nama.contains(query, true) || it.lokasi.contains(query, true) }
        adapterRekomendasi.updateList(filteredRekomendasi)
        adapterFavorit.updateList(filteredFavorit)
    }

    private fun getDummyKos(count: Int): List<KosItem> {
        val list = mutableListOf<KosItem>()
        for (i in 1..count) {
            list.add(KosItem(i, "Kos $i Jember", "Jember, Dekat Unej", "Rp 800.000/bulan"))
        }
        return list
    }
}

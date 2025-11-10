package com.apk.koshub.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.models.KosItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.CheckBox
import android.widget.ImageButton


class HomeFragment : Fragment() {

    private lateinit var rvRekomendasi: RecyclerView
    private lateinit var rvFavoritHome: RecyclerView
    private lateinit var etSearchHome: EditText
    private lateinit var btnFilter: ImageView
    private lateinit var adapterRekomendasi: KosAdapter
    private lateinit var adapterFavorit: KosAdapter
    private var allRekomendasi = mutableListOf<KosItem>()
    private var allFavorit = mutableListOf<KosItem>()
    private var selectedFilters = mutableSetOf<String>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 🧩 Inisialisasi View
        etSearchHome = view.findViewById(R.id.etSearchHome)
        rvRekomendasi = view.findViewById(R.id.rvRekomendasi)
        rvFavoritHome = view.findViewById(R.id.rvFavoritHome)
        btnFilter = view.findViewById(R.id.ibFilterHome) // ganti icon jadi filter kalau mau
        val ibSearchIcon = view.findViewById<ImageButton>(R.id.ibSearchIcon)


        // 🧩 Setup Recyclerview
        setupRecyclerViews()

        // 🧩 Dummy Data
        allRekomendasi.addAll(getDummyKos("Rekomendasi", 6))
        allFavorit.addAll(getDummyKos("Favorit", 5))

        adapterRekomendasi.updateList(allRekomendasi)
        adapterFavorit.updateList(allFavorit)

        // 🧩 Fitur Search
        etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterKos(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🧩 Tombol Filter (munculin BottomSheet)
        btnFilter.setOnClickListener {
            showFilterDialog()
        }
        ibSearchIcon.setOnClickListener {
            val query =etSearchHome.text.toString()
            filterKos(query)
            Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    private fun setupRecyclerViews() {
        rvRekomendasi.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFavoritHome.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        adapterRekomendasi = KosAdapter(emptyList()) { kos ->
            Toast.makeText(context, "Klik ${kos.nama}", Toast.LENGTH_SHORT).show()
        }
        adapterFavorit = KosAdapter(emptyList()) { kos ->
            Toast.makeText(context, "Klik favorit ${kos.nama}", Toast.LENGTH_SHORT).show()
        }

        rvRekomendasi.adapter = adapterRekomendasi
        rvFavoritHome.adapter = adapterFavorit
    }

    private fun filterKos(query: String) {
        val filteredRekomendasi = if (query.isEmpty()) allRekomendasi else allRekomendasi.filter {
            it.nama.contains(query, true) || it.lokasi.contains(query, true)
        }
        val filteredFavorit = if (query.isEmpty()) allFavorit else allFavorit.filter {
            it.nama.contains(query, true) || it.lokasi.contains(query, true)
        }

        adapterRekomendasi.updateList(filteredRekomendasi)
        adapterFavorit.updateList(filteredFavorit)


    //Simpan Dan Reset State Filter
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("selectedFilters", ArrayList(selectedFilters))
    }
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getStringArrayList("selectedFilters")?.let {
            selectedFilters.clear()
            selectedFilters.addAll(it)
            applyMultiFilter(selectedFilters.toList())
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)
        dialog.setContentView(view)

        val cbPutra = view.findViewById<CheckBox>(R.id.cbPutra)
        val cbPutri = view.findViewById<CheckBox>(R.id.cbPutri)
        val cbAC = view.findViewById<CheckBox>(R.id.cbAC)
        val cbWifi = view.findViewById<CheckBox>(R.id.cbWifi)
        val btnReset = view.findViewById<Button>(R.id.btnResetFilter)
        val btnTerapkan = view.findViewById<Button>(R.id.btnTerapkanFilter)
        val cbCampur = view.findViewById<CheckBox>(R.id.cbCampur)

        cbPutra.isChecked = selectedFilters.contains("Putra")
        cbPutri.isChecked = selectedFilters.contains("Putri")
        cbAC.isChecked = selectedFilters.contains("AC")
        cbWifi.isChecked = selectedFilters.contains("Wifi")
        cbCampur.isChecked = selectedFilters.contains("Campur")

        btnReset.setOnClickListener {
            cbPutra.isChecked = false
            cbPutri.isChecked = false
            cbAC.isChecked = false
            cbWifi.isChecked = false
            cbCampur.isChecked = false
            resetFilters()
            selectedFilters.clear() // sekalian reset state
            dialog.dismiss()
        }

        btnTerapkan.setOnClickListener {
            val filters = mutableListOf<String>()
            if (cbPutra.isChecked) filters.add("Putra")
            if (cbPutri.isChecked) filters.add("Putri")
            if (cbAC.isChecked) filters.add("AC")
            if (cbWifi.isChecked) filters.add("Wifi")
            if (cbCampur.isChecked)filters.add("Campur")
            selectedFilters.clear()
            selectedFilters.addAll(filters)
            applyMultiFilter(filters)
            dialog.dismiss()
        }

        dialog.show()

    }


        private fun resetFilters() {
            // Balikin semua list ke kondisi awal
            adapterRekomendasi.updateList(allRekomendasi)
            adapterFavorit.updateList(allFavorit)
        }

        private fun applyMultiFilter(filters: List<String>) {
            if (filters.isEmpty()) {
                adapterRekomendasi.updateList(allRekomendasi)
                adapterFavorit.updateList(allFavorit)
                return
            }

            val filteredRekomendasi = allRekomendasi.filter { kos ->
                filters.any { f -> kos.nama.contains(f, ignoreCase = true) }
            }
            val filteredFavorit = allFavorit.filter { kos ->
                filters.any { f -> kos.nama.contains(f, ignoreCase = true) }
            }

            adapterRekomendasi.updateList(filteredRekomendasi)
            adapterFavorit.updateList(filteredFavorit)
        }

    }
    private fun getDummyKos(prefix: String, count: Int): List<KosItem> {
        val list = mutableListOf<KosItem>()
        for (i in 1..count) {
            list.add(KosItem(i, "Kos $prefix $i", "Jember, Dekat Unej", "Rp 800.000/bulan"))
        }
        return list
    }

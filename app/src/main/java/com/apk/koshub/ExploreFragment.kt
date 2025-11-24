package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosCardAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.FilterState
import com.apk.koshub.models.KosItemCard
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.toKosItemCard
import com.apk.koshub.utils.SharedPrefHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreFragment : Fragment(), FilterDialogFragment.OnFilterApplied {

    private lateinit var rvExplore: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: KosCardAdapter
    private lateinit var db: DatabaseHelper
    private lateinit var pref: SharedPrefHelper

    private val allKos = mutableListOf<KosItemCard>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val filterButton = view.findViewById<ImageButton>(R.id.btnFilter)
        filterButton.setOnClickListener {
            FilterDialogFragment().show(parentFragmentManager, "filterDialog")
        }

        rvExplore = view.findViewById(R.id.rvExplore)
        etSearch = view.findViewById(R.id.etSearchExplore)

        db = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        // ✅ FIX UTAMA: GridLayout auto measure supaya RV di dalam ScrollView tampil semua
        val glm = GridLayoutManager(requireContext(), 2)
        glm.isAutoMeasureEnabled = true
        rvExplore.layoutManager = glm
        rvExplore.setHasFixedSize(false)
        rvExplore.isNestedScrollingEnabled = false

        adapter = KosCardAdapter(mutableListOf()) { kos ->
            openDetail(kos)
        }
        rvExplore.adapter = adapter

        // reset search biar gak ke-filter otomatis
        etSearch.setText("")

        loadKos()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterKos(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        if (pref.isLoggedIn()) {
            refreshFavorites()
        }
    }

    private fun loadKos() {
        ApiClient.instance.getKosList()
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(
                    call: Call<KosResponse>,
                    res: Response<KosResponse>
                ) {
                    val kosBody = res.body()

                    if (res.isSuccessful && kosBody?.isSuccess == true) {

                        val list = kosBody.data.map { it.toKosItemCard() }

                        allKos.clear()
                        allKos.addAll(list)
                        adapter.updateList(allKos)

                        if (pref.isLoggedIn()) {
                            refreshFavorites()
                        }

                    } else {
                        Toast.makeText(requireContext(), "Gagal memuat kos", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun refreshFavorites() {
        val user = db.getUser() ?: return

        ApiClient.instance.getFavoriteKos(userId = user.id)
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(
                    call: Call<KosResponse>,
                    favRes: Response<KosResponse>
                ) {
                    val favBody = favRes.body()

                    val favoriteIds =
                        if (favRes.isSuccessful && favBody?.isSuccess == true) {
                            favBody.data.map { it.id }.toSet()
                        } else emptySet()

                    allKos.forEach { item ->
                        item.isFavorite = favoriteIds.contains(item.id)
                    }

                    adapter.updateList(allKos)
                    filterKos(etSearch.text.toString())
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) { }
            })
    }

    private fun filterKos(query: String) {
        val result = if (query.isEmpty()) {
            allKos
        } else {
            allKos.filter {
                it.nama.contains(query, true) ||
                        it.lokasi.contains(query, true)
            }
        }
        adapter.updateList(result)
    }

    private fun openDetail(kos: KosItemCard) {
        val fm = parentFragmentManager
        val detail = DetailKosFragment.newInstance(
            nama = kos.nama,
            lokasi = kos.lokasi,
            harga = kos.harga,
            kategori = "Kos",
            deskripsi = kos.fasilitas,
            lat = kos.latitude,
            lon = kos.longitude
        )

        fm.beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit()
    }

    override fun onApplyFilter(state: FilterState) {
        val filtered = allKos.filter { item ->
            val hargaInt = item.harga.replace(".", "").toIntOrNull() ?: 0
            val minOk = if (state.minHarga > 0) hargaInt >= state.minHarga else true
            val maxOk = if (state.maxHarga > 0) hargaInt <= state.maxHarga else true
            val kamarOk = if (state.jumlahKamar > 0) item.fasilitas.contains("${state.jumlahKamar} Kamar") else true
            val jenisOk = if (state.jenisKos.isNotEmpty()) item.fasilitas.contains(state.jenisKos) else true
            val fasilitasOk = state.fasilitas.all { f -> item.fasilitas.contains(f) }

            minOk && maxOk && kamarOk && jenisOk && fasilitasOk
        }
        adapter.updateList(filtered)
    }
}

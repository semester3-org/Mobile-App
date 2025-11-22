package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var rvRekom: RecyclerView
    private lateinit var rvFavorit: RecyclerView
    private lateinit var etSearch: TextView
    private lateinit var adapterRekom: KosAdapter
    private lateinit var adapterFav: KosAdapter

    private val allRekom = mutableListOf<KosItem>()
    private val allFav = mutableListOf<KosItem>()

    private val filterVM: HomeFilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val filterButton = view.findViewById<ImageButton>(R.id.ibFilterHome)
        filterButton.setOnClickListener {
            val dialog = FilterDialogFragment()
            dialog.show(parentFragmentManager, "filterDialog")
        }

        rvRekom = view.findViewById(R.id.rvRekomendasi)
        rvFavorit = view.findViewById(R.id.rvFavoritHome)
        etSearch = view.findViewById(R.id.etSearchHome)

        rvRekom.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFavorit.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        adapterRekom = KosAdapter(emptyList()) { openDetail(it) }
        adapterFav = KosAdapter(emptyList()) { openDetail(it) }

        rvRekom.adapter = adapterRekom
        rvFavorit.adapter = adapterFav

        loadKos()

        // SEARCH
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applySearch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        filterVM.filterState.observe(viewLifecycleOwner) { state ->
            applyFilter(
                minHarga = state.minHarga,
                maxHarga = state.maxHarga,
                selectedFasilitas = state.fasilitas.toList(),
                selectedJenisKos = state.jenisKos,
                jumlahKamar = state.jumlahKamar
            )
        }


        return view
    }

    private fun loadKos() {
        ApiClient.instance.getKosList()
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {
                    if (res.isSuccessful) {
                        val data = res.body()?.data ?: emptyList()

                        allRekom.clear()
                        allFav.clear()

                        allRekom.addAll(data.take(6).map { it.toKosItem() })
                        allFav.addAll(data.shuffled().take(6).map { it.toKosItem() })

                        applySearch()
                    }
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applySearch() {
        val q = etSearch.text.toString().lowercase()

        val rekom = allRekom.filter {
            it.nama.lowercase().contains(q) || it.lokasi.lowercase().contains(q)
        }

        val fav = allFav.filter {
            it.nama.lowercase().contains(q) || it.lokasi.lowercase().contains(q)
        }

        adapterRekom.updateList(rekom)
        adapterFav.updateList(fav)
    }

    fun applyFilter(
        minHarga: Int,
        maxHarga: Int,
        selectedFasilitas: List<String>,
        selectedJenisKos: String,
        jumlahKamar: Int
    ) {

        val filteredRekom = allRekom.filter { kos ->
            kos.harga.toInt() in minHarga..maxHarga &&
                    (selectedFasilitas.isEmpty() || selectedFasilitas.all { f -> f in kos.fasilitas }) &&
                    (selectedJenisKos.isEmpty() || kos.jenisKos == selectedJenisKos) &&
                    (jumlahKamar == 0 || kos.jumlahKamar >= jumlahKamar)
        }

        val filteredFav = allFav.filter { kos ->
            kos.harga.toInt() in minHarga..maxHarga &&
                    (selectedFasilitas.isEmpty() || selectedFasilitas.all { f -> f in kos.fasilitas }) &&
                    (selectedJenisKos.isEmpty() || kos.jenisKos == selectedJenisKos) &&
                    (jumlahKamar == 0 || kos.jumlahKamar >= jumlahKamar)
        }

        adapterRekom.updateList(filteredRekom)
        adapterFav.updateList(filteredFav)
    }

    private fun openDetail(kos: KosItem) {
        val fm = parentFragmentManager
        val detail = DetailKosFragment.newInstance(
            nama = kos.nama,
            lokasi = kos.lokasi,
            harga = kos.harga,
            kategori = "Kos",
            deskripsi = kos.deskripsi
        )

        fm.beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit()
    }
}

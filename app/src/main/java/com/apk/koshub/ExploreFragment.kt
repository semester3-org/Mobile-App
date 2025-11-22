package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosCardAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreFragment : Fragment(), FilterDialogFragment.OnFilterApplied {

    private lateinit var rvExplore: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: KosCardAdapter

    private val allKos = mutableListOf<KosItemCard>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val filterButton = view.findViewById<ImageButton>(R.id.btnFilter)
        filterButton.setOnClickListener {
            FilterDialogFragment().show(parentFragmentManager, "filterDialog")
        }

        rvExplore = view.findViewById(R.id.rvExplore)
        etSearch = view.findViewById(R.id.etSearchExplore)

        rvExplore.layoutManager = GridLayoutManager(context, 2)

        adapter = KosCardAdapter(mutableListOf()) { kos ->
            openDetail(kos)
        }

        rvExplore.adapter = adapter

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

    private fun loadKos() {
        ApiClient.instance.getKosList()
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {

                    if (res.isSuccessful) {
                        val list = res.body()?.data?.map { it.toKosItemCard() } ?: emptyList()

                        allKos.clear()
                        allKos.addAll(list)

                        adapter.updateList(allKos)
                    } else {
                        Toast.makeText(requireContext(), "Error ${res.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
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
            deskripsi = kos.fasilitas
        )

        fm.beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit()
    }
    override fun onApplyFilter(state: FilterState) {
        // harga di API = STRING → convert dulu
        val filtered = allKos.filter { item ->

            val hargaInt = item.harga.replace(".", "").toIntOrNull() ?: 0

            val minOk = if (state.minHarga > 0) hargaInt >= state.minHarga else true
            val maxOk = if (state.maxHarga > 0) hargaInt <= state.maxHarga else true

            val kamarOk = if (state.jumlahKamar > 0)
                item.fasilitas.contains("${state.jumlahKamar} Kamar")
            else true

            val jenisOk = if (state.jenisKos.isNotEmpty())
                item.fasilitas.contains(state.jenisKos)
            else true

            val fasilitasOk = state.fasilitas.all { f ->
                item.fasilitas.contains(f)
            }

            minOk && maxOk && kamarOk && jenisOk && fasilitasOk
        }

        adapter.updateList(filtered)
    }

}

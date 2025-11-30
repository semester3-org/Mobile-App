package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var rvResults: RecyclerView
    private lateinit var etSearch: TextView
    private lateinit var tvRecentTitle: TextView
    private lateinit var rvRecentSearches: RecyclerView

    private lateinit var adapter: KosAdapter

    private val allKos = mutableListOf<KosItem>() // data full dari API

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        etSearch = view.findViewById(R.id.etSearch)
        tvRecentTitle = view.findViewById(R.id.tvRecentTitle)
        rvRecentSearches = view.findViewById(R.id.rvRecentSearches)

        rvResults.layoutManager = LinearLayoutManager(context)
        rvRecentSearches.layoutManager = LinearLayoutManager(context)

        adapter = KosAdapter(emptyList()) { kos ->
            openDetail(kos)
        }

        rvResults.adapter = adapter
        rvRecentSearches.adapter = adapter

        loadKosFromApi()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSearch(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun loadKosFromApi() {
        ApiClient.api.getKosList()
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {
                    if (res.isSuccessful) {
                        val data = res.body()?.data ?: emptyList()

                        allKos.clear()
                        allKos.addAll(data.map { it.toKosItem() })

                        adapter.updateList(emptyList()) // awal kosong dulu
                    } else {
                        Toast.makeText(requireContext(), "Error ${res.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterSearch(query: String) {
        if (query.isBlank()) {
            adapter.updateList(emptyList())
            tvRecentTitle.visibility = View.VISIBLE
            rvRecentSearches.visibility = View.VISIBLE
            return
        }

        val result = allKos.filter {
            it.nama.contains(query, true) ||
                    it.lokasi.contains(query, true) ||
                    it.deskripsi.contains(query, true)
        }

        adapter.updateList(result)

        tvRecentTitle.visibility = View.GONE
        rvRecentSearches.visibility = View.GONE
    }

    private fun openDetail(kos: KosItem) {
        val fragment = DetailKosFragment.newInstance(
            nama = kos.nama,
            lokasi = kos.lokasi,
            harga = kos.harga,
            kategori = "Kos",
            deskripsi = kos.deskripsi
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

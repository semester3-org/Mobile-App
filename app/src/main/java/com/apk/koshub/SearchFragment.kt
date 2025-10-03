package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.models.KosItem

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var rvRecentSearches: RecyclerView
    private lateinit var tvRecentTitle: TextView
    private lateinit var adapter: KosAdapter
    private var allSearchResults = mutableListOf<KosItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        etSearch = view.findViewById(R.id.etSearch)
        rvRecentSearches = view.findViewById(R.id.rvRecentSearches)
        tvRecentTitle = view.findViewById(R.id.tvRecentTitle)

        // Setup RecyclerView untuk recent searches (bisa ganti ke horizontal chips adapter nanti)
        rvRecentSearches.layoutManager = LinearLayoutManager(context)
        adapter = KosAdapter(emptyList()) { kos ->
            android.widget.Toast.makeText(context, "Search: ${kos.nama}", android.widget.Toast.LENGTH_SHORT).show()
        }
        rvRecentSearches.adapter = adapter

        // Dummy recent searches (nanti dari DB)
        allSearchResults.addAll(getDummySearchResults(5))
        adapter.updateList(allSearchResults)

        // Search listener (filter real-time)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSearch(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Jika search kosong, sembunyikan recent
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    tvRecentTitle.visibility = View.VISIBLE
                    rvRecentSearches.visibility = View.VISIBLE
                } else {
                    tvRecentTitle.visibility = View.GONE
                    rvRecentSearches.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun filterSearch(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(allSearchResults)
            return
        }
        val filtered = allSearchResults.filter { it.nama.contains(query, ignoreCase = true) || it.lokasi.contains(query, ignoreCase = true) }
        adapter.updateList(filtered)
    }

    private fun getDummySearchResults(count: Int): List<KosItem> {
        val list = mutableListOf<KosItem>()
        for (i in 1..count) {
            list.add(KosItem(i, "Search Kos $i", "Jember Search Area", "Rp 750.000/bulan"))
        }
        return list
    }
}
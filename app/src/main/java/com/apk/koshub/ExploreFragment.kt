package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.models.KosItem

class ExploreFragment : Fragment() {

    private lateinit var rvExplore: RecyclerView
    private lateinit var etSearchExplore: EditText
    private lateinit var adapter: KosAdapter
    private var allKos = mutableListOf<KosItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        etSearchExplore = view.findViewById(R.id.etSearchExplore)
        rvExplore = view.findViewById(R.id.rvExplore)

        // Setup RecyclerView
        rvExplore.layoutManager = LinearLayoutManager(context)
        adapter = KosAdapter(emptyList()) { kos ->
            android.widget.Toast.makeText(context, "Explore: ${kos.nama}", android.widget.Toast.LENGTH_SHORT).show()
        }
        rvExplore.adapter = adapter

        // Dummy data
        allKos.addAll(getDummyKos(10))
        adapter.updateList(allKos)

        // Search listener
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
        val filtered = allKos.filter { it.nama.contains(query, ignoreCase = true) || it.lokasi.contains(query, ignoreCase = true) }
        adapter.updateList(filtered)
    }

    private fun getDummyKos(count: Int): List<KosItem> {
        val list = mutableListOf<KosItem>()
        for (i in 1..count) {
            list.add(KosItem(i, "Kos Explore $i", "Jember, Area $i", "Rp ${700 + i * 100}.000/bulan"))
        }
        return list
    }
}
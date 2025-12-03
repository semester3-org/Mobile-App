package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageButton
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
import android.util.Log

class HomeFragment : Fragment(), FilterDialogFragment.OnFilterApplied {

    private lateinit var rvRekom: RecyclerView
    private lateinit var rvFavorit: RecyclerView
    private lateinit var etSearch: TextView
    private lateinit var adapterRekom: KosAdapter
    private lateinit var adapterFav: KosAdapter

    private val allRekom = mutableListOf<KosItem>()
    private val allFav = mutableListOf<KosItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val notifButton = view.findViewById<ImageButton>(R.id.ibNotification)
        notifButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotifFragment())
                .addToBackStack(null)
                .commit()
        }

        val filterButton = view.findViewById<ImageButton>(R.id.ibFilterHome)
        filterButton.setOnClickListener {
            val dialog = FilterDialogFragment()
            dialog.setTargetFragment(this, 0)
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

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applySearch() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun loadKos() {
        ApiClient.api.getKosList()
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

    private fun buildFacilityIds(state: FilterState): String? {
        val ids = state.fasilitas
        return if (ids.isNotEmpty()) ids.joinToString(",") else null
    }



    override fun onApplyFilter(state: FilterState) {
        Log.d("HomeFragment", "onApplyFilter: $state")

        val kosType = when (state.jenisKos.lowercase()) {
            "putra", "putri", "campur" -> state.jenisKos.lowercase()
            else -> null
        }
        val minPrice = state.minHarga.takeIf { it > 0 }
        val maxPrice = state.maxHarga.takeIf { it > 0 }
        val availableOnly = if (state.jumlahKamar > 0) 1 else null
        val facilityIds = buildFacilityIds(state)

        Log.d("ExploreFragment", "filter facilityIds = $facilityIds")

        ApiClient.api.getFilteredKos(

            kosType = kosType,
            availableOnly = availableOnly,
            minPrice = minPrice,
            maxPrice = maxPrice,
            facilityIds = facilityIds


        ).enqueue(object : Callback<KosResponse> {
            override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {
                val body = res.body()
                if (res.isSuccessful && body?.isSuccess == true) {
                    val list = body.data.map { it.toKosItem() }

                    allRekom.clear()
                    allFav.clear()
                    allRekom.addAll(list.take(6))
                    allFav.addAll(list.shuffled().take(6))

                    applySearch()
                } else {
                    Toast.makeText(requireContext(), "Filter gagal: ${body?.message ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openDetail(kos: KosItem) {
        val fm = parentFragmentManager
        val detail = DetailKosFragment.newInstance(kosId = kos.id)

        fm.beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit()
    }
}

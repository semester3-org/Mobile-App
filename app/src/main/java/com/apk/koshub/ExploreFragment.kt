package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
import android.util.Log

class ExploreFragment : Fragment(), FilterDialogFragment.OnFilterApplied {

    private enum class Category { PUTRA, PUTRI, CAMPUR, EXCLUSIVE }

    private lateinit var rvExplore: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: KosCardAdapter
    private lateinit var db: DatabaseHelper
    private lateinit var pref: SharedPrefHelper

    // data full dari kos_list (approved only)
    private val allKosFull = mutableListOf<KosItemCard>()
    // data yang sedang dipakai di layar (bisa full, bisa hasil dialog filter)
    private val allKosView = mutableListOf<KosItemCard>()

    private var selectedCategory: Category? = null
    private var lastDialogFilter: FilterState? = null
    private var isDialogFilterActive: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        view.findViewById<ImageButton>(R.id.btnFilter).setOnClickListener {
            val dialog = FilterDialogFragment()
            dialog.setTargetFragment(this, 0)
            dialog.show(parentFragmentManager, "filterDialog")
        }

        view.findViewById<LinearLayout>(R.id.btnKosPutra).setOnClickListener { toggleCategory(Category.PUTRA) }
        view.findViewById<LinearLayout>(R.id.btnKosPutri).setOnClickListener { toggleCategory(Category.PUTRI) }
        view.findViewById<LinearLayout>(R.id.btnKosCampur).setOnClickListener { toggleCategory(Category.CAMPUR) }
        view.findViewById<LinearLayout>(R.id.btnKosExclusive).setOnClickListener { toggleCategory(Category.EXCLUSIVE) }

        rvExplore = view.findViewById(R.id.rvExplore)
        etSearch = view.findViewById(R.id.etSearchExplore)

        db = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        val glm = GridLayoutManager(requireContext(), 2)
        glm.isAutoMeasureEnabled = true
        rvExplore.layoutManager = glm
        rvExplore.setHasFixedSize(false)
        rvExplore.isNestedScrollingEnabled = false

        adapter = KosCardAdapter(mutableListOf()) { kos -> openDetail(kos) }
        rvExplore.adapter = adapter

        etSearch.setText("")
        loadKos()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyExploreFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        if (pref.isLoggedIn()) refreshFavorites()
    }

    private fun toggleCategory(cat: Category) {
        // tiap kali user main kategori, anggap keluar dari mode dialog filter
        isDialogFilterActive = false
        allKosView.clear()
        allKosView.addAll(allKosFull)

        selectedCategory = if (selectedCategory == cat) null else cat
        applyExploreFilters()
    }

    private fun loadKos() {
        ApiClient.api.getKosList()
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {
                    val kosBody = res.body()
                    if (res.isSuccessful && kosBody?.isSuccess == true) {
                        val list = kosBody.data.map { it.toKosItemCard() }

                        allKosFull.clear()
                        allKosFull.addAll(list)

                        // kalau lagi gak ada filter dialog aktif, tampilan ikut full
                        if (!isDialogFilterActive) {
                            allKosView.clear()
                            allKosView.addAll(allKosFull)
                        }

                        if (pref.isLoggedIn()) refreshFavorites() else applyExploreFilters()
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
        val user = db.getUser() ?: run {
            applyExploreFilters()
            return
        }

        ApiClient.api.getFavoriteKos(userId = user.id)
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(call: Call<KosResponse>, favRes: Response<KosResponse>) {
                    val favBody = favRes.body()

                    val favoriteIds =
                        if (favRes.isSuccessful && favBody?.isSuccess == true) {
                            favBody.data.map { it.id }.toSet()
                        } else emptySet()

                    allKosFull.forEach { item ->
                        item.isFavorite = favoriteIds.contains(item.id)
                    }
                    allKosView.forEach { item ->
                        item.isFavorite = favoriteIds.contains(item.id)
                    }

                    applyExploreFilters()
                }

                override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                    applyExploreFilters()
                }
            })
    }

    private fun applyExploreFilters() {
        val q = etSearch.text.toString().trim()

        var list = allKosView.toList()

        list = when (selectedCategory) {
            Category.PUTRA   -> list.filter { it.jenisKos?.equals("putra", true) == true }
            Category.PUTRI   -> list.filter { it.jenisKos?.equals("putri", true) == true }
            Category.CAMPUR  -> list.filter { it.jenisKos?.equals("campur", true) == true }
            Category.EXCLUSIVE -> list.filter { it.priceMonthly >= 500_000 }
            null -> list
        }

        if (q.isNotEmpty()) {
            list = list.filter {
                it.nama.contains(q, true) || it.lokasi.contains(q, true)
            }
        }

        adapter.updateList(list)
    }

    private fun openDetail(kos: KosItemCard) {
        val detail = DetailKosFragment.newInstance(kosId = kos.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit()
    }

    private fun buildFacilityIds(state: FilterState): String? {
        val ids = state.fasilitas
        return if (ids.isNotEmpty()) ids.joinToString(",") else null
    }

    override fun onApplyFilter(state: FilterState) {
        lastDialogFilter = state
        isDialogFilterActive = true
        Log.d("ExploreFragment", "onApplyFilter: $state")

        val kosType = when (state.jenisKos.lowercase()) {
            "putra", "putri", "campur" -> state.jenisKos.lowercase()
            else -> null
        }
        val minPrice = state.minHarga.takeIf { it > 0 }
        val maxPrice = state.maxHarga.takeIf { it > 0 }
        val availableOnly = if (state.jumlahKamar > 0) 1 else null
        val facilityIds = buildFacilityIds(state)

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
                    val list = body.data.map { it.toKosItemCard() }

                    allKosView.clear()
                    allKosView.addAll(list)

                    applyExploreFilters()
                } else {
                    Toast.makeText(requireContext(), "Filter gagal: ${body?.message ?: "Unknown"}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

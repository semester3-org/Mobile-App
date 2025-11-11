package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.models.KosItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.RangeSlider
import kotlin.math.max
import kotlin.math.min

class HomeFragment : Fragment() {

    private lateinit var rvRekomendasi: RecyclerView
    private lateinit var rvFavoritHome: RecyclerView
    private lateinit var etSearchHome: TextView
    private lateinit var btnFilter: ImageView
    private lateinit var adapterRekomendasi: KosAdapter
    private lateinit var adapterFavorit: KosAdapter

    private val allRekomendasi = mutableListOf<KosItem>()
    private val allFavorit = mutableListOf<KosItem>()

    // ===== STATE FILTER =====
    private var minHargaFilter: Int? = null       // Rp
    private var maxHargaFilter: Int? = null
    private var jumlahKamarFilter: Int? = null    // 1,2,3,4(=4+)
    private var jenisKosFilter: String? = null    // "Putra","Putri","Campur"
    private val fasilitasFilter = mutableSetOf<String>() // "WiFi","AC",...

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        etSearchHome = view.findViewById(R.id.etSearchHome)
        rvRekomendasi = view.findViewById(R.id.rvRekomendasi)
        rvFavoritHome = view.findViewById(R.id.rvFavoritHome)
        btnFilter = view.findViewById(R.id.ibFilterHome)
        val ibSearchIcon = view.findViewById<ImageButton>(R.id.ibSearchIcon)

        setupRecyclerViews()

        // dummy data
        allRekomendasi.addAll(getDummyKos("Rekomendasi", 6))
        allFavorit.addAll(getDummyKos("Favorit", 5))

        applyAllFiltersAndSearch()

        // live search
        etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyAllFiltersAndSearch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ibSearchIcon.setOnClickListener {
            applyAllFiltersAndSearch()
        }

        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        return view
    }

    // ================== LIST & DETAIL ==================

    private fun setupRecyclerViews() {
        rvRekomendasi.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFavoritHome.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        adapterRekomendasi = KosAdapter(emptyList()) { kos ->
            openDetailFromKosItem(kos)
        }
        adapterFavorit = KosAdapter(emptyList()) { kos ->
            openDetailFromKosItem(kos)
        }

        rvRekomendasi.adapter = adapterRekomendasi
        rvFavoritHome.adapter = adapterFavorit
    }

    private fun openDetailFromKosItem(kos: KosItem) {
        val detailFragment = DetailKosFragment.newInstance(
            nama = kos.nama,
            lokasi = kos.lokasi,
            harga = kos.harga,
            kategori = "Kos",
            deskripsi = "" // nanti ganti dari data asli
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    // ================== FILTER BOTTOM SHEET ==================

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)
        dialog.setContentView(view)

        // close
        view.findViewById<ImageView>(R.id.ivCloseFilter)?.setOnClickListener {
            dialog.dismiss()
        }

        // ----- Harga (RangeSlider) -----
        val rangeSlider = view.findViewById<RangeSlider>(R.id.rsHarga)
        val tvMinHarga = view.findViewById<TextView>(R.id.tvMinHarga)
        val tvMaxHarga = view.findViewById<TextView>(R.id.tvMaxHarga)

        val defaultMin = 400_000
        val defaultMax = 1_000_000

        val currentMin = (minHargaFilter ?: defaultMin).toFloat()
        val currentMax = (maxHargaFilter ?: defaultMax).toFloat()

        rangeSlider?.let { slider ->
            val minVal = max(slider.valueFrom, min(currentMin, slider.valueTo))
            val maxVal = min(slider.valueTo, max(currentMax, minVal + 1f))

            slider.values = listOf(minVal, maxVal)
            tvMinHarga?.text = formatRupiah(minVal.toInt())
            tvMaxHarga?.text = formatRupiah(maxVal.toInt())

            slider.addOnChangeListener { s, _, _ ->
                val values = s.values
                tvMinHarga?.text = formatRupiah(values[0].toInt())
                tvMaxHarga?.text = formatRupiah(values[1].toInt())
            }
        }

        // ----- Jumlah Kamar chips -----
        val btnK1 = view.findViewById<TextView>(R.id.btnKamar1)
        val btnK2 = view.findViewById<TextView>(R.id.btnKamar2)
        val btnK3 = view.findViewById<TextView>(R.id.btnKamar3)
        val btnK4 = view.findViewById<TextView>(R.id.btnKamar4Plus)

        val kamarButtons = listOf(
            1 to btnK1,
            2 to btnK2,
            3 to btnK3,
            4 to btnK4
        )

        fun updateKamarUI(selected: Int?) {
            kamarButtons.forEach { (value, tv) ->
                tv ?: return@forEach
                if (selected == value) setChipFilled(tv) else setChipOutline(tv)
            }
        }

        updateKamarUI(jumlahKamarFilter)

        kamarButtons.forEach { (value, tv) ->
            tv?.setOnClickListener {
                jumlahKamarFilter = if (jumlahKamarFilter == value) null else value
                updateKamarUI(jumlahKamarFilter)
            }
        }

        // ----- Jenis Kos (expand/collapse + chips) -----
        val rowJenisKos = view.findViewById<View>(R.id.rowJenisKos)
        val layoutJenisKosContent = view.findViewById<View>(R.id.layoutJenisKosContent)
        val ivJenisKosToggle = view.findViewById<ImageView>(R.id.ivJenisKosToggle)

        val btnPutra = view.findViewById<TextView>(R.id.btnPutra)
        val btnPutri = view.findViewById<TextView>(R.id.btnPutri)
        val btnCampur = view.findViewById<TextView>(R.id.btnCampur)

        fun updateJenisKosUI(selected: String?) {
            fun style(tv: TextView?, active: Boolean) {
                if (tv == null) return
                if (active) setChipFilled(tv) else setChipOutline(tv)
            }
            style(btnPutra, selected == "Putra")
            style(btnPutri, selected == "Putri")
            style(btnCampur, selected == "Campur")
        }

        updateJenisKosUI(jenisKosFilter)

        btnPutra?.setOnClickListener {
            jenisKosFilter = if (jenisKosFilter == "Putra") null else "Putra"
            updateJenisKosUI(jenisKosFilter)
        }
        btnPutri?.setOnClickListener {
            jenisKosFilter = if (jenisKosFilter == "Putri") null else "Putri"
            updateJenisKosUI(jenisKosFilter)
        }
        btnCampur?.setOnClickListener {
            jenisKosFilter = if (jenisKosFilter == "Campur") null else "Campur"
            updateJenisKosUI(jenisKosFilter)
        }

        // default: Jenis Kos kelihatan
        layoutJenisKosContent?.visibility = View.GONE
        ivJenisKosToggle?.setImageResource(R.drawable.ic_arrow_down)

        rowJenisKos?.setOnClickListener {
            if (layoutJenisKosContent?.visibility == View.VISIBLE) {
                layoutJenisKosContent.visibility = View.GONE
                ivJenisKosToggle?.setImageResource(R.drawable.ic_arrow_down)
            } else {
                layoutJenisKosContent?.visibility = View.VISIBLE
                ivJenisKosToggle?.setImageResource(R.drawable.ic_arrow_up)
            }
        }

        // ----- Fasilitas (expand/collapse + chips) -----
        val rowFasilitas = view.findViewById<View>(R.id.rowFasilitas)
        val layoutFasilitasContent = view.findViewById<View>(R.id.layoutFasilitasContent)
        val ivFasilitasToggle = view.findViewById<ImageView>(R.id.ivFasilitasToggle)

        // chip fasilitas: pastikan id ini ada di dialog_filter.xml sebagai TextView
        val chipWifi = view.findViewById<TextView>(R.id.chipWifi)
        val chipAc = view.findViewById<TextView>(R.id.chipAc)
        val chipKmDalam = view.findViewById<TextView>(R.id.chipKmDalam)
        val chipMeja = view.findViewById<TextView>(R.id.chipMeja)
        val chipLemari = view.findViewById<TextView>(R.id.chipLemari)

        val fasilitasChips = listOfNotNull(
            chipWifi?.let { "WiFi" to it },
            chipAc?.let { "AC" to it },
            chipKmDalam?.let { "KM Dalam" to it },
            chipMeja?.let { "Meja" to it },
            chipLemari?.let { "Lemari" to it }
        )

        fun refreshFasilitasChips() {
            fasilitasChips.forEach { (name, tv) ->
                val selected = fasilitasFilter.contains(name)
                if (selected) setChipFilled(tv) else setChipOutline(tv)
            }
        }

        fasilitasChips.forEach { (name, tv) ->
            tv.setOnClickListener {
                if (fasilitasFilter.contains(name)) {
                    fasilitasFilter.remove(name)
                } else {
                    fasilitasFilter.add(name)
                }
                refreshFasilitasChips()
            }
        }

        // default: fasilitas hidden
        layoutFasilitasContent?.visibility = View.GONE
        ivFasilitasToggle?.setImageResource(R.drawable.ic_arrow_down)
        refreshFasilitasChips()

        rowFasilitas?.setOnClickListener {
            if (layoutFasilitasContent?.visibility == View.VISIBLE) {
                layoutFasilitasContent.visibility = View.GONE
                ivFasilitasToggle?.setImageResource(R.drawable.ic_arrow_down)
            } else {
                layoutFasilitasContent?.visibility = View.VISIBLE
                ivFasilitasToggle?.setImageResource(R.drawable.ic_arrow_up)
            }
        }

        // ----- Reset -----
        val btnReset = view.findViewById<TextView>(R.id.btnResetFilter)
        btnReset?.setOnClickListener {
            minHargaFilter = null
            maxHargaFilter = null
            jumlahKamarFilter = null
            jenisKosFilter = null
            fasilitasFilter.clear()

            applyAllFiltersAndSearch()
            dialog.dismiss()
        }

        // ----- Terapkan -----
        val btnApply = view.findViewById<TextView>(R.id.btnTerapkanFilter)
        btnApply?.setOnClickListener {
            rangeSlider?.let { s ->
                val values = s.values
                if (values.size >= 2) {
                    minHargaFilter = values[0].toInt()
                    maxHargaFilter = values[1].toInt()
                }
            }

            applyAllFiltersAndSearch()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ================== FILTER CORE ==================

    private fun applyAllFiltersAndSearch() {
        val query = etSearchHome.text?.toString().orEmpty().lowercase()

        val (rekomBase, favoritBase) = getFilteredLists()

        val rekom = if (query.isBlank()) {
            rekomBase
        } else {
            rekomBase.filter { it.matchesQuery(query) }
        }

        val favorit = if (query.isBlank()) {
            favoritBase
        } else {
            favoritBase.filter { it.matchesQuery(query) }
        }

        adapterRekomendasi.updateList(rekom)
        adapterFavorit.updateList(favorit)
    }

    private fun getFilteredLists(): Pair<List<KosItem>, List<KosItem>> {
        var rekom = allRekomendasi.toList()
        var favorit = allFavorit.toList()

        rekom = rekom.filterByHarga()
        favorit = favorit.filterByHarga()

        jenisKosFilter?.let { jenis ->
            rekom = rekom.filter { it.nama.contains(jenis, true) }
            favorit = favorit.filter { it.nama.contains(jenis, true) }
        }

        jumlahKamarFilter?.let { jumlah ->
            rekom = rekom.filter { matchJumlahKamarDummy(it.nama, jumlah) }
            favorit = favorit.filter { matchJumlahKamarDummy(it.nama, jumlah) }
        }

        if (fasilitasFilter.isNotEmpty()) {
            rekom = rekom.filter { it.matchesFasilitasDummy(fasilitasFilter) }
            favorit = favorit.filter { it.matchesFasilitasDummy(fasilitasFilter) }
        }

        return Pair(rekom, favorit)
    }

    // === extension dummy: adjust nanti kalau data asli udah proper ===

    private fun List<KosItem>.filterByHarga(): List<KosItem> {
        val minF = minHargaFilter
        val maxF = maxHargaFilter
        if (minF == null && maxF == null) return this

        return this.filter { item ->
            val angka = item.harga
                .replace("Rp", "", ignoreCase = true)
                .replace(".", "")
                .replace(",", "")
                .filter { it.isDigit() }
                .toIntOrNull() ?: return@filter true

            val passMin = minF?.let { angka >= it } ?: true
            val passMax = maxF?.let { angka <= it } ?: true
            passMin && passMax
        }
    }

    private fun KosItem.matchesQuery(q: String): Boolean {
        return nama.lowercase().contains(q) || lokasi.lowercase().contains(q)
    }

    private fun matchJumlahKamarDummy(nama: String, jumlah: Int): Boolean {
        return if (jumlah == 4) {
            nama.contains("4", true)
        } else {
            nama.contains(jumlah.toString(), true)
        }
    }

    private fun KosItem.matchesFasilitasDummy(selected: Set<String>): Boolean {
        if (selected.isEmpty()) return true
        val text = (nama + " " + lokasi + " " + harga).lowercase()
        // sementara: cek kata kunci di text; nanti ganti pakai field fasilitas beneran
        return selected.all { f -> text.contains(f.lowercase()) }
    }

    // ================== UI HELPERS ==================

    private fun setChipFilled(tv: TextView) {
        tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_apply)
        tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun setChipOutline(tv: TextView) {
        tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_reset)
        tv.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.green_primary
            )
        )
    }

    private fun formatRupiah(value: Int): String {
        val s = String.format("%,d", value).replace(",", ".")
        return "Rp $s"
    }

    // ================== DUMMY DATA ==================

    private fun getDummyKos(kategori: String, count: Int): List<KosItem> {
        val list = mutableListOf<KosItem>()
        for (i in 1..count) {
            list.add(
                KosItem(
                    id = i,
                    nama = "$kategori Kos $i",
                    lokasi = "Jember Area",
                    harga = "Rp ${600_000 + (i * 50_000)}/bulan",
                    gambar = "https://picsum.photos/300/200?random=$i"
                )
            )
        }
        return list
    }
}

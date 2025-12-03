package com.apk.koshub.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.apk.koshub.R
import com.apk.koshub.api.ApiClient
import com.apk.koshub.models.FacilityDto
import com.apk.koshub.models.FilterState
import com.apk.koshub.models.FilterStorage
import kotlinx.coroutines.launch
import com.google.android.flexbox.FlexboxLayout


class FilterDialogFragment : DialogFragment() {

    interface OnFilterApplied {
        fun onApplyFilter(state: FilterState)
    }

    private lateinit var storage: FilterStorage
    private lateinit var state: FilterState
    private var callback: OnFilterApplied? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = targetFragment as? OnFilterApplied
    }

    private fun setupRupiahFormatter(edit: EditText) {
        edit.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString() ?: ""
                if (str == current) return

                edit.removeTextChangedListener(this)

                try {
                    val clean = str.replace(".", "")
                    if (clean.isNotEmpty()) {
                        val num = clean.toLong()
                        val formatted = String.format("%,d", num).replace(",", ".")
                        current = formatted
                        edit.setText(formatted)
                        edit.setSelection(formatted.length)
                    } else {
                        current = ""
                    }
                } catch (_: Exception) {
                }

                edit.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun formatRupiah(value: Int): String {
        if (value <= 0) return ""
        return String.format("%,d", value).replace(",", ".")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_filter, container, false)

        storage = FilterStorage(requireContext())
        state = storage.load()

        val etMin = view.findViewById<EditText>(R.id.etMinHarga)
        val etMax = view.findViewById<EditText>(R.id.etMaxHarga)

        etMin.setText(formatRupiah(state.minHarga))
        etMax.setText(formatRupiah(state.maxHarga))

        setupRupiahFormatter(etMin)
        setupRupiahFormatter(etMax)

        // ============ JUMLAH KAMAR ============
        val kamarMap = mapOf(
            1 to view.findViewById<TextView>(R.id.btnKamar1),
            2 to view.findViewById<TextView>(R.id.btnKamar2),
            3 to view.findViewById<TextView>(R.id.btnKamar3),
            4 to view.findViewById<TextView>(R.id.btnKamar4Plus),
        )

        fun updateKamarUI() {
            kamarMap.forEach { (value, btn) ->
                btn.setBackgroundResource(
                    if (state.jumlahKamar == value)
                        R.drawable.bg_filter_chip_filled
                    else
                        R.drawable.bg_filter_chip_outline
                )
            }
        }

        kamarMap.forEach { (value, btn) ->
            btn.setOnClickListener {
                state = if (state.jumlahKamar == value) {
                    state.copy(jumlahKamar = 0)   // klik lagi -> batal
                } else {
                    state.copy(jumlahKamar = value)
                }
                updateKamarUI()
            }
        }
        updateKamarUI()

        // ============ FASILITAS (DINAMIS) ============
        val contentFasilitas = view.findViewById<FlexboxLayout>(R.id.layoutFasilitasContent)
        val arrowFasilitas = view.findViewById<ImageView>(R.id.ivFasilitasToggle)

        contentFasilitas.visibility = View.VISIBLE
        arrowFasilitas.rotation = 180f

        fun toggleFasilitas() {
            val isVisible = contentFasilitas.visibility == View.VISIBLE
            if (isVisible) {
                contentFasilitas.visibility = View.GONE
                arrowFasilitas.animate().rotation(0f).setDuration(200).start()
            } else {
                contentFasilitas.visibility = View.VISIBLE
                arrowFasilitas.animate().rotation(180f).setDuration(200).start()
            }
        }

        view.findViewById<View>(R.id.rowFasilitas).setOnClickListener { toggleFasilitas() }

        // id fasilitas -> chip TextView
        val fasilitasChipMap = mutableMapOf<Int, TextView>()

        fun updateFasilitasUI() {
            fasilitasChipMap.forEach { (id, chip) ->
                val selected = state.fasilitas.contains(id)
                chip.setBackgroundResource(
                    if (selected) R.drawable.bg_filter_chip_filled
                    else R.drawable.bg_filter_chip_outline
                )
                chip.setTextColor(
                    if (selected) requireContext().getColor(android.R.color.white)
                    else requireContext().getColor(android.R.color.black)
                )
            }
        }

        fun dp(v: Int): Int =
            (v * resources.displayMetrics.density).toInt()

        fun createFacilityChip(f: FacilityDto): TextView {
            val chip = TextView(requireContext())
            chip.text = f.name
            chip.textSize = 10f
            chip.setPadding(dp(18), dp(8), dp(18), dp(8))
            chip.height = dp(36)
            chip.gravity = Gravity.CENTER
            chip.setBackgroundResource(R.drawable.bg_filter_chip_outline)

            val lp = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                dp(36)
            )
            if (contentFasilitas.childCount > 0) {
                lp.setMargins(dp(6), 0, 0, dp(6))
            }
            chip.layoutParams = lp

            // icon dari DB (kalau ada)
            val iconRes = mapFacilityIconToDrawable(f.icon, f.name)
            if (iconRes != null) {
                val drawable = requireContext().getDrawable(iconRes)
                drawable?.setBounds(0, 0, dp(16), dp(16))
                chip.setCompoundDrawablesRelative(drawable, null, null, null)
                chip.compoundDrawablePadding = dp(6)
            }

            chip.setOnClickListener {
                val newSet = state.fasilitas.toMutableSet()
                if (newSet.contains(f.id)) newSet.remove(f.id) else newSet.add(f.id)
                state = state.copy(fasilitas = newSet)
                updateFasilitasUI()
            }

            return chip
        }

        // load fasilitas dari backend pakai coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.api.getFacilities()
                if (res.status == "success") {
                    contentFasilitas.removeAllViews()
                    fasilitasChipMap.clear()

                    res.data.forEach { f ->
                        val chip = createFacilityChip(f)
                        fasilitasChipMap[f.id] = chip
                        contentFasilitas.addView(chip)
                    }

                    // sync pilihan lama yang sudah disimpan di state
                    updateFasilitasUI()
                } else {
                    Log.e("FilterDialog", "getFacilities error: ${res.message}")
                }
            } catch (e: Exception) {
                Log.e("FilterDialog", "getFacilities exception: ${e.message}", e)
            }
        }

        // ============ JENIS KOS ============
        val contentJenis = view.findViewById<LinearLayout>(R.id.layoutJenisKosContent)
        val arrowJenis = view.findViewById<ImageView>(R.id.ivJenisKosToggle)

        contentJenis.visibility = View.VISIBLE
        arrowJenis.rotation = 180f

        fun toggleJenis() {
            val isVisible = contentJenis.visibility == View.VISIBLE
            if (isVisible) {
                contentJenis.visibility = View.GONE
                arrowJenis.animate().rotation(0f).setDuration(200).start()
            } else {
                contentJenis.visibility = View.VISIBLE
                arrowJenis.animate().rotation(180f).setDuration(200).start()
            }
        }

        view.findViewById<View>(R.id.rowJenisKos).setOnClickListener { toggleJenis() }

        val jenisMap = mapOf(
            "Putra" to view.findViewById<TextView>(R.id.btnPutra),
            "Putri" to view.findViewById<TextView>(R.id.btnPutri),
            "Campur" to view.findViewById<TextView>(R.id.btnCampur),
        )

        fun updateJenisUI() {
            jenisMap.forEach { (key, btn) ->
                btn.setBackgroundResource(
                    if (state.jenisKos == key)
                        R.drawable.bg_filter_chip_filled
                    else
                        R.drawable.bg_filter_chip_outline
                )
            }
        }

        jenisMap.forEach { (key, btn) ->
            btn.setOnClickListener {
                state = if (state.jenisKos == key) {
                    state.copy(jenisKos = "")
                } else {
                    state.copy(jenisKos = key)
                }
                updateJenisUI()
            }
        }
        updateJenisUI()

        // ============ RESET ============
        view.findViewById<TextView>(R.id.btnResetFilter).setOnClickListener {
            state = FilterState()
            storage.clear()

            etMin.setText("")
            etMax.setText("")

            updateKamarUI()
            updateFasilitasUI()
            updateJenisUI()
        }

        // ============ APPLY ============
        view.findViewById<TextView>(R.id.btnTerapkanFilter).setOnClickListener {
            val minH = etMin.text.toString().replace(".", "").toIntOrNull() ?: 0
            val maxH = etMax.text.toString().replace(".", "").toIntOrNull() ?: 0

            state = state.copy(minHarga = minH, maxHarga = maxH)
            storage.save(state)

            Log.d("FilterDialog", "Apply clicked, callback=$callback, state=$state")
            callback?.onApplyFilter(state)
            dismiss()
        }

        view.findViewById<ImageView>(R.id.ivCloseFilter).setOnClickListener { dismiss() }

        return view
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        window.setBackgroundDrawableResource(R.drawable.bg_dialog_round)
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
    }

    // ====== ICON MAPPER FASILITAS ======
    private fun mapFacilityIconToDrawable(icon: String?, name: String): Int? {
        val fa = icon?.trim()?.lowercase().orEmpty()
        val n = name.trim().lowercase()

        // prioritas pakai icon code dari server (kalau ada)
        when (fa) {
            "fa-wifi" -> return R.drawable.ic_wifi
            "fa-fan", "fa-snowflake" -> return R.drawable.ic_ac
            "fa-shower" -> return R.drawable.ic_shower
            "fa-plug" -> return R.drawable.ic_plug
            "fa-wind" -> return R.drawable.ic_laundry
            "fa-fridge" -> return R.drawable.ic_fridge
            "fa-fingerprint" -> return R.drawable.ic_fingerprint
            "fa-warehouse" -> return R.drawable.ic_garage
            "fa-bicycle" -> return R.drawable.ic_parking
            "fa-user-shield" -> return R.drawable.ic_security
            "fa-video", "fa-camera" -> return R.drawable.ic_cctv
            "fa-bed", "fa-couch" -> return R.drawable.ic_bed
            "fa-toilet" -> return R.drawable.ic_toilet
            "fa-dresser" -> return R.drawable.ic_lemari
            "fa-chair" -> return R.drawable.ic_chair
            "fa-drycleaning" -> return R.drawable.ic_drycleaning
        }

        // fallback pakai nama fasilitas
        return when {
            n.contains("wifi") || n.contains("wi-fi") ->
                R.drawable.ic_wifi

            n == "ac" || n.contains(" ac") || n.contains("ac ") || n.contains("air conditioner") ->
                R.drawable.ic_ac

            n.contains("air bersih") || n.contains("shower") ->
                R.drawable.ic_shower

            n.contains("listrik") || n.contains("free listrik") || n.contains("plug") || n.contains(
                "colokan"
            ) ->
                R.drawable.ic_plug

            n.contains("laundry") ->
                R.drawable.ic_laundry

            n.contains("fingerprint") ->
                R.drawable.ic_fingerprint

            n.contains("cctv") ->
                R.drawable.ic_cctv

            n.contains("security") || n.contains("satpam") ->
                R.drawable.ic_security

            n.contains("parkir luas") || n.contains("garasi") ->
                R.drawable.ic_garage

            n.contains("parkir motor") || n.contains("parkir mobil") || n.contains("parkir") ->
                R.drawable.ic_parking

            n.contains("kasur") || n.contains("bed") ->
                R.drawable.ic_bed

            n.contains("meja") ->
                R.drawable.ic_meja

            n.contains("lemari") ->
                R.drawable.ic_lemari

            n.contains("kamar mandi") || n == "km" || n.contains("toilet") || n.contains("wc") ->
                R.drawable.ic_km_dalam

            else -> null
        }
    }
}

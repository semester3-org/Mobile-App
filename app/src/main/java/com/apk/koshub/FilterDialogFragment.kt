package com.apk.koshub.fragments

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.apk.koshub.R
import com.apk.koshub.models.FilterState
import com.apk.koshub.models.FilterStorage
import android.view.Gravity
import android.widget.LinearLayout
import android.text.TextWatcher
import android.text.Editable

class FilterDialogFragment : DialogFragment() {

    interface OnFilterApplied {
        fun onApplyFilter(state: FilterState)
    }

    private lateinit var storage: FilterStorage
    private lateinit var state: FilterState
    private var callback: OnFilterApplied? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = parentFragment as? OnFilterApplied
    }

    // ============================
    // FORMATTER YG AMAN & ANTI CRASH
    // ============================
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
                        val formatted =
                            String.format("%,d", num).replace(",", ".")

                        current = formatted
                        edit.setText(formatted)
                        edit.setSelection(formatted.length)
                    } else {
                        current = ""
                    }
                } catch (_: Exception) {
                    // ignore biar ga crash
                }

                edit.addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Format angka float ke string rupiah
    private fun formatRupiah(value: Int): String {
        if (value <= 0) return ""
        return String.format("%,d", value).replace(",", ".")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_filter, container, false)

        storage = FilterStorage(requireContext())
        state = storage.load()

        val etMin = view.findViewById<EditText>(R.id.etMinHarga)
        val etMax = view.findViewById<EditText>(R.id.etMaxHarga)

        // SET VALUE DARI STATE → AUTO FORMAT
        etMin.setText(formatRupiah(state.minHarga))
        etMax.setText(formatRupiah(state.maxHarga))

        setupRupiahFormatter(etMin)
        setupRupiahFormatter(etMax)

        // ========================= KAMAR =========================
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
                state = state.copy(jumlahKamar = value)
                updateKamarUI()
            }
        }

        updateKamarUI()

        // ========================= FASILITAS =========================
        val contentFasilitas = view.findViewById<LinearLayout>(R.id.layoutFasilitasContent)
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

        view.findViewById<View>(R.id.rowFasilitas).setOnClickListener {
            toggleFasilitas()
        }

        val fasilitasMap = mapOf(
            "WiFi" to view.findViewById<TextView>(R.id.chipWifi),
            "AC" to view.findViewById<TextView>(R.id.chipAc),
            "KM Dalam" to view.findViewById<TextView>(R.id.chipKmDalam),
            "Meja" to view.findViewById<TextView>(R.id.chipMeja),
            "Lemari" to view.findViewById<TextView>(R.id.chipLemari),
        )

        fun updateFasilitasUI() {
            fasilitasMap.forEach { (key, chip) ->
                chip.setBackgroundResource(
                    if (state.fasilitas.contains(key))
                        R.drawable.bg_filter_chip_filled
                    else
                        R.drawable.bg_filter_chip_outline
                )
            }
        }

        fasilitasMap.forEach { (key, chip) ->
            chip.setOnClickListener {
                val newSet = state.fasilitas.toMutableSet()
                if (key in newSet) newSet.remove(key) else newSet.add(key)

                state = state.copy(fasilitas = newSet)
                updateFasilitasUI()
            }
        }

        updateFasilitasUI()

        // ========================= JENIS KOS =========================
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

        view.findViewById<View>(R.id.rowJenisKos).setOnClickListener {
            toggleJenis()
        }

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
                state = state.copy(jenisKos = key)
                updateJenisUI()
            }
        }

        updateJenisUI()

        // ========================= RESET =========================
        view.findViewById<TextView>(R.id.btnResetFilter).setOnClickListener {
            state = FilterState()
            storage.clear()

            etMin.setText("")
            etMax.setText("")

            updateKamarUI()
            updateFasilitasUI()
            updateJenisUI()
        }

        // ========================= APPLY =========================
        view.findViewById<TextView>(R.id.btnTerapkanFilter).setOnClickListener {
            val minH = etMin.text.toString().replace(".", "").toIntOrNull() ?: 0
            val maxH = etMax.text.toString().replace(".", "").toIntOrNull() ?: 0

            state = state.copy(minHarga = minH, maxHarga = maxH)

            storage.save(state)
            callback?.onApplyFilter(state)
            dismiss()
        }

        view.findViewById<ImageView>(R.id.ivCloseFilter).setOnClickListener {
            dismiss()
        }

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
}

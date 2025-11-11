package com.apk.koshub.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.apk.koshub.R

class DetailKosFragment : Fragment(R.layout.item_detail_kos_card) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nama = arguments?.getString(ARG_NAMA).orEmpty()
        val lokasi = arguments?.getString(ARG_LOKASI).orEmpty()
        val harga = arguments?.getString(ARG_HARGA).orEmpty()
        val kategori = arguments?.getString(ARG_KATEGORI).orEmpty()
        val deskripsiArg = arguments?.getString(ARG_DESKRIPSI).orEmpty()

        val tvNamaKos = view.findViewById<TextView>(R.id.tvNamaKos)
        val tvLokasi = view.findViewById<TextView>(R.id.tvAlamat)
        val tvHarga = view.findViewById<TextView>(R.id.tvHarga)
        val tvKategori = view.findViewById<TextView>(R.id.tvKategoriKos)
        val tvDeskripsi = view.findViewById<TextView>(R.id.tvDeskripsi)
        val tvReadMore = view.findViewById<TextView>(R.id.tvReadMore)
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)

        // set basic info
        tvNamaKos.text = nama
        tvLokasi.text = lokasi
        tvHarga.text = harga
        tvKategori.text = if (kategori.isNotBlank()) kategori else "Kos"

        // pakai deskripsi dari args kalau ada, kalau nggak pakai text default dari XML (kalau ada)
        val fullText = if (deskripsiArg.isNotBlank()) {
            deskripsiArg
        } else {
            tvDeskripsi.text.toString()
        }

        tvDeskripsi.text = fullText

        // tombol back
        ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ====== LOGIC BACA SELENGKAPNYA ======
        var expanded = false

        if (fullText.length > 150) {
            tvDeskripsi.text = fullText.take(150) + "..."
            tvReadMore.visibility = View.VISIBLE
        } else {
            tvReadMore.visibility = View.GONE
        }

        tvReadMore.setOnClickListener {
            expanded = !expanded
            if (expanded) {
                tvDeskripsi.text = fullText
                tvReadMore.text = "Tampilkan Lebih Sedikit"
            } else {
                tvDeskripsi.text = if (fullText.length > 150) {
                    fullText.take(150) + "..."
                } else {
                    fullText
                }
                tvReadMore.text = "Baca Selengkapnya"
            }
        }
    }

    companion object {
        private const val ARG_NAMA = "ARG_NAMA"
        private const val ARG_LOKASI = "ARG_LOKASI"
        private const val ARG_HARGA = "ARG_HARGA"
        private const val ARG_KATEGORI = "ARG_KATEGORI"
        private const val ARG_DESKRIPSI = "ARG_DESKRIPSI"

        fun newInstance(
            nama: String,
            lokasi: String,
            harga: String,
            kategori: String = "",
            deskripsi: String = ""
        ): DetailKosFragment {
            return DetailKosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAMA, nama)
                    putString(ARG_LOKASI, lokasi)
                    putString(ARG_HARGA, harga)
                    putString(ARG_KATEGORI, kategori)
                    putString(ARG_DESKRIPSI, deskripsi)
                }
            }
        }
    }
}

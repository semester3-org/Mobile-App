package com.apk.koshub.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.apk.koshub.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.security.MessageDigest

class DetailKosFragment : Fragment(R.layout.fragment_detail_kos), OnMapReadyCallback {

    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var namaKos: String = ""

    private var gMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ================= ARGUMENTS =================
        namaKos = arguments?.getString(ARG_NAMA).orEmpty()
        val lokasi = arguments?.getString(ARG_LOKASI).orEmpty()
        val harga = arguments?.getString(ARG_HARGA).orEmpty()
        val kategori = arguments?.getString(ARG_KATEGORI).orEmpty()
        val deskripsiArg = arguments?.getString(ARG_DESKRIPSI).orEmpty()
        lat = arguments?.getDouble(ARG_LAT) ?: 0.0
        lon = arguments?.getDouble(ARG_LON) ?: 0.0

        // ================= VIEWS =================
        val tvNamaKos = view.findViewById<TextView>(R.id.tvNamaKos)
        val tvLokasi = view.findViewById<TextView>(R.id.tvAlamat)
        val tvHarga = view.findViewById<TextView>(R.id.tvHarga)
        val tvKategori = view.findViewById<TextView>(R.id.tvKategoriKos)
        val tvDeskripsi = view.findViewById<TextView>(R.id.tvDeskripsi)
        val tvReadMore = view.findViewById<TextView>(R.id.tvReadMore)
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)

        // ================= BASIC INFO =================
        tvNamaKos.text = namaKos
        tvLokasi.text = lokasi
        tvHarga.text = harga
        tvKategori.text = if (kategori.isNotBlank()) kategori else "Kos"

        // ================= DESKRIPSI + READ MORE =================
        val fullText =
            if (deskripsiArg.isNotBlank()) deskripsiArg
            else tvDeskripsi.text.toString()

        ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        var expanded = false
        if (fullText.length > 150) {
            tvDeskripsi.text = fullText.take(150) + "..."
            tvReadMore.visibility = View.VISIBLE
        } else {
            tvDeskripsi.text = fullText
            tvReadMore.visibility = View.GONE
        }

        tvReadMore.setOnClickListener {
            expanded = !expanded
            if (expanded) {
                tvDeskripsi.text = fullText
                tvReadMore.text = "Tampilkan Lebih Sedikit"
            } else {
                tvDeskripsi.text =
                    if (fullText.length > 150) fullText.take(150) + "..." else fullText
                tvReadMore.text = "Baca Selengkapnya"
            }
        }

        // ================= DEBUG KEY + SHA1 =================
        logMapsDebug()

        // ================= MAP INTERAKTIF =================
        val mapFrag =
            childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment

        if (mapFrag == null) {
            Log.e("MAP_DEBUG", "SupportMapFragment tidak ditemukan. Cek fragment_detail_kos.xml")
            return
        }

        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
        }

        if (lat == 0.0 && lon == 0.0) {
            Log.w("MAP_DEBUG", "Lat/Lon kosong, map tidak diarahkan ke marker.")
            return
        }

        val pos = LatLng(lat, lon)

        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(pos)
                .title(namaKos)
        )

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(pos, 16f)
        )

        googleMap.setOnMapClickListener {
            val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($namaKos)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun logMapsDebug() {
        try {
            val pm = requireContext().packageManager
            val pkg = requireContext().packageName

            // key dari manifest
            val appInfo = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
            val keyFromManifest =
                appInfo.metaData?.getString("com.google.android.geo.API_KEY")

            Log.d("MAP_DEBUG", "runtime package=$pkg")
            Log.d("MAP_DEBUG", "keyFromManifest=$keyFromManifest")

            // SHA1 runtime (aman utk semua versi)
            val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // signingInfo bisa null -> handle
                info.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                info.signatures ?: emptyArray()
            }

            if (signatures.isEmpty()) {
                Log.w("MAP_DEBUG", "signatures kosong, tidak bisa ambil SHA1 runtime.")
                return
            }

            signatures.forEach { sig ->
                val md = MessageDigest.getInstance("SHA-1")
                md.update(sig.toByteArray())
                val sha1 = md.digest().joinToString(":") { "%02X".format(it) }
                Log.d("MAP_DEBUG", "runtime SHA1=$sha1")
            }

        } catch (e: Exception) {
            Log.e("MAP_DEBUG", "Gagal debug maps: ${e.message}")
        }
    }

    companion object {
        private const val ARG_NAMA = "ARG_NAMA"
        private const val ARG_LOKASI = "ARG_LOKASI"
        private const val ARG_HARGA = "ARG_HARGA"
        private const val ARG_KATEGORI = "ARG_KATEGORI"
        private const val ARG_DESKRIPSI = "ARG_DESKRIPSI"
        private const val ARG_LAT = "ARG_LAT"
        private const val ARG_LON = "ARG_LON"

        fun newInstance(
            nama: String,
            lokasi: String,
            harga: String,
            kategori: String = "",
            deskripsi: String = "",
            lat: Double = 0.0,
            lon: Double = 0.0
        ): DetailKosFragment {
            return DetailKosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAMA, nama)
                    putString(ARG_LOKASI, lokasi)
                    putString(ARG_HARGA, harga)
                    putString(ARG_KATEGORI, kategori)
                    putString(ARG_DESKRIPSI, deskripsi)
                    putDouble(ARG_LAT, lat)
                    putDouble(ARG_LON, lon)
                }
            }
        }
    }
}

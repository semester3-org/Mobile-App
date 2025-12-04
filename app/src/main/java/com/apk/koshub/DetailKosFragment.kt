package com.apk.koshub.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.apk.koshub.R
import com.apk.koshub.api.ApiClient
import com.apk.koshub.api.ApiService
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.BasicResponse
import com.apk.koshub.models.FacilityDto
import com.apk.koshub.models.KosDetailDto
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.UserRatingResponse
import com.apk.koshub.ui.KosPhotoPagerAdapter
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class DetailKosFragment : Fragment(R.layout.fragment_detail_kos), OnMapReadyCallback {

    // ===== Fragment Result keys (sync favorite) =====
    private val REQ_FAV_CHANGED = "fav_changed"
    private val KEY_KOS_ID = "kos_id"
    private val KEY_IS_FAVORITE = "is_favorite"

    private var kosId: Int = -1
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var namaKos: String = ""

    private var gMap: GoogleMap? = null

    private var currentDetail: KosDetailDto? = null


    // ===== Favorite state =====
    private lateinit var pref: SharedPrefHelper
    private lateinit var db: DatabaseHelper
    private var isFavorite: Boolean = false
    private var favoriteBusy: Boolean = false
    private lateinit var ivFavorite: ImageView

    // ===== Views =====
    private lateinit var tvNamaKos: TextView
    private lateinit var tvAlamat: TextView
    private lateinit var tvHarga: TextView
    private lateinit var tvKategori: TextView
    private lateinit var tvDeskripsi: TextView
    private lateinit var tvReadMore: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvRatingValue: TextView
    private lateinit var chipGroupFacilities: ChipGroup
    private lateinit var btnBooking: Button

    // buat bottom sheet
    private var lastDetail: KosDetailDto? = null


    private var userRating: Float = 0f
    private var userComment: String = ""

    //----- RATING -----
    private lateinit var ratingBarUser: RatingBar
    private lateinit var etUserComment: EditText
    private lateinit var btnSubmitRating: Button

    // ===== Photos + dots manual =====
    private lateinit var vpPhotos: ViewPager2
    private lateinit var dotsContainer: LinearLayout
    private lateinit var photoAdapter: KosPhotoPagerAdapter
    private val dots = mutableListOf<View>()
    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            selectDot(position)
        }
    }

    private val api: ApiService get() = ApiClient.api

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kosId = arguments?.getInt(ARG_KOS_ID) ?: -1
        if (kosId == -1) {
            Log.e(TAG, "kosId kosong. Pastikan kirim ARG_KOS_ID.")
            parentFragmentManager.popBackStack()
            return
        }

        pref = SharedPrefHelper(requireContext())
        db = DatabaseHelper(requireContext())

        // bind views
        tvNamaKos = view.findViewById(R.id.tvNamaKos)
        tvAlamat = view.findViewById(R.id.tvAlamat)
        tvHarga = view.findViewById(R.id.tvHarga)
        tvKategori = view.findViewById(R.id.tvKategoriKos)
        tvDeskripsi = view.findViewById(R.id.tvDeskripsi)
        tvReadMore = view.findViewById(R.id.tvReadMore)
        ratingBar = view.findViewById(R.id.ratingBar)
        tvRatingValue = view.findViewById(R.id.tvRatingValue)
        chipGroupFacilities = view.findViewById(R.id.chipGroupFacilities)
        btnBooking = view.findViewById(R.id.btnBooking)
        ratingBarUser = view.findViewById(R.id.ratingBarUser)
        etUserComment = view.findViewById(R.id.etUserComment)
        btnSubmitRating = view.findViewById(R.id.btnSubmitRating)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnBooking.setOnClickListener { openBookingSheet() }

        // favorite
        ivFavorite = view.findViewById(R.id.ivFavorite)
        updateFavoriteIcon()
        ivFavorite.setOnClickListener { onFavoriteClicked() }

        // slider + dots manual
        vpPhotos = view.findViewById(R.id.vpKosPhotos)
        dotsContainer = view.findViewById(R.id.layoutDots)

        photoAdapter = KosPhotoPagerAdapter()
        vpPhotos.adapter = photoAdapter
        vpPhotos.registerOnPageChangeCallback(pageCallback)

        // map async
        val mapFrag = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFrag?.getMapAsync(this)

        // load detail
        fetchDetail()

        fetchUserRating()

        // sync status favorite awal
        syncFavoriteStatus()

        btnSubmitRating.setOnClickListener {
            submitUserRating()
        }
    }


    override fun onDestroyView() {
        vpPhotos.unregisterOnPageChangeCallback(pageCallback)
        super.onDestroyView()
    }

    // ===================== DETAIL API =====================

    private fun fetchDetail() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Request kos_detail kos_id=$kosId")
                val res = api.getKosDetail(kosId)

                val d = res.data
                if (d == null) {
                    Log.e(TAG, "Response data null. status=${res.status} msg=${res.message}")
                    Toast.makeText(requireContext(), "Detail kosong", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                renderDetail(d)
            } catch (e: Exception) {
                Log.e(TAG, "fetchDetail error: ${e.message}", e)
                Toast.makeText(requireContext(), "Gagal load detail: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderDetail(d: KosDetailDto) {
        lastDetail = d   // <<-- penting buat bottom sheet

        namaKos = d.name

        tvNamaKos.text = d.name
        tvKategori.text = formatKosType(d.kosType)

        tvAlamat.text = d.address ?: (d.locationName ?: "")
        tvHarga.text = d.priceMonthly?.let { "Rp ${formatRupiah(it)}" } ?: "-"

        val ratingVal = d.rating ?: 0.0
        val ratingCount = d.ratingCount ?: 0
        ratingBar.rating = ratingVal.toFloat()
        tvRatingValue.text =
            if (ratingCount > 0) "${ratingVal} ($ratingCount Reviews)" else "$ratingVal"

        setupReadMore(d.description.orEmpty())

        // photos
        val imgs = d.images.orEmpty()
        photoAdapter.submit(imgs)
        buildDots(imgs.size)
        vpPhotos.isUserInputEnabled = imgs.size > 1
        if (imgs.isNotEmpty()) selectDot(0)

        // facilities
        val facList = if (!d.facilitiesList.isNullOrEmpty()) d.facilitiesList!! else parseFacilitiesText(d.facilities)
        renderFacilities(facList)

        // map coords
        lat = d.latitude ?: 0.0
        lon = d.longitude ?: 0.0
        updateMap()
    }

    // ===================== FAVORITE =====================

    private fun syncFavoriteStatus() {
        if (!pref.isLoggedIn()) {
            isFavorite = false
            updateFavoriteIcon()
            return
        }

        val user = db.getUser()
        if (user == null) {
            isFavorite = false
            updateFavoriteIcon()
            return
        }

        api.getFavoriteKos(userId = user.id).enqueue(object : Callback<KosResponse> {
            override fun onResponse(call: Call<KosResponse>, response: Response<KosResponse>) {
                val body = response.body()
                val ids = if (response.isSuccessful && body?.isSuccess == true) {
                    body.data.map { it.id }.toSet()
                } else emptySet()

                isFavorite = ids.contains(kosId)
                updateFavoriteIcon()
            }

            override fun onFailure(call: Call<KosResponse>, t: Throwable) {
                Log.e(TAG, "syncFavoriteStatus fail: ${t.message}", t)
            }
        })
    }

    private fun onFavoriteClicked() {
        if (favoriteBusy) return

        if (!pref.isLoggedIn()) {
            Toast.makeText(requireContext(), "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val user = db.getUser()
        if (user == null) {
            Toast.makeText(requireContext(), "User Tidak Ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        favoriteBusy = true

        val old = isFavorite
        isFavorite = !isFavorite
        updateFavoriteIcon()

        val call: Call<BasicResponse> = if (isFavorite) {
            api.addFavorite(userId = user.id, kosId = kosId)
        } else {
            api.removeFavorite(userId = user.id, kosId = kosId)
        }

        call.enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                favoriteBusy = false
                val body = response.body()

                if (!response.isSuccessful || body?.success != true) {
                    isFavorite = old
                    updateFavoriteIcon()
                    Toast.makeText(requireContext(), body?.message ?: "Gagal update favorite", Toast.LENGTH_SHORT).show()
                    return
                }

                parentFragmentManager.setFragmentResult(
                    REQ_FAV_CHANGED,
                    Bundle().apply {
                        putInt(KEY_KOS_ID, kosId)
                        putBoolean(KEY_IS_FAVORITE, isFavorite)
                    }
                )
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                favoriteBusy = false
                isFavorite = old
                updateFavoriteIcon()
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateFavoriteIcon() {
        ivFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    // ===================== READ MORE =====================

    private fun setupReadMore(fullText: String) {
        var expanded = false

        if (fullText.length > 150) {
            tvDeskripsi.text = fullText.take(150) + "..."
            tvReadMore.visibility = View.VISIBLE
            tvReadMore.text = "Baca Selengkapnya"
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
                tvDeskripsi.text = fullText.take(150) + "..."
                tvReadMore.text = "Baca Selengkapnya"
            }
        }
    }

    // ===================== DOTS (MANUAL) =====================

    private fun buildDots(count: Int) {
        dotsContainer.removeAllViews()
        dots.clear()

        if (count <= 1) {
            dotsContainer.visibility = View.GONE
            return
        }

        dotsContainer.visibility = View.VISIBLE

        repeat(count) { i ->
            val dot = View(requireContext()).apply {
                val size = dp(8)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                }
                setBackgroundResource(R.drawable.dots_selector)
                isSelected = (i == 0)
                setOnClickListener { vpPhotos.currentItem = i }
            }
            dots.add(dot)
            dotsContainer.addView(dot)
        }
    }

    private fun selectDot(pos: Int) {
        dots.forEachIndexed { i, v -> v.isSelected = (i == pos) }
    }

    private fun dp(v: Int): Int =
        (v * requireContext().resources.displayMetrics.density).toInt()

    // ===================== FACILITIES =====================

    private fun renderFacilities(items: List<FacilityDto>) {
        chipGroupFacilities.removeAllViews()

        items.forEach { f ->
            val chip = Chip(requireContext()).apply {
                text = f.name
                isClickable = false
                isCheckable = false
                chipIconTint = null
                chipIconSize = dp(18).toFloat()

                val iconRes = mapFacilityIconToDrawable(f.icon, f.name)
                if (iconRes != null) {
                    chipIcon = ContextCompat.getDrawable(requireContext(), iconRes)
                    isChipIconVisible = true
                }
            }
            chipGroupFacilities.addView(chip)
        }
    }

    private fun mapFacilityIconToDrawable(icon: String?, name: String): Int? {
        val fa = icon?.trim()?.lowercase().orEmpty()
        val n = name.trim().lowercase()

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

        return when {
            n.contains("wifi") || n.contains("wi-fi") -> R.drawable.ic_wifi
            n == "ac" || n.contains(" ac") || n.contains("ac ") || n.contains("air conditioner") -> R.drawable.ic_ac
            n.contains("air bersih") || n.contains("shower") -> R.drawable.ic_shower
            n.contains("listrik") || n.contains("free listrik") || n.contains("plug") || n.contains("colokan") -> R.drawable.ic_plug
            n.contains("laundry") -> R.drawable.ic_laundry
            n.contains("fingerprint") -> R.drawable.ic_fingerprint
            n.contains("cctv") -> R.drawable.ic_cctv
            n.contains("security") || n.contains("satpam") -> R.drawable.ic_security
            n.contains("parkir luas") || n.contains("garasi") -> R.drawable.ic_garage
            n.contains("parkir motor") || n.contains("parkir mobil") || n.contains("parkir") -> R.drawable.ic_parking
            n.contains("kasur") || n.contains("bed") -> R.drawable.ic_bed
            n.contains("meja") -> R.drawable.ic_meja
            n.contains("lemari") -> R.drawable.ic_lemari
            n.contains("kamar mandi") || n == "km" || n.contains("toilet") || n.contains("wc") -> R.drawable.ic_km_dalam
            else -> null
        }
    }

    private fun parseFacilitiesText(text: String?): List<FacilityDto> {
        if (text.isNullOrBlank()) return emptyList()
        return text.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { name ->
                FacilityDto(
                    id = 0,
                    name = name,
                    icon = null
                )
            }
    }

    // ===================== KOS TYPE + RUPIAH =====================

    private fun formatKosType(type: String?): String {
        val t = type?.trim()?.lowercase().orEmpty()
        if (t.isBlank()) return "Kos"
        val label = when (t) {
            "putra" -> "Putra"
            "putri" -> "Putri"
            "campur", "campuran", "mix" -> "Campur"
            else -> t.replaceFirstChar { it.uppercaseChar() }
        }
        return "Kos $label"
    }

    private fun formatRupiah(value: Int): String {
        val nf = NumberFormat.getInstance(Locale("in", "ID"))
        return nf.format(value).replace(",", ".")
    }

    // ===================== MAP =====================

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
        }

        updateMap()

        googleMap.setOnMapClickListener {
            if (lat == 0.0 && lon == 0.0) return@setOnMapClickListener
            val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($namaKos)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            startActivity(mapIntent)
        }
    }

    private fun updateMap() {
        val map = gMap ?: return
        if (lat == 0.0 && lon == 0.0) return

        val pos = LatLng(lat, lon)
        map.clear()
        map.addMarker(MarkerOptions().position(pos).title(namaKos))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
    }

    // ===================== BOOKING BOTTOM SHEET =====================

    private fun openBookingSheet() {
        if (!pref.isLoggedIn()) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val detail = lastDetail
        if (detail == null) {
            Toast.makeText(requireContext(), "Data kos belum siap", Toast.LENGTH_SHORT).show()
            return
        }

        val sheet = BookingBottomSheetFragment.newInstance(
            kosId = kosId,
            kosName = detail.name,
            priceMonthly = detail.priceMonthly ?: 0,
            priceDaily = detail.priceDaily ?: 0, // TAMBAH FIELD INI DI KosDetailDto + API
            address = detail.address ?: (detail.locationName ?: "")
        )
        sheet.show(parentFragmentManager, "booking_sheet")
    }
    private fun fetchUserRating() {
        lifecycleScope.launch {
            try {
                // langsung dapat UserRatingResponse
                val userRatingData = api.getUserRating(kosId)

                userRating = userRatingData.rating ?: 0f
                userComment = userRatingData.comment.orEmpty()

                ratingBarUser.rating = userRating
                etUserComment.setText(userComment)
            } catch (e: Exception) {
                Log.e(TAG, "fetchUserRating error: ${e.message}", e)
            }
        }
    }



    private fun submitUserRating() {
        val rating = ratingBarUser.rating
        val comment = etUserComment.text.toString()
        Log.d(TAG, "submitUserRating() clicked, rating=$rating, comment=$comment")
        if (rating == 0f || comment.isEmpty()) {
            Toast.makeText(requireContext(), "Rating dan komentar harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val user = db.getUser()
        if (user == null) {
            Toast.makeText(requireContext(), "User Tidak Ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val res = api.submitUserRating(
                    kosId = kosId,
                    rating = rating,
                    comment = comment,
                    userId = user.id
                )
                Log.d(TAG, "submitUserRating response: success=${res.success}, status=${res.status}, message=${res.message}")

                if (res.success) {
                    ratingBarUser.rating = 0f      // reset bintang
                    etUserComment.setText("")      // kosongkan komentar
                    Toast.makeText(
                        requireContext(),
                        res.message ?: "Rating berhasil dikirim",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        res.message ?: "Gagal mengirim rating",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "submitUserRating error: ${e.message}", e)
                Toast.makeText(requireContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val TAG = "DETAIL_KOS"
        private const val ARG_KOS_ID = "ARG_KOS_ID"

        fun newInstance(kosId: Int): DetailKosFragment {
            return DetailKosFragment().apply {
                arguments = Bundle().apply { putInt(ARG_KOS_ID, kosId) }
            }
        }
    }
}

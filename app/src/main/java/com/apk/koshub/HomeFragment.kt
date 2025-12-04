package com.apk.koshub.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.KosAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.FilterState
import com.apk.koshub.models.KosItem
import com.apk.koshub.models.KosResponse
import com.apk.koshub.models.UnreadCountResponse
import com.apk.koshub.models.toKosItem
import com.apk.koshub.utils.SharedPrefHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(), FilterDialogFragment.OnFilterApplied {

    private lateinit var rvRekom: RecyclerView
    private lateinit var rvFavorit: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapterRekom: KosAdapter
    private lateinit var adapterFav: KosAdapter

    private val allRekom = mutableListOf<KosItem>()
    private val allFav = mutableListOf<KosItem>()

    // notif badge
    private var badgeDot: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var badgeRunnable: Runnable? = null

    private lateinit var pref: SharedPrefHelper
    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        pref = SharedPrefHelper(requireContext())
        db = DatabaseHelper(requireContext())
        userId = db.getUser()?.id ?: pref.getUserId()

        val notifButton = view.findViewById<ImageButton>(R.id.ibNotification)
        badgeDot = view.findViewById(R.id.notifBadgeDot)

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
        etSearch = view.findViewById(R.id.etSearchHome) // ✅ ini EditText

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

        // pertama kali buka home -> cek badge
        refreshNotifBadge()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshNotifBadge()
        startBadgePolling()
    }

    override fun onPause() {
        super.onPause()
        stopBadgePolling()
    }

    private fun startBadgePolling() {
        stopBadgePolling()
        if (userId <= 0) {
            badgeDot?.visibility = View.GONE
            return
        }

        badgeRunnable = object : Runnable {
            override fun run() {
                refreshNotifBadge()
                handler.postDelayed(this, 5000) // 5 detik sekali (santai)
            }
        }
        handler.post(badgeRunnable!!)
    }

    private fun stopBadgePolling() {
        badgeRunnable?.let { handler.removeCallbacks(it) }
        badgeRunnable = null
    }

    private fun refreshNotifBadge() {
        if (userId <= 0) {
            badgeDot?.visibility = View.GONE
            return
        }

        // Endpoint: users/unread_count.php?user_id=...
        ApiClient.api.getUnreadCount(userId).enqueue(object : Callback<UnreadCountResponse> {
            override fun onResponse(
                call: Call<UnreadCountResponse>,
                response: Response<UnreadCountResponse>
            ) {
                if (!isAdded) return

                val body = response.body()
                val unread = if (response.isSuccessful && body?.success == true) body.unread_count else 0
                badgeDot?.visibility = if (unread > 0) View.VISIBLE else View.GONE

                Log.d("HOME_BADGE", "unread=$unread success=${body?.success} http=${response.code()}")
            }

            override fun onFailure(call: Call<UnreadCountResponse>, t: Throwable) {
                if (!isAdded) return
                // kalau gagal jaringan, jangan spam toast, cukup ilangin dot biar ga misleading
                badgeDot?.visibility = View.GONE
                Log.e("HOME_BADGE", "error=${t.message}")
            }
        })
    }

    private fun loadKos() {
        ApiClient.api.getKosList()
            .enqueue(object : Callback<KosResponse> {
                override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {
                    if (!isAdded) return
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
                    if (!isAdded) return
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

        Log.d("HomeFragment", "filter facilityIds = $facilityIds")

        ApiClient.api.getFilteredKos(
            kosType = kosType,
            availableOnly = availableOnly,
            minPrice = minPrice,
            maxPrice = maxPrice,
            facilityIds = facilityIds
        ).enqueue(object : Callback<KosResponse> {
            override fun onResponse(call: Call<KosResponse>, res: Response<KosResponse>) {
                if (!isAdded) return
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
                if (!isAdded) return
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openDetail(kos: KosItem) {
        val detail = DetailKosFragment.newInstance(kosId = kos.id)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detail)
            .addToBackStack(null)
            .commit()
    }
}

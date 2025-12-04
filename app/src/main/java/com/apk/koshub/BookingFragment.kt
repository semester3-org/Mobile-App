package com.apk.koshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.adapters.BookingAdapter
import com.apk.koshub.api.ApiClient
import com.apk.koshub.models.BasicResponse
import com.apk.koshub.models.BookingItem
import com.apk.koshub.models.BookingListResponse
import com.apk.koshub.utils.SharedPrefHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingFragment : Fragment(), BookingAdapter.Listener {

    private lateinit var rvBooking: RecyclerView
    private lateinit var progressBooking: ProgressBar
    private lateinit var tvEmptyBooking: TextView
    private lateinit var btnBackBooking: ImageButton

    private lateinit var adapter: BookingAdapter
    private lateinit var pref: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_booking, container, false)

        rvBooking = view.findViewById(R.id.rvBooking)
        progressBooking = view.findViewById(R.id.progressBooking)
        tvEmptyBooking = view.findViewById(R.id.tvEmptyBooking)
        btnBackBooking = view.findViewById(R.id.btnBackBooking)

        pref = SharedPrefHelper(requireContext())

        adapter = BookingAdapter(this)
        rvBooking.layoutManager = LinearLayoutManager(requireContext())
        rvBooking.adapter = adapter

        btnBackBooking.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        loadBookings()

        return view
    }

    // ========= LOAD LIST BOOKING =========
    private fun loadBookings() {
        val userId = pref.getUserId() ?: run {
            tvEmptyBooking.visibility = View.VISIBLE
            tvEmptyBooking.text = "Silakan login untuk melihat booking."
            return
        }

        progressBooking.visibility = View.VISIBLE
        tvEmptyBooking.visibility = View.GONE

        ApiClient.api.getUserBookings(userId)
            .enqueue(object : Callback<BookingListResponse> {
                override fun onResponse(
                    call: Call<BookingListResponse>,
                    response: Response<BookingListResponse>
                ) {
                    progressBooking.visibility = View.GONE
                    val body = response.body()

                    if (response.isSuccessful && body?.status == "success") {
                        val data = body.data
                        adapter.setData(data)
                        tvEmptyBooking.visibility =
                            if (data.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        tvEmptyBooking.visibility = View.VISIBLE
                        tvEmptyBooking.text = "Gagal memuat booking."
                    }
                }

                override fun onFailure(call: Call<BookingListResponse>, t: Throwable) {
                    progressBooking.visibility = View.GONE
                    tvEmptyBooking.visibility = View.VISIBLE
                    tvEmptyBooking.text = "Error: ${t.message}"
                }
            })
    }

    // ========= LISTENER DARI ADAPTER =========

    override fun onDetailClicked(item: BookingItem) {
        // TODO: buka detail booking
    }

    override fun onPayClicked(item: BookingItem) {
        // TODO: nanti arahin ke Midtrans / WebView
    }

    override fun onCancelClicked(item: BookingItem) {
        // cek lagi biar aman:
        // property di model: paymentStatus (bukan payment_status)
        if (item.status != "pending" || item.paymentStatus != "unpaid") {
            // harusnya ga kejadian kalau adapter udah bener
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Booking")
            .setMessage("Yakin mau membatalkan pengajuan booking ini?")
            .setPositiveButton("Ya") { _, _ ->
                doCancelBooking(item)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun doCancelBooking(item: BookingItem) {
        val userId = pref.getUserId() ?: return

        progressBooking.visibility = View.VISIBLE

        ApiClient.api.cancelBooking(
            bookingId = item.id,
            userId = userId
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {
                progressBooking.visibility = View.GONE

                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    // refresh list biar status jadi "cancelled"
                    loadBookings()
                } else {
                    tvEmptyBooking.visibility = View.VISIBLE
                    tvEmptyBooking.text = body?.message ?: "Gagal membatalkan booking."
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                progressBooking.visibility = View.GONE
                tvEmptyBooking.visibility = View.VISIBLE
                tvEmptyBooking.text = "Error: ${t.message}"
            }
        })
    }
    override fun onResume() {
        super.onResume()
        loadBookings()
    }
}

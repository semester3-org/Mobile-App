package com.apk.koshub.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.apk.koshub.R
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.BookingCreateResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_KOS_ID = "kos_id"
        private const val ARG_KOS_NAME = "kos_name"
        private const val ARG_PRICE_MONTHLY = "price_monthly"
        private const val ARG_PRICE_DAILY = "price_daily"
        private const val ARG_ADDRESS = "address"

        fun newInstance(
            kosId: Int,
            kosName: String,
            priceMonthly: Int,
            priceDaily: Int,
            address: String
        ): BookingBottomSheetFragment {
            return BookingBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_KOS_ID, kosId)
                    putString(ARG_KOS_NAME, kosName)
                    putInt(ARG_PRICE_MONTHLY, priceMonthly)
                    putInt(ARG_PRICE_DAILY, priceDaily)
                    putString(ARG_ADDRESS, address)
                }
            }
        }
    }

    private var kosId = 0
    private var kosName = ""
    private var priceMonthly = 0
    private var priceDaily = 0
    private var address = ""

    private lateinit var tvKosName: TextView
    private lateinit var tvKosPrice: TextView
    private lateinit var tvAddress: TextView
    private lateinit var spinnerType: Spinner
    private lateinit var tvCheckInDate: TextView
    private lateinit var tvDurationLabel: TextView
    private lateinit var spinnerDuration: Spinner
    private lateinit var tvTotalPreview: TextView
    private lateinit var btnConfirm: Button
    private lateinit var btnClose: ImageButton

    private var bookingType: String = "monthly"  // monthly / daily
    private var selectedDuration: Int = 1
    private var calendarCheckIn: Calendar? = null

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kosId = it.getInt(ARG_KOS_ID)
            kosName = it.getString(ARG_KOS_NAME).orEmpty()
            priceMonthly = it.getInt(ARG_PRICE_MONTHLY)
            priceDaily = it.getInt(ARG_PRICE_DAILY)
            address = it.getString(ARG_ADDRESS).orEmpty()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())

        tvKosName = view.findViewById(R.id.tvSheetKosName)
        tvKosPrice = view.findViewById(R.id.tvSheetKosPrice)
        tvAddress = view.findViewById(R.id.tvSheetKosAddress)
        spinnerType = view.findViewById(R.id.spinnerBookingType)
        tvCheckInDate = view.findViewById(R.id.tvCheckInDate)
        tvDurationLabel = view.findViewById(R.id.tvDurationLabel)
        spinnerDuration = view.findViewById(R.id.spinnerDuration)
        tvTotalPreview = view.findViewById(R.id.tvTotalPricePreview)
        btnConfirm = view.findViewById(R.id.btnConfirmBooking)
        btnClose = view.findViewById(R.id.btnCloseBookingSheet)

        tvKosName.text = kosName
        tvAddress.text = address
        tvKosPrice.text = "Rp ${formatRupiah(priceMonthly)} /bulan"

        setupTypeSpinner()
        setupDurationSpinnerMonthly()
        updateTotalPreview()

        tvCheckInDate.setOnClickListener { showDatePicker() }
        btnClose.setOnClickListener { dismiss() }
        btnConfirm.setOnClickListener { submitBooking() }
    }

    // ====== UI ======

    private fun setupTypeSpinner() {
        val labels = mutableListOf<String>()
        val values = mutableListOf<String>()

        if (priceMonthly > 0) {
            labels.add("Per Bulan")
            values.add("monthly")
        }
        if (priceDaily > 0) {
            labels.add("Per Hari")
            values.add("daily")
        }
        if (labels.isEmpty()) {
            labels.add("Per Bulan")
            values.add("monthly")
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            labels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                bookingType = values[position]

                if (bookingType == "monthly") {
                    tvDurationLabel.text = "Durasi (bulan)"
                    tvKosPrice.text = "Rp ${formatRupiah(priceMonthly)} /bulan"
                    setupDurationSpinnerMonthly()
                } else {
                    tvDurationLabel.text = "Durasi (hari)"
                    tvKosPrice.text = "Rp ${formatRupiah(priceDaily)} /hari"
                    setupDurationSpinnerDaily()
                }
                updateTotalPreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDurationSpinnerMonthly() {
        val items = (1..12).map { "$it Bulan" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDuration.adapter = adapter
        spinnerDuration.setSelection(0)
        spinnerDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                selectedDuration = pos + 1
                updateTotalPreview()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupDurationSpinnerDaily() {
        val items = (1..30).map { "$it Hari" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDuration.adapter = adapter
        spinnerDuration.setSelection(0)
        spinnerDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                selectedDuration = pos + 1
                updateTotalPreview()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun showDatePicker() {
        val cal = calendarCheckIn ?: Calendar.getInstance()
        val dp = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day, 0, 0, 0)
                calendarCheckIn = c

                val fmtShow = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
                tvCheckInDate.text = fmtShow.format(c.time)

                updateTotalPreview()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dp.show()
    }

    private fun updateTotalPreview() {
        if (selectedDuration <= 0) return
        val total = if (bookingType == "monthly") {
            priceMonthly * selectedDuration
        } else {
            priceDaily * selectedDuration
        }
        tvTotalPreview.text = "Total: Rp ${formatRupiah(total)}"
    }

    // ====== SUBMIT ======

    private fun submitBooking() {
        val user = db.getUser()
        if (user == null) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = calendarCheckIn
        if (cal == null) {
            Toast.makeText(requireContext(), "Pilih tanggal check-in dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val fmtApi = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val checkInStr = fmtApi.format(cal.time)

        val body = mutableMapOf<String, String>()
        body["user_id"] = user.id.toString()
        body["kos_id"] = kosId.toString()
        body["booking_type"] = bookingType
        body["check_in_date"] = checkInStr

        if (bookingType == "monthly") {
            body["duration_months"] = selectedDuration.toString()
            // check_out_date boleh kosong, PHP lu udah auto hitung
        } else {
            // daily → wajib kirim check_out_date
            val out = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, selectedDuration)
            }
            val outStr = fmtApi.format(out.time)
            body["check_out_date"] = outStr
        }

        btnConfirm.isEnabled = false
        btnConfirm.text = "Memproses..."

        ApiClient.api.createBooking(body)
            .enqueue(object : Callback<BookingCreateResponse> {
                override fun onResponse(
                    call: Call<BookingCreateResponse>,
                    response: Response<BookingCreateResponse>
                ) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Konfirmasi & Booking"

                    val res = response.body()
                    if (response.isSuccessful && res != null && res.status == "success") {
                        Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            res?.message ?: "Gagal membuat booking.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BookingCreateResponse>, t: Throwable) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Konfirmasi & Booking"
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun formatRupiah(value: Int): String {
        return "%,d".format(value).replace(",", ".")
    }
}

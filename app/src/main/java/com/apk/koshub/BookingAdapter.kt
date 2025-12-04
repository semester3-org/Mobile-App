package com.apk.koshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.models.BookingItem

class BookingAdapter(
    private val listener: Listener
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    private val items = mutableListOf<BookingItem>()

    interface Listener {
        fun onDetailClicked(item: BookingItem)
        fun onPayClicked(item: BookingItem)
        fun onCancelClicked(item: BookingItem)
    }

    fun setData(newItems: List<BookingItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvBookingCode: TextView = itemView.findViewById(R.id.tvBookingCode)
        private val tvKosTypeBadge: TextView = itemView.findViewById(R.id.tvKosTypeBadge)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        private val tvKosName: TextView = itemView.findViewById(R.id.tvKosName)
        private val tvKosLocation: TextView = itemView.findViewById(R.id.tvKosLocation)
        private val tvCheckIn: TextView = itemView.findViewById(R.id.tvCheckIn)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        private val btnPrimary: Button = itemView.findViewById(R.id.btnPrimary)
        private val btnSecondary: Button = itemView.findViewById(R.id.btnSecondary)

        fun bind(item: BookingItem) {
            // kode booking
            tvBookingCode.text = "#%05d".format(item.id)

            // nama & lokasi
            tvKosName.text = item.kosName
            tvKosLocation.text = item.locationName

            // check-in & durasi
            tvCheckIn.text = item.checkInDate
            tvDuration.text = if (item.bookingType == "monthly") {
                val dur = item.durationMonths ?: 0
                if (dur > 0) "$dur Bulan" else "- Bulan"
            } else {
                "${item.checkInDate} - ${item.checkOutDate ?: "-"}"
            }

            // harga & waktu dibuat
            tvTotalPrice.text = formatRupiah(item.totalPrice)
            tvCreatedAt.text = "Dibuat ${item.createdAt}"

            // tipe kos badge (optional, sesuaikan dengan field di BookingItem)
            val kosType = item.kosType?.lowercase() ?: ""
            tvKosTypeBadge.text = when (kosType) {
                "putra" -> "Putra"
                "putri" -> "Putri"
                "campur" -> "Campur"
                else -> "Kos"
            }

            tvKosTypeBadge.setBackgroundResource(R.drawable.bg_badge_pink)


            android.util.Log.d(
                "BookingDebug",
                "id=${item.id}, status='${item.status}', payment='${item.paymentStatus}'"
            )
            // status + payment
            val status = item.status.lowercase()
            val payment = item.paymentStatus.lowercase()

            // set text badge status
            tvStatusBadge.text = when {
                status == "confirmed" ->
                    "Menunggu"
                status == "approved" && payment == "unpaid" ->
                    "Dikonfirmasi • Belum Bayar"
                status == "confirmed" && payment == "paid" ->
                    "Dikonfirmasi • Lunas"
                status == "rejected" ->
                    "Ditolak"
                status == "cancelled" ->
                    "Dibatalkan"
                else ->
                    item.status
            }

            // warna background badge status (simple aja)
            when {
                status == "pending" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_yellow)
                }
                status == "confirmed" -> {
                    // pakai chip hijau yang udah ada di project
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_chip_green)
                }
                status == "rejected" || status == "cancelled" -> {
                    tvStatusBadge.setBackgroundResource(android.R.color.darker_gray)
                }
                else -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_yellow)
                }
            }

            // ====== tombol sesuai kombinasi status + payment ======

            // reset dulu
            btnPrimary.visibility = View.VISIBLE
            btnSecondary.visibility = View.VISIBLE
            btnPrimary.isEnabled = true

            when {
                // PENDING: Batal + Detail
                status == "pending" && payment == "unpaid" -> {
                    btnPrimary.text = "Batal"
                    btnSecondary.text = "Detail"

                    btnPrimary.setOnClickListener { listener.onCancelClicked(item) }
                    btnSecondary.setOnClickListener { listener.onDetailClicked(item) }
                }

                // APPROVED tapi BELUM BAYAR: Bayar Sekarang + Detail
                status == "confirmed" && payment == "unpaid" -> {
                    btnPrimary.text = "Bayar Sekarang"
                    btnSecondary.text = "Detail"

                    btnPrimary.setOnClickListener { listener.onPayClicked(item) }
                    btnSecondary.setOnClickListener { listener.onDetailClicked(item) }
                }

                // APPROVED + PAID: Sudah Bayar (disabled) + Detail
                status == "confirmed" && payment == "paid" -> {
                    btnPrimary.text = "Sudah Bayar"
                    btnPrimary.isEnabled = false
                    btnSecondary.text = "Detail"

                    btnPrimary.setOnClickListener { /* no-op */ }
                    btnSecondary.setOnClickListener { listener.onDetailClicked(item) }
                }

                // REJECTED / CANCELLED: cuma Detail
                status == "rejected" || status == "cancelled" -> {
                    btnPrimary.visibility = View.GONE
                    btnSecondary.visibility = View.VISIBLE
                    btnSecondary.text = "Detail"

                    btnSecondary.setOnClickListener { listener.onDetailClicked(item) }
                }

                // fallback: kalau ada status aneh
                else -> {
                    btnPrimary.visibility = View.GONE
                    btnSecondary.visibility = View.VISIBLE
                    btnSecondary.text = "Detail"

                    btnSecondary.setOnClickListener { listener.onDetailClicked(item) }
                }
            }
        }

        private fun formatRupiah(value: Int): String {
            return "Rp %,d".format(value).replace(",", ".")
        }
    }
        fun updateStatusLocal(bookingId: Int, newStatus: String) {
        val index = items.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            val old = items[index]
            items[index] = old.copy(
                status = newStatus
                // paymentStatus biarin, masih "unpaid"
            )
            notifyItemChanged(index)
        }
    }

}

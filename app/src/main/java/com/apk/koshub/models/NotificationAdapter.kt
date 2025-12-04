package com.apk.koshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.models.NotificationItem
import com.apk.koshub.utils.TimeAgo

class NotificationAdapter(
    private val items: MutableList<NotificationItem> = mutableListOf(),
    private val onItemClick: (NotificationItem) -> Unit = {}
) : RecyclerView.Adapter<NotificationAdapter.NotifVH>() {

    inner class NotifVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconNotif: ImageView = itemView.findViewById(R.id.iconNotif)
        val redDot: View = itemView.findViewById(R.id.redDot)
        val title: TextView = itemView.findViewById(R.id.tvNotifTitle)
        val message: TextView = itemView.findViewById(R.id.tvNotifMessage)
        val time: TextView = itemView.findViewById(R.id.tvNotifTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotifVH(v)
    }

    override fun onBindViewHolder(holder: NotifVH, position: Int) {
        val item = items[position]

        holder.title.text = item.title ?: "Notifikasi"
        holder.message.text = item.message ?: ""
        holder.time.text = item.created_at?.let { TimeAgo.getTimeAgo(it) } ?: ""

        holder.redDot.visibility = if (item.is_read == 0) View.VISIBLE else View.GONE

        when (item.type) {
            "booking_approved" -> holder.iconNotif.setImageResource(R.drawable.ic_mail)
            "booking_rejected" -> holder.iconNotif.setImageResource(R.drawable.ic_mail)
            else -> holder.iconNotif.setImageResource(R.drawable.ic_mail)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<NotificationItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}

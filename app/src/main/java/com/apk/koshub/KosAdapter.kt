package com.apk.koshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.models.KosItem

class KosAdapter(
    private var kosList: List<KosItem>,
    private val onItemClick: (KosItem) -> Unit
) : RecyclerView.Adapter<KosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivKosImage: ImageView = view.findViewById(R.id.ivKosImage)
        val tvNamaKos: TextView = view.findViewById(R.id.tvNamaKos)
        val tvLokasi: TextView = view.findViewById(R.id.tvLokasi)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kos = kosList[position]
        holder.ivKosImage.setImageResource(kos.gambar)
        holder.tvNamaKos.text = kos.nama
        holder.tvLokasi.text = kos.lokasi
        holder.tvHarga.text = kos.harga

        holder.itemView.setOnClickListener { onItemClick(kos) }
    }

    override fun getItemCount() = kosList.size

    // Fungsi untuk filter list (dipanggil dari search/filter)
    fun updateList(newList: List<KosItem>) {
        kosList = newList
        notifyDataSetChanged()
    }
}
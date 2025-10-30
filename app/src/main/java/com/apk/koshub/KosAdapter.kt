package com.apk.koshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.models.KosItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class KosAdapter(
    private var kosList: List<KosItem>,
    private val onItemClick: (KosItem) -> Unit
) : RecyclerView.Adapter<KosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivKosImage: ImageView = view.findViewById(R.id.ivKosImage)
        val tvNamaKos: TextView = view.findViewById(R.id.tvNamaKos)
        val tvLokasi: TextView = view.findViewById(R.id.tvLokasi)
        val tvHarga: TextView = view.findViewById(R.id.tvHarga)
        val btnLihatDetail: Button = view.findViewById(R.id.btnLihatDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kos = kosList[position]

        // Load gambar dari drawable dengan efek fade-in
        Glide.with(holder.itemView.context)
            .load(kos.gambar)
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .into(holder.ivKosImage)

        holder.tvNamaKos.text = kos.nama
        holder.tvLokasi.text = kos.lokasi
        holder.tvHarga.text = kos.harga

        holder.btnLihatDetail.setOnClickListener { onItemClick(kos) }
        holder.itemView.setOnClickListener { onItemClick(kos) }
    }

    override fun getItemCount(): Int = kosList.size

    fun updateList(newList: List<KosItem>) {
        kosList = newList
        notifyDataSetChanged()
    }
}

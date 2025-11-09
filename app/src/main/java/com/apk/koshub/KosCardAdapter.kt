package com.apk.koshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.models.KosItemCard
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class KosCardAdapter(
    private var kosList: MutableList<KosItemCard>,
    private val onItemClick: (KosItemCard) -> Unit
) : RecyclerView.Adapter<KosCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgKos: ImageView = view.findViewById(R.id.imgKos)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvNamaKos: TextView = view.findViewById(R.id.tvNamaKos)
        val tvAlamatKos: TextView = view.findViewById(R.id.tvAlamatKos)
        val tvFasilitasKos: TextView = view.findViewById(R.id.tvFasilitasKos)
        val tvHargaKos: TextView = view.findViewById(R.id.tvHargaKos)
        val btnDetail: TextView = view.findViewById(R.id.btnDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kos_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kos = kosList[position]

        // Gambar kos
        Glide.with(holder.itemView.context)
            .load(kos.gambar)
            .placeholder(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .into(holder.imgKos)

        // Text binding
        holder.tvNamaKos.text = kos.nama
        holder.tvAlamatKos.text = kos.lokasi
        holder.tvFasilitasKos.text = kos.fasilitas
        holder.tvHargaKos.text = kos.harga
        holder.tvRating.text = kos.rating.toString()

        // Status awal favorite
        holder.btnFavorite.setImageResource(
            if (kos.isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )

        // Klik "Lihat Detail" atau item card
        holder.btnDetail.setOnClickListener { onItemClick(kos) }
        holder.itemView.setOnClickListener { onItemClick(kos) }

        // Klik tombol favorite
        holder.btnFavorite.setOnClickListener {
            kos.isFavorite = !kos.isFavorite
            holder.btnFavorite.setImageResource(
                if (kos.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )
            // opsional: update list supaya status tersimpan
            kosList[position] = kos
        }
    }

    override fun getItemCount(): Int = kosList.size

    fun updateList(newList: List<KosItemCard>) {
        kosList.clear()
        kosList.addAll(newList)
        notifyDataSetChanged()
    }
}

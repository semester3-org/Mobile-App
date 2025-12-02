package com.apk.koshub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.BasicResponse
import com.apk.koshub.models.KosItemCard
import com.apk.koshub.utils.SharedPrefHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KosCardAdapter(
    private val kosList: MutableList<KosItemCard>,
    private val onItemClick: (KosItemCard) -> Unit
) : RecyclerView.Adapter<KosCardAdapter.ViewHolder>() {

    private lateinit var pref: SharedPrefHelper
    private lateinit var db: DatabaseHelper

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgKos: ImageView = view.findViewById(R.id.imgKos)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvNamaKos: TextView = view.findViewById(R.id.tvNamaKos)
        val tvAlamatKos: TextView = view.findViewById(R.id.tvAlamatKos)
        val tvHargaKos: TextView = view.findViewById(R.id.tvHargaKos)
        val btnDetail: TextView = view.findViewById(R.id.btnDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kos_card, parent, false)

        if (!::pref.isInitialized) pref = SharedPrefHelper(parent.context)
        if (!::db.isInitialized) db = DatabaseHelper(parent.context)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < 0 || position >= kosList.size) return
        val kos = kosList[position]
        val context = holder.itemView.context

        Glide.with(context)
            .load(kos.gambar)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.imgKos)

        holder.tvNamaKos.text = kos.nama
        holder.tvAlamatKos.text = kos.lokasi
        holder.tvHargaKos.text = kos.harga
        holder.tvRating.text = kos.rating.toString()

        holder.btnFavorite.setImageResource(
            if (kos.isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )

        holder.itemView.setOnClickListener { onItemClick(kos) }
        holder.btnDetail.setOnClickListener { onItemClick(kos) }

        holder.btnFavorite.setOnClickListener {
            if (!pref.isLoggedIn()) {
                Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = db.getUser()
            if (user == null) {
                Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldStatus = kos.isFavorite
            kos.isFavorite = !kos.isFavorite

            notifyItemChanged(pos)

            val call = if (kos.isFavorite) {
                ApiClient.api.addFavorite(
                    userId = user.id,
                    kosId = kos.id
                )
            } else {
                ApiClient.api.removeFavorite(
                    userId = user.id,
                    kosId = kos.id
                )
            }

            call.enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    val body = response.body()

                    if (body == null || !body.success) {
                        kos.isFavorite = oldStatus
                        notifyItemChanged(pos)

                        Toast.makeText(
                            context,
                            "Gagal: ${body?.message ?: "Terjadi kesalahan"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    kos.isFavorite = oldStatus
                    notifyItemChanged(pos)

                    Toast.makeText(
                        context,
                        "Network Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    override fun getItemCount(): Int = kosList.size

    fun updateList(newList: List<KosItemCard>) {
        kosList.clear()
        kosList.addAll(newList)
        notifyDataSetChanged()
    }
}

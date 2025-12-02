package com.apk.koshub.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.apk.koshub.R

class KosPhotoPagerAdapter : RecyclerView.Adapter<KosPhotoPagerAdapter.VH>() {

    private val items = mutableListOf<String>()

    fun submit(urls: List<String>) {
        items.clear()
        if (urls.isEmpty()) {
            // biar tetap ada 1 slide placeholder
            items.add("")
        } else {
            items.addAll(urls)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val iv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kos_photo, parent, false) as ImageView
        return VH(iv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = items[position]

        if (url.isBlank()) {
            holder.iv.setImageResource(R.drawable.sampel_kos)
            return
        }

        Log.d("PHOTO_SLIDER", "load url=$url")

        holder.iv.load(url) {
            placeholder(R.drawable.sampel_kos)
            error(R.drawable.sampel_kos)
            crossfade(true)
            listener(
                onSuccess = { _, result ->
                    val src = (result as? SuccessResult)?.dataSource
                    Log.d("PHOTO_SLIDER", "success url=$url source=$src")
                },
                onError = { _, result ->
                    val throwable = (result as? ErrorResult)?.throwable
                    Log.e("PHOTO_SLIDER", "error url=$url err=${throwable?.message}", throwable)
                }
            )
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)
}

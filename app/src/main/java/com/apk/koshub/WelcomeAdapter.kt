package com.apk.koshub // ganti sesuai package kamu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apk.koshub.R // sesuaikan package import ini

class WelcomeAdapter(
    private val context: Context,
    private val items: List<WelcomeItem>
) : RecyclerView.Adapter<WelcomeAdapter.WelcomeViewHolder>() {

    data class WelcomeItem(
        val imageRes: Int,
        val title: String,
        val desc: String
    )

    inner class WelcomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageSlide)
        val title: TextView = view.findViewById(R.id.titleSlide)
        val desc: TextView = view.findViewById(R.id.descSlide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WelcomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_welcome, parent, false)
        return WelcomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: WelcomeViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageRes)
        holder.title.text = item.title
        holder.desc.text = item.desc
    }

    override fun getItemCount(): Int = items.size
}

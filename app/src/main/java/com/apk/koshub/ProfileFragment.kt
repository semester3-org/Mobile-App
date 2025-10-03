package com.apk.koshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apk.koshub.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inisialisasi views
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        val tvNamaUser: TextView = view.findViewById(R.id.tvNamaUser)
        val tvEmailUser: TextView = view.findViewById(R.id.tvEmailUser)

        // Dummy data
        tvNamaUser.text = "Alexsandra Zenair"
        tvEmailUser.text = "zenair.alexa@gmail.com"


        // Button listeners (dummy Toast; nanti ganti dengan Intent atau dialog)
        view.findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(context, "Edit Profil - Buka form edit", Toast.LENGTH_SHORT).show()
            // Contoh: startActivity(Intent(context, EditProfileActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnRiwayatPencarian).setOnClickListener {
            Toast.makeText(context, "Riwayat Pencarian", Toast.LENGTH_SHORT).show()
            // Contoh: load fragment atau activity riwayat
        }

        view.findViewById<Button>(R.id.btnKosFavorit).setOnClickListener {
            Toast.makeText(context, "Kos Favorit - Load dari DB", Toast.LENGTH_SHORT).show()
            // Contoh: navigasi ke FavoriteFragment atau activity detail
        }

        view.findViewById<Button>(R.id.btnNotifikasi).setOnClickListener {
            Toast.makeText(context, "Notifikasi", Toast.LENGTH_SHORT).show()
            // Contoh: buka activity notifikasi
        }

        view.findViewById<Button>(R.id.btnBahasa).setOnClickListener {
            Toast.makeText(context, "Bahasa - Ganti ke English?", Toast.LENGTH_SHORT).show()
            // Contoh: dialog pilihan bahasa
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Toast.makeText(context, "Logout - Konfirmasi?", Toast.LENGTH_SHORT).show()
            // Contoh: FirebaseAuth.getInstance().signOut(); lalu intent ke LoginActivity
        }

        return view
    }
}
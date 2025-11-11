package com.apk.koshub.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.apk.koshub.LoginActivity
import com.apk.koshub.R
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pref: SharedPrefHelper
    private lateinit var ivProfile: ImageView
    private lateinit var tvNamaUser: TextView
    private lateinit var tvEmailUser: TextView
    private lateinit var btnLogout: Button
    private lateinit var cardEditProfile: View
    private lateinit var cardBahasa: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 🔹 Inisialisasi
        dbHelper = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        ivProfile = view.findViewById(R.id.ivProfile)
        tvNamaUser = view.findViewById(R.id.tvNamaUser)
        tvEmailUser = view.findViewById(R.id.tvEmailUser)
        btnLogout = view.findViewById(R.id.btnLogout)
        cardEditProfile = view.findViewById(R.id.cardEditProfile)
        cardBahasa = view.findViewById(R.id.cardBahasa)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 🔐 Cek apakah user sudah login
        if (pref.isLoggedIn()) {
            loadUserData()

            cardEditProfile.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, EditProfileFragment())
                    .addToBackStack(null)
                    .commit()
                bottomNav.visibility = View.GONE
            }

            cardBahasa.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GantiBahasaFragment())
                    .addToBackStack(null)
                    .commit()
                bottomNav.visibility = View.GONE
            }

            btnLogout.setOnClickListener {
                dbHelper.logoutUser()
                pref.clearSession()
                Toast.makeText(context, "Berhasil logout", Toast.LENGTH_SHORT).show()

                // 🚪 Redirect ke LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

        } else {
            // 🚫 Belum login → arahkan ke LoginActivity
            Toast.makeText(context, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (pref.isLoggedIn()) loadUserData()
    }

    private fun loadUserData() {
        val user = dbHelper.getUser()
        if (user != null) {
            tvNamaUser.text = user.full_name.ifEmpty { user.username }
            tvEmailUser.text = user.email

            if (!user.profile_image.isNullOrEmpty()) {
                ivProfile.setImageURI(Uri.parse(user.profile_image))
            } else {
                ivProfile.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            // Jika tidak ada user di database → logout otomatis
            pref.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}

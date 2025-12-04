package com.apk.koshub.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.apk.koshub.LoginActivity
import com.apk.koshub.R
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.BasicResponse
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pref: SharedPrefHelper

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var tvNamaUser: TextView
    private lateinit var tvEmailUser: TextView
    private lateinit var cardEditProfile: View
    private lateinit var cardFavorit: View
    private lateinit var cardRiwayat: View
    private lateinit var cardBahasa: View
    private lateinit var btnLogout: Button
    private lateinit var switchNotif: Switch
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        dbHelper = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        ivProfile = view.findViewById(R.id.ivProfile)
        tvNamaUser = view.findViewById(R.id.tvNamaUser)
        tvEmailUser = view.findViewById(R.id.tvEmailUser)
        cardEditProfile = view.findViewById(R.id.cardEditProfile)
        cardFavorit = view.findViewById(R.id.cardFavorit)
        cardRiwayat = view.findViewById(R.id.cardRiwayat)
        cardBahasa = view.findViewById(R.id.cardBahasa)
        btnLogout = view.findViewById(R.id.btnLogout)
        switchNotif = view.findViewById(R.id.switchNotifikasi)

        bottomNav = requireActivity().findViewById(R.id.bottom_navigation)

        if (!pref.isLoggedIn()) {
            redirectToLogin()
            return view
        }

        // Load initial data
        loadUserData()

        // Setup switch notif
        setupNotifSwitch(switchNotif)

        // ============ NAVIGATION HANDLER ============
        cardEditProfile.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, EditProfileFragment())
                addToBackStack(null)
            }
        }

        cardFavorit.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, FavoriteFragment())
                addToBackStack(null)
            }
        }

        cardRiwayat.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur belum aktif", Toast.LENGTH_SHORT).show()
        }

        cardBahasa.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur bahasa belum aktif", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            dbHelper.logoutUser()
            pref.clearSession()
            redirectToLogin()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (pref.isLoggedIn()) loadUserData()
    }

    // ===============================================================
    // LOAD USER DATA
    // ===============================================================
    private fun loadUserData() {
        val user = dbHelper.getUser()

        if (user == null) {
            redirectToLogin()
            return
        }

        tvNamaUser.text = user.full_name.ifEmpty { user.username }
        tvEmailUser.text = user.email

        if (!user.profile_image.isNullOrEmpty()) {
            val file = File(user.profile_image)
            if (file.exists()) {
                ivProfile.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            } else {
                ivProfile.setImageResource(R.drawable.ic_default_profile)
            }
        } else {
            ivProfile.setImageResource(R.drawable.ic_default_profile)
        }
    }

    // ===============================================================
    // SWITCH NOTIFIKASI
    // ===============================================================
    private fun setupNotifSwitch(switchNotif: Switch) {
        val user = dbHelper.getUser() ?: return

        // Set awal dari shared pref
        switchNotif.isChecked = pref.isNotifEnabled()

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            val enabled = if (isChecked) 1 else 0

            // Simpan lokal
            pref.setNotifEnabled(isChecked)

            // Kirim ke server
            ApiClient.api.updateNotificationPref(
                userId = user.id,
                enabled = enabled
            ).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    Toast.makeText(
                        requireContext(),
                        if (isChecked) "Notifikasi diaktifkan" else "Notifikasi dimatikan",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan preferensi notifikasi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    // ===============================================================
    // REDIRECT LOGIN
    // ===============================================================
    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}

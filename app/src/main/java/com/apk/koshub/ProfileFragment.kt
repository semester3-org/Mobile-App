package com.apk.koshub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        dbHelper = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        ivProfile = view.findViewById(R.id.ivProfile)
        tvNamaUser = view.findViewById(R.id.tvNamaUser)
        tvEmailUser = view.findViewById(R.id.tvEmailUser)

        val cardEditProfile = view.findViewById<View>(R.id.cardEditProfile)
        val cardBahasa = view.findViewById<View>(R.id.cardBahasa)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        loadUserData()

        cardEditProfile.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility =
                View.GONE
        }

        cardBahasa.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GantiBahasaFragment())
                .addToBackStack(null)
                .commit()
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility =
                View.GONE
        }

        btnLogout.setOnClickListener {
            dbHelper.logoutUser()
            pref.clearSession()
            Toast.makeText(context, "Berhasil logout", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // biar data langsung update pas balik dari edit
        loadUserData()
    }

    private fun loadUserData() {
        val user = dbHelper.getUser()
        if (user != null) {
            tvNamaUser.text = user.full_name
            tvEmailUser.text = user.email
            if (!user.profile_image.isNullOrEmpty()) {
                ivProfile.setImageURI(android.net.Uri.parse(user.profile_image))
            } else {
                ivProfile.setImageResource(R.drawable.ic_default_profile)
            }
        }
    }
}

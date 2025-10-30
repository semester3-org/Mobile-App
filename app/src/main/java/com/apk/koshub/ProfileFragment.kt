package com.apk.koshub.fragments
import com.apk.koshub.fragments.GantiBahasaFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.apk.koshub.LoginActivity
import com.apk.koshub.R
import com.apk.koshub.utils.SharedPrefHelper

class ProfileFragment : Fragment() {

    private lateinit var pref: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        pref = SharedPrefHelper(requireContext())

        // ------------------- Switch Notifikasi -------------------
        val switchNotifikasi: Switch = view.findViewById(R.id.switchNotifikasi)
        switchNotifikasi.isChecked = pref.isNotifEnabled()

        switchNotifikasi.setOnCheckedChangeListener { _, isChecked ->
            val wasEnabled = pref.isNotifEnabled()
            if (isChecked != wasEnabled) {
                showNotifDialog(switchNotifikasi, isChecked)
            }
        }

        // ------------------- Profile Info -------------------
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        val tvNamaUser: TextView = view.findViewById(R.id.tvNamaUser)
        val tvEmailUser: TextView = view.findViewById(R.id.tvEmailUser)

        tvNamaUser.text = "Alexsandra Zenair"
        tvEmailUser.text = "zenair.4lexsa@gmail.com"

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // ------------------- Card Navigasi -------------------
        view.findViewById<View>(R.id.cardEditProfile).setOnClickListener {
            Toast.makeText(context, "Edit Profil - Buka form edit", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.cardRiwayat).setOnClickListener {
            Toast.makeText(context, "Riwayat Pencarian", Toast.LENGTH_SHORT).show()
            // Navigasi ke Fragment Riwayat bisa disini
        }

        view.findViewById<View>(R.id.cardFavorit).setOnClickListener {
            Toast.makeText(context, "Kos Favorit - Tampilkan daftar favorit", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.cardNotifikasi).setOnClickListener {
            switchNotifikasi.isChecked = !switchNotifikasi.isChecked
        }

        view.findViewById<View>(R.id.cardBahasa).setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,GantiBahasaFragment())
                .addToBackStack(null)
                .commit()

            //hide bottom_nav
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.GONE
        }

        // ------------------- Logout -------------------
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }

        return view
    }

    // ------------------- Dialog Notifikasi -------------------
    private fun showNotifDialog(switch: Switch, enable: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_notifikasi, null)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnYa = dialogView.findViewById<Button>(R.id.btnYa)

        tvMessage.text = if (enable) "Aktifkan notifikasi?" else "Nonaktifkan notifikasi?"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnBatal.setOnClickListener {
            switch.isChecked = !enable
            dialog.dismiss()
        }

        btnYa.setOnClickListener {
            pref.setNotifEnabled(enable)
            Toast.makeText(
                requireContext(),
                "Notifikasi ${if (enable) "diaktifkan" else "dinonaktifkan"}",
                Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    // ------------------- Dialog Logout -------------------
    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val btnYa = dialogView.findViewById<Button>(R.id.btnYa)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnYa.setOnClickListener {
            pref.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}

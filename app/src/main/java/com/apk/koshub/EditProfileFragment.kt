package com.apk.koshub.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.apk.koshub.R
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.User
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class EditProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pref: SharedPrefHelper
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        dbHelper = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnEditPhoto = view.findViewById<ImageButton>(R.id.btnEditPhoto)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        val etNama = view.findViewById<EditText>(R.id.etNamaLengkap)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etTelepon = view.findViewById<EditText>(R.id.etTelepon)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpanPerubahan)

        // Ambil data user
        val user = dbHelper.getUser()
        user?.let {
            etNama.setText(it.full_name)
            etUsername.setText(it.username)
            etTelepon.setText(it.phone)
            etEmail.setText(it.email)

            if (!it.profile_image.isNullOrEmpty()) {
                ivProfile.setImageURI(Uri.parse(it.profile_image))
            } else {
                ivProfile.setImageResource(R.drawable.ic_default_profile)
            }
        }

        // Tombol kembali
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility =
                View.VISIBLE
        }

        // Pilih foto profil dari galeri
        btnEditPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        // Tombol simpan
        btnSimpan.setOnClickListener {
            val updatedUser = User(
                id = user?.id ?: 0,
                username = etUsername.text.toString(),
                email = user?.email ?: "",
                full_name = etNama.text.toString(),
                phone = etTelepon.text.toString(),
                user_type = user?.user_type ?: "user",
                profile_image = selectedImageUri?.toString() ?: user?.profile_image
            )

            val success = dbHelper.updateUser(updatedUser)

            if (success) {
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(context, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            view?.findViewById<ImageView>(R.id.ivProfile)?.setImageURI(selectedImageUri)
        }
    }
}

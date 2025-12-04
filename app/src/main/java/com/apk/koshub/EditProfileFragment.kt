package com.apk.koshub.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.apk.koshub.R
import com.apk.koshub.api.ApiClient
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.User
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var pref: SharedPrefHelper

    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var etNama: EditText
    private lateinit var etUsername: EditText
    private lateinit var etTelepon: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnEditPhoto: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        dbHelper = DatabaseHelper(requireContext())
        pref = SharedPrefHelper(requireContext())

        ivProfile = view.findViewById(R.id.ivProfile)
        etNama = view.findViewById(R.id.etNamaLengkap)
        etUsername = view.findViewById(R.id.etUsername)
        etTelepon = view.findViewById(R.id.etTelepon)
        etEmail = view.findViewById(R.id.etEmail)
        btnSimpan = view.findViewById(R.id.btnSimpanPerubahan)
        btnBack = view.findViewById(R.id.btnBack)
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto)

        currentUser = dbHelper.getUser()

        loadUserData()

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnEditPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        btnSimpan.setOnClickListener {
            saveProfileToServer()
        }

        return view
    }

    private fun loadUserData() {
        val user = currentUser ?: return

        etNama.setText(user.full_name)
        etUsername.setText(user.username)
        etTelepon.setText(user.phone)
        etEmail.setText(user.email)

        // preview profile picture
        if (!user.profile_image.isNullOrEmpty()) {
            val file = File(user.profile_image)
            if (file.exists()) {
                ivProfile.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            } else {
                ivProfile.setImageResource(R.drawable.ic_default_profile)
            }
        }
    }

    @Deprecated("Deprecated in Android 13+")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            ivProfile.setImageURI(selectedImageUri)
        }
    }

    private fun saveProfileToServer() {
        val user = currentUser ?: return
        val loading = Toast.makeText(requireContext(), "Menyimpan...", Toast.LENGTH_SHORT)
        loading.show()

        val nameBody = etNama.text.toString().toRequestBody("text/plain".toMediaType())
        val usernameBody = etUsername.text.toString().toRequestBody("text/plain".toMediaType())
        val emailBody = etEmail.text.toString().toRequestBody("text/plain".toMediaType())
        val phoneBody = etTelepon.text.toString().toRequestBody("text/plain".toMediaType())
        val idBody = user.id.toString().toRequestBody("text/plain".toMediaType())

        var imagePart: MultipartBody.Part? = null

        selectedImageUri?.let { uri ->
            val file = File(requireContext().cacheDir, "profile_${user.id}.jpg")

            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("profile_picture", file.name, reqFile)

            // simpan ke SQLite path lokal (preview)
            currentUser = currentUser!!.copy(profile_image = file.absolutePath)
            dbHelper.updateUser(currentUser!!)
        }

        lifecycleScope.launch {
            try {
                val res = ApiClient.api.updateProfile(
                    idBody,
                    usernameBody,
                    nameBody,
                    emailBody,
                    phoneBody,
                    imagePart
                )

                Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()

                // update SQLite biar UI profile berubah
                currentUser = currentUser!!.copy(
                    full_name = etNama.text.toString(),
                    username = etUsername.text.toString(),
                    phone = etTelepon.text.toString(),
                    email = etEmail.text.toString()
                )

                dbHelper.updateUser(currentUser!!)

                parentFragmentManager.popBackStack()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Gagal update: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

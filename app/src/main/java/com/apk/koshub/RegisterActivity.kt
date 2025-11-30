package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.api.ApiClient
import com.apk.koshub.api.ApiService
import com.apk.koshub.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.animation.DecelerateInterpolator

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullName: EditText
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvLogIn: TextView

    private lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        api = ApiClient.api

        // View utama form + logo (buat animasi)
        val logo = findViewById<ImageView>(R.id.logo)
        val registerContainer = findViewById<LinearLayout>(R.id.registerContainer)

        // Form fields (ID sudah disamain dengan layout baru)
        fullName = findViewById(R.id.fullName)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        phone = findViewById(R.id.phone)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogIn = findViewById(R.id.tvLogIn)

        logo.alpha = 0f
        logo.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        // 🔹 Form pop-up dari bawah (slide up + overshoot dikit)
        registerContainer.alpha = 0f
        registerContainer.translationY = 250f
        registerContainer.animate()
            .alpha(0.9f) // 🧊 tetap sedikit transparan
            .translationY(0f)
            .setDuration(550)
            .setStartDelay(150)
            .setInterpolator(DecelerateInterpolator(1.1f))
            .start()

        // Tombol Register
        btnRegister.setOnClickListener {
            val fullNameStr = fullName.text.toString().trim()
            val usernameStr = username.text.toString().trim()
            val emailStr = email.text.toString().trim()
            val phoneStr = phone.text.toString().trim()
            val passwordStr = password.text.toString().trim()
            val confirmPasswordStr = confirmPassword.text.toString().trim()

            if (fullNameStr.isEmpty() || usernameStr.isEmpty() || emailStr.isEmpty()
                || phoneStr.isEmpty() || passwordStr.isEmpty() || confirmPasswordStr.isEmpty()
            ) {
                toast("Semua field harus diisi!")
                return@setOnClickListener
            }

            if (!termsCheckbox.isChecked) {
                toast("Setujui syarat dan ketentuan terlebih dahulu!")
                return@setOnClickListener
            }

            if (passwordStr != confirmPasswordStr) {
                toast("Password dan konfirmasi tidak sama!")
                return@setOnClickListener
            }

            registerUser(fullNameStr, usernameStr, emailStr, phoneStr, passwordStr)
        }

        // Pindah ke Login
        tvLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String
    ) {
        val data = mapOf(
            "full_name" to fullName,
            "username" to username,
            "email" to email,
            "phone" to phone,
            "password" to password,
            "user_type" to "user"
        )

        api.register(data).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.status == "success") {
                        toast("Registrasi berhasil! Silakan login.")
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        toast(res.message ?: "Registrasi gagal.")
                    }
                } else {
                    toast("Gagal terhubung ke server.")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                toast("Error: ${t.message}")
            }
        })
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

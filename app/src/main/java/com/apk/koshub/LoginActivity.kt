package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.api.ApiClient
import com.apk.koshub.api.ApiService
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.UserResponse
import com.apk.koshub.utils.SharedPrefHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvCreateOne: TextView
    private lateinit var db: DatabaseHelper
    private lateinit var api: ApiService
    private lateinit var pref: SharedPrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🧩 Inisialisasi view sesuai ID di XML
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvCreateOne = findViewById(R.id.tvCreateOne)

        val logo = findViewById<ImageView>(R.id.logo)
        val loginContainer = findViewById<LinearLayout>(R.id.loginContainer)

        // 🎞️ Animasi logo
        logo?.apply {
            alpha = 0f
            animate().alpha(1f).setDuration(600).start()
        }

        // 🎞️ Animasi form login
        loginContainer?.apply {
            alpha = 0f
            translationY = 200f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(550)
                .setStartDelay(150)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // 🧠 Init Database, Pref, dan API
        db = DatabaseHelper(this)
        pref = SharedPrefHelper(this)
        api = ApiClient.instance

        // 🔁 Pindah ke Register
        tvCreateOne.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // 🔐 Tombol Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            doLogin(email, password)
        }
    }

    private fun doLogin(email: String, password: String) {
        val body = mapOf(
            "email" to email,
            "password" to password
        )

        api.login(body).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.d("LOGIN_DEBUG", "Response code: ${response.code()}")
                Log.d("LOGIN_DEBUG", "Response body: ${response.body()}")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.status == "success") {
                        val user = userResponse.user
                        user?.let {
                            // 💾 Simpan user ke SQLite
                            db.insertUser(it)
                            Log.d("DB_DEBUG", "User saved: ${it.email}")

                            // ✅ Simpan juga ke SharedPreferences
                            pref.saveUserData(
                                id = it.id,
                                name = it.full_name ?: it.username,
                                email = it.email,
                                phone = it.phone
                            )
                        }

                        Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT)
                            .show()

                        // 🚀 Pindah ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            userResponse?.message ?: "Email atau password salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login gagal, kode: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("LOGIN_ERROR", "Error: ${t.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "Gagal terhubung ke server: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}

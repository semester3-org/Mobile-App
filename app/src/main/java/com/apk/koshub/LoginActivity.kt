package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.api.ApiClient
import com.apk.koshub.api.ApiService
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.User
import com.apk.koshub.models.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var btnLogin: Button
    private lateinit var signInText: TextView
    private lateinit var db: DatabaseHelper
    private lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi view
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)
        signInText = findViewById(R.id.SignUpButton)

        // Inisialisasi database helper & Retrofit
        db = DatabaseHelper(this)
        api = ApiClient.apiService

        // Tombol Sign Up → pindah ke RegisterActivity
        signInText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        // Tombol Login ditekan
        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                emailInput.error = "Email wajib diisi"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password wajib diisi"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // Jalankan proses login
            doLogin(email, password)
        }
    }

    private fun doLogin(email: String, password: String) {
        val body = mapOf(
            "email" to email,
            "password" to password
        )

        // Kirim request POST (JSON body)
        api.login(body).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.d("LOGIN_DEBUG", "Response code: ${response.code()}")
                Log.d("LOGIN_DEBUG", "Response body: ${response.body()}")

                if (response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse?.status == "success") {
                        val user = userResponse.user

                        // Simpan user ke SQLite
                        user?.let {
                            db.insertUser(it)
                            Log.d("DB_DEBUG", "User saved: ${it.email}")
                        }

                        Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT)
                            .show()

                        // Pindah ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_EMAIL", email)
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
    }}

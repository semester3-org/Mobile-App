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
import com.apk.koshub.models.User
import com.apk.koshub.models.UserResponse
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

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

        // 🎞️ Animasi logo dan form login
        animateLogoAndLoginForm(logo, loginContainer)

        // 🧠 Init Database, Pref, dan API
        db = DatabaseHelper(this)
        pref = SharedPrefHelper(this)
        api = ApiClient.api

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
                etEmail.error = "Email Wajib Diisi"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password Wajib Diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            doLogin(email, password)
        }

        // Inisialisasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Web Client ID yang sudah disimpan di strings.xml
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Tombol Google Login
        findViewById<LinearLayout>(R.id.btnGoogleLogin).setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun animateLogoAndLoginForm(logo: ImageView?, loginContainer: LinearLayout?) {
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
    }

    private fun signInWithGoogle() {
        Log.d("GoogleSignIn", "Starting Google Sign-In...")
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            Log.d("GoogleSignIn", "Received result from Google Sign-In")
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    Log.d("GoogleSignIn", "Google Sign-In successful: ${account.email}")
                    handleGoogleSignIn(account)
                }
            } catch (e: ApiException) {
                // Debugging: Log error details
                Log.e("GoogleSignIn", "signInResult:failed code=" + e.statusCode)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("GoogleSignInError", "Error code: ${e.statusCode}")
            }
        }
    }

    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        Log.d("GoogleSignIn", "Handling Google Sign-In for account: ${account.email}")

        // Simpan data pengguna dari akun Google
        val email = account.email
        val name = account.displayName
        val profileImage = account.photoUrl?.toString()

        // Membuat objek User
        val user = User(
            id = 0,  // ID di-generate oleh database
            username = name ?: "",
            email = email ?: "",
            full_name = name ?: "",
            phone = "",  // Untuk sementara kosong
            profile_image = profileImage,
            user_type = "user"
        )

        // Simpan user ke database
        db.insertUser(user)

        // Simpan data ke SharedPreferences
        pref.saveUserData(
            id = user.id,
            name = user.full_name,
            email = user.email,
            phone = user.phone,
            profileImage = user.profile_image
        )

        // Pindah ke MainActivity
        Log.d("GoogleSignIn", "Navigating to MainActivity...")
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
                    if (userResponse?.code == 200) {
                        val user = userResponse.user
                        user?.let {
                            // Simpan user ke SQLite
                            db.insertUser(it)
                            Log.d("DB_DEBUG", "User saved: ${it.email}")

                            // Simpan juga ke SharedPreferences
                            pref.saveUserData(
                                id = it.id,
                                name = it.full_name ?: it.username,
                                email = it.email,
                                phone = it.phone,
                                profileImage = it.profile_image
                            )
                        }

                        Toast.makeText(this@LoginActivity, userResponse.message + ", " + user!!.full_name, Toast.LENGTH_SHORT)
                            .show()

                        // Pindah ke MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            userResponse?.message ?: "Email Atau Password Salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login Gagal, kode: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("LOGIN_ERROR", "Error: ${t.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "Gagal Terhubung Ke Server: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}

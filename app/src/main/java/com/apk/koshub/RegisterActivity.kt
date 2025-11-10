package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.db.DatabaseHelper
import com.apk.koshub.models.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var fullName: EditText
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var btnRegister: Button
    private lateinit var tvLogIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        // === Inisialisasi View ===
        fullName = findViewById(R.id.fullName)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        phone = findViewById(R.id.phone)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogIn = findViewById(R.id.tvLogIn)

        // === Tombol Sign Up ===
        btnRegister.setOnClickListener {
            val fullNameStr = fullName.text.toString().trim()
            val usernameStr = username.text.toString().trim()
            val emailStr = email.text.toString().trim()
            val phoneStr = phone.text.toString().trim()
            val passwordStr = password.text.toString().trim()
            val confirmPasswordStr = confirmPassword.text.toString().trim()

            // Validasi input
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

            // Buat objek user
            val user = User(
                id = 1, // bisa diatur otomatis
                username = usernameStr,
                email = emailStr,
                full_name = fullNameStr,
                phone = phoneStr,
                user_type = "user" // default
            )

            // Simpan ke database lokal
            db.insertUser(user)

            toast("Registrasi berhasil! Silakan login.")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // === Tombol Log In (kembali ke halaman login) ===
        tvLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

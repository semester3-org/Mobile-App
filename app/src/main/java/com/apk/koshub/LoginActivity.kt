package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty()) {
                emailInput.error = "Email is required"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            // TODO: Implement API call to validate login with backend MySQL

            // For demo, assume login success if email == "user@example.com" and password == "123456"
            if (email == "arya@bintang.com" && password == "123456") {
                val intent = Intent(this,  MainActivity::class.java)
                intent.putExtra("USER_EMAIL", email)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
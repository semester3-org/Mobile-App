package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullNameInput = findViewById(R.id.fullName)
        usernameInput = findViewById(R.id.username)
        emailInput = findViewById(R.id.email)
        phoneInput = findViewById(R.id.phone)
        passwordInput = findViewById(R.id.password)
        confirmPasswordInput = findViewById(R.id.confirmPassword)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()
            val agreedTerms = termsCheckbox.isChecked

            if (fullName.isEmpty()) {
                fullNameInput.error = "Full name is required"
                fullNameInput.requestFocus()
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                usernameInput.error = "Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailInput.error = "Email is required"
                emailInput.requestFocus()
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                phoneInput.error = "Phone number is required"
                phoneInput.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                confirmPasswordInput.error = "Please confirm your password"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                confirmPasswordInput.requestFocus()
                return@setOnClickListener
            }
            if (!agreedTerms) {
                Toast.makeText(this, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Implement API call to register user in MySQL backend

            // For demo, assume registration success
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
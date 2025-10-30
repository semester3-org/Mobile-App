package com.apk.koshub

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val logo = findViewById<ImageView>(R.id.logo)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeIn)


        lifecycleScope.launch {
            delay(2000)
            startActivity(Intent(this@SplashActivity,  WelcomeActivity::class.java))
            finish()
        }
    }
}

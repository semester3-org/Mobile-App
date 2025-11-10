package com.apk.koshub

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class WelcomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: WelcomeAdapter
    private lateinit var dotLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // === Membuat status bar transparan agar gambar sampai ke atas ===
        window.apply {
            decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            statusBarColor = Color.TRANSPARENT
        }

        viewPager = findViewById(R.id.viewPager)
        dotLayout = findViewById(R.id.dotIndicator)

        val items = listOf(
            WelcomeAdapter.WelcomeItem(
                R.drawable.welcome_1,
                "Temukan Kos Impianmu",
                "Jelajahi berbagai pilihan kos dengan mudah. Dapatkan tempat tinggal yang nyaman, sesuai kebutuhan dan budget kamu."
            ),
            WelcomeAdapter.WelcomeItem(
                R.drawable.welcome_2,
                "Sewa Lebih Praktis",
                "Booking kamar cukup lewat aplikasi. Semua pembayaran tercatat otomatis, tanpa repot datang langsung."
            ),
            WelcomeAdapter.WelcomeItem(
                R.drawable.welcome_3,
                "Mulai dengan KosHub Hari ini",
                "Dengan KosHub, semua informasi dalam genggamanmu. Cek ketersediaan kamar, tagihan, dan notifikasi tanpa repot."
            )
        )

        adapter = WelcomeAdapter(this, items)
        viewPager.adapter = adapter

        // Dot indicator setup
        setupDots(items.size)
        setCurrentDot(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentDot(position)
            }
        })

        // Tombol bawah
        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnRegister: Button = findViewById(R.id.btn_register)
        val tvExplore: TextView = findViewById(R.id.tvExplore)

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvExplore.setOnClickListener {
            // Buka MainActivity dan arahkan ke HomeFragment
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("open_fragment", "home")
            startActivity(intent)
            finish() // Tutup Welcome agar tidak bisa kembali
        }

        // === Tambahan opsional: beri jarak aman otomatis pada logo kalau ketimpa status bar ===
        val headerLayout: LinearLayout = findViewById(R.id.headerLayout)
        headerLayout.setPadding(
            headerLayout.paddingLeft,
            headerLayout.paddingTop + getStatusBarHeight(),
            headerLayout.paddingRight,
            headerLayout.paddingBottom
        )
    }

    // Fungsi untuk ambil tinggi status bar
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun setupDots(count: Int) {
        dots = arrayOfNulls(count)
        dotLayout.removeAllViews()

        for (i in 0 until count) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.dot_inactive)
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            dotLayout.addView(dots[i], params)
        }
    }

    private fun setCurrentDot(index: Int) {
        for (i in dots.indices) {
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == index) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
        }
    }
}


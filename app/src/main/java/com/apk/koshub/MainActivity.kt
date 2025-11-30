package com.apk.koshub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.fragments.FavoriteFragment
import com.apk.koshub.fragments.ExploreFragment
import com.apk.koshub.fragments.HomeFragment
import com.apk.koshub.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.apk.koshub.R
import android.content.Context
import android.content.res.Configuration
import com.apk.koshub.utils.SharedPrefHelper
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.core.view.WindowInsetsCompat
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val pref = SharedPrefHelper(newBase)
        val lang = pref.getLanguage() ?: "id"

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.mainRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, sysBars.top, 0, sysBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Default fragment: Home
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Listener untuk bottom nav
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_explore -> {
                    loadFragment(ExploreFragment())
                    true
                }
                R.id.nav_favorite -> {
                    loadFragment(FavoriteFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
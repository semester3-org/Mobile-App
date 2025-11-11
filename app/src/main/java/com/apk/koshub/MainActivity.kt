package com.apk.koshub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.fragments.FavoriteFragment
import com.apk.koshub.fragments.ExploreFragment
import com.apk.koshub.fragments.HomeFragment
import com.apk.koshub.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // ✅ hanya set fragment awal kalau activity baru pertama kali dibuat
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Listener bottom nav (tetap sama)
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

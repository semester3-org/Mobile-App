package com.apk.koshub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.fragments.FavoriteFragment
import com.apk.koshub.fragments.ExploreFragment
import com.apk.koshub.fragments.HomeFragment
import com.apk.koshub.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.apk.koshub.R

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Default fragment: Home
        loadFragment(HomeFragment())

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
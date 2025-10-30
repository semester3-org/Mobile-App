package com.apk.koshub.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import android.widget.RadioButton
import com.apk.koshub.R
import androidx.activity.addCallback
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class GantiBahasaFragment : Fragment(R.layout.fragment_ganti_bahasa) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back callback: show bottom nav dan pop
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.VISIBLE
            requireActivity().supportFragmentManager.popBackStack()
        }

        val pref = SharedPrefHelper(requireContext())
        val cardID = view.findViewById<CardView>(R.id.card_bahasa_id)
        val cardEN = view.findViewById<CardView>(R.id.card_bahasa_en)
        val radioID = view.findViewById<RadioButton>(R.id.radio_id)
        val radioEN = view.findViewById<RadioButton>(R.id.radio_en)

        // helper kecil buat update UI + simpan
        fun setLanguageAndUpdateUI(lang: String) {
            
            if (lang == "id") {
                radioID.isChecked = true
                radioEN.isChecked = false
            } else {
                radioEN.isChecked = true
                radioID.isChecked = false
            }
            pref.setLanguage(lang)
        }

        // set awal sesuai pref
        setLanguageAndUpdateUI(pref.getLanguage())

        // klik card -> update + simpan
        cardID.setOnClickListener { setLanguageAndUpdateUI("id") }
        cardEN.setOnClickListener { setLanguageAndUpdateUI("en") }

        // klik radio langsung -> update + simpan juga
        radioID.setOnClickListener { setLanguageAndUpdateUI("id") }
        radioEN.setOnClickListener { setLanguageAndUpdateUI("en") }
    }
}

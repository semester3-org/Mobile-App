package com.apk.koshub.fragments

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.apk.koshub.R
import com.apk.koshub.utils.SharedPrefHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class GantiBahasaFragment : Fragment(R.layout.fragment_ganti_bahasa) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                .visibility = View.VISIBLE
            requireActivity().supportFragmentManager.popBackStack()
        }

        val pref = SharedPrefHelper(requireContext())

        val cardID = view.findViewById<CardView>(R.id.card_bahasa_id)
        val cardEN = view.findViewById<CardView>(R.id.card_bahasa_en)
        val radioID = view.findViewById<RadioButton>(R.id.radio_id)
        val radioEN = view.findViewById<RadioButton>(R.id.radio_en)

        val tvTitle = view.findViewById<TextView>(R.id.tvLangTitle)
        val tvIdTitle = view.findViewById<TextView>(R.id.tv_id_title)
        val tvIdSub = view.findViewById<TextView>(R.id.tv_id_sub)
        val tvEnTitle = view.findViewById<TextView>(R.id.tv_en_title)
        val tvEnSub = view.findViewById<TextView>(R.id.tv_en_sub)

        radioID.isSaveEnabled = false
        radioEN.isSaveEnabled = false

        fun applyLanguageUI(lang: String) {
            val isId = lang == "id"
            radioID.isChecked = isId
            radioEN.isChecked = !isId
            radioID.jumpDrawablesToCurrentState()
            radioEN.jumpDrawablesToCurrentState()
        }

        fun applyTexts() {
            tvTitle.text = getString(R.string.lang_title)
            tvIdTitle.text = getString(R.string.lang_id_title)
            tvIdSub.text = getString(R.string.lang_id_sub)
            tvEnTitle.text = getString(R.string.lang_en_title)
            tvEnSub.text = getString(R.string.lang_en_sub)
        }

        val current = pref.getLanguage()
        applyLanguageUI(current)
        applyTexts()

        fun changeLanguage(lang: String) {
            val old = pref.getLanguage()
            if (lang == old) {
                applyLanguageUI(lang)
                return
            }

            pref.setLanguage(lang)

            // recreate activity supaya BaseActivity apply locale baru
            requireActivity().recreate()
            // nggak pakai AppCompatDelegate.setApplicationLocales
            // nggak ada navigate ke home (karena MainActivity pakai savedInstanceState guard)
        }

        cardID.setOnClickListener { changeLanguage("id") }
        cardEN.setOnClickListener { changeLanguage("en") }
        radioID.setOnClickListener { changeLanguage("id") }
        radioEN.setOnClickListener { changeLanguage("en") }
    }
}

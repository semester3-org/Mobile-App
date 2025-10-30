package com.apk.koshub.utils

import android.content.Context
import kotlin.apply

class SharedPrefHelper(context: Context) {

    private val pref = context.getSharedPreferences("koshub_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIF = "notif_enabled"

        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun setNotifEnabled(enabled: Boolean) {
        pref.edit().putBoolean(KEY_NOTIF, enabled).apply()
    }

    fun isNotifEnabled(): Boolean {
        return pref.getBoolean(KEY_NOTIF, false)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        pref.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    fun clearSession() {
        pref.edit().remove(KEY_IS_LOGGED_IN).apply()
    }
    fun setLanguage(lang: String) {
        pref.edit().putString("APP_LANG", lang).apply()
    }

    fun getLanguage(): String {
        return pref.getString("APP_LANG", "id") ?: "id"  // default indo
    }

}



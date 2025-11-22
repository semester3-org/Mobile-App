package com.apk.koshub.utils

import android.content.Context

class SharedPrefHelper(context: Context) {

    private val pref = context.getSharedPreferences("koshub_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIF = "notif_enabled"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        // ✅ key tambahan untuk data user
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
    }

    // 🔔 Notifikasi
    fun setNotifEnabled(enabled: Boolean) {
        pref.edit().putBoolean(KEY_NOTIF, enabled).apply()
    }

    fun isNotifEnabled(): Boolean {
        return pref.getBoolean(KEY_NOTIF, false)
    }

    // 🔐 Status login
    fun setLoggedIn(loggedIn: Boolean) {
        pref.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        pref.edit().clear().apply()
    }

    // 🌐 Bahasa
    fun setLanguage(lang: String) {
        pref.edit().putString("APP_LANG", lang).apply()
    }

    fun getLanguage(): String {
        return pref.getString("APP_LANG", "id") ?: "id"
    }

    // 🧠 Generic string setter/getter
    fun setString(key: String, value: String) {
        pref.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return pref.getString(key, "")
    }

    // 🧍‍♂️ Simpan data user setelah login
    fun saveUserData(id: Int, name: String?, email: String?, phone: String?) {
        pref.edit()
            .putInt(KEY_USER_ID, id)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_PHONE, phone)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserId(): Int = pref.getInt(KEY_USER_ID, -1)
    fun getUserName(): String? = pref.getString(KEY_USER_NAME, "")
    fun getUserEmail(): String? = pref.getString(KEY_USER_EMAIL, "")
    fun getUserPhone(): String? = pref.getString(KEY_USER_PHONE, "")
}


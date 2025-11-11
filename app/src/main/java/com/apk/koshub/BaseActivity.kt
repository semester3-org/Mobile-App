package com.apk.koshub

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.apk.koshub.utils.LocaleHelper
import com.apk.koshub.utils.SharedPrefHelper

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val pref = SharedPrefHelper(newBase)
        val lang = pref.getLanguage()
        val localizedContext = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(localizedContext)
    }

    override fun onPostResume() {
        super.onPostResume()
        // cut anim transisi setelah recreate biar nggak kerasa "kedip"
        overridePendingTransition(0, 0)
    }
}

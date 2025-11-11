package com.apk.koshub.utils

import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun applyLocale(base: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = base.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            base.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            base.resources.updateConfiguration(config, base.resources.displayMetrics)
            base
        }
    }
}

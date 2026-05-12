package com.example.memorandum.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleManager {

    fun setLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "Romanian" -> Locale("ro", "RO")
            "Ukrainian" -> Locale("uk", "UA")
            "Spanish" -> Locale("es", "ES")
            "Portuguese" -> Locale("pt", "PT")
            else -> Locale("en", "US")
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
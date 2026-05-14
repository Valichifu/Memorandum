package com.example.memorandum.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "Romanian" -> Locale.forLanguageTag("ro-RO")
            "Ukrainian" -> Locale.forLanguageTag("uk-UA")
            "Spanish" -> Locale.forLanguageTag("es-ES")
            "Portuguese" -> Locale.forLanguageTag("pt-PT")
            else -> Locale.forLanguageTag("en-US")
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
package com.example.memorandum.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    fun setLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "Romanian", "Română" -> Locale.forLanguageTag("ro-RO")
            "Ukrainian", "Українська" -> Locale.forLanguageTag("uk-UA")
            "Spanish", "Español" -> Locale.forLanguageTag("es-ES")
            "Portuguese", "Português" -> Locale.forLanguageTag("pt-PT")
            "Russian", "Русский" -> Locale.forLanguageTag("ru-RU")
            else -> Locale.forLanguageTag("en-US")
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
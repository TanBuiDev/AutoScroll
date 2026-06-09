package com.personal.autoscroll.core.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.personal.autoscroll.domain.model.LanguageMode
import java.util.Locale

object AppLocale {
    fun localizedContext(context: Context, languageMode: LanguageMode): Context {
        val locale = languageMode.localeOrNull() ?: run {
            Locale.setDefault(context.resources.configuration.locales.get(0))
            return context
        }
        val configuration = localizedConfiguration(context.resources.configuration, languageMode)
        Locale.setDefault(locale)
        return context.createConfigurationContext(configuration)
    }

    fun localizedConfiguration(configuration: Configuration, languageMode: LanguageMode): Configuration {
        val locale = languageMode.localeOrNull() ?: return Configuration(configuration)
        return Configuration(configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
        }
    }

    private fun LanguageMode.localeOrNull(): Locale? =
        languageTag?.let(Locale::forLanguageTag)
}

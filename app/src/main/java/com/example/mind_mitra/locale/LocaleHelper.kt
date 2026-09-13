package com.example.mind_mitra.locale

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val PREFS = "mind_mitra_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_LANGUAGE_SELECTED = "language_selected"

    val supportedLanguages = listOf(
        LanguageOption("en", "English"),
        LanguageOption("as", "অসমীয়া"),
        LanguageOption("bn", "বাংলা"),
        LanguageOption("mni", "মৈতৈলোন্")
    )

    data class LanguageOption(val code: String, val nativeLabel: String)

    fun getSavedLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun hasSelectedLanguage(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LANGUAGE_SELECTED, false)
    }

    fun saveLanguage(context: Context, code: String, recreate: Boolean = true) {
        prefs(context).edit()
            .putString(KEY_LANGUAGE, code)
            .putBoolean(KEY_LANGUAGE_SELECTED, true)
            .commit()
        applyAppLocale(code)
        if (recreate && context is Activity) {
            context.recreate()
        }
    }

    fun applySavedLocale(context: Context) {
        applyAppLocale(getSavedLanguage(context))
    }

    fun applyAppLocale(code: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
    }

    fun wrapContext(base: Context): Context {
        val code = getSavedLanguage(base)
        val locale = Locale.forLanguageTag(code)
        val config = base.resources.configuration
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}

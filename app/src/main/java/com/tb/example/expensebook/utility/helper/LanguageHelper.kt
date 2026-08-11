package com.tb.example.expensebook.utility.helper

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.LanguageModel
import java.util.Locale

object LanguageHelper {

    fun getLanguageList(selectedCode: String = ""): List<LanguageModel> {
        val rawList = mutableListOf(
            LanguageModel(R.drawable.ic_flag_english, "English (English)", "en"),
            LanguageModel(R.drawable.ic_flag_hindi, "Hindi (हिंदी)", "hi"),
            LanguageModel(R.drawable.ic_flag_spanish, "Spanish (Española)", "es"),
            LanguageModel(R.drawable.ic_flag_french, "French (Français)", "fr"),
            LanguageModel(R.drawable.ic_flag_french_canada, "French (Français canadien)", "fr-rCA", "Canada"),
            LanguageModel(R.drawable.ic_flag_portuguese, "Portuguese (Português)", "pt"),
            LanguageModel(R.drawable.ic_flag_portuguese_brazil, "Portuguese (Português)", "pt-rBR", "Brasil"),
            LanguageModel(R.drawable.ic_flag_arabic, "Arabic (عربي)", "ar"),
            LanguageModel(R.drawable.ic_flag_russian, "Russian (Русский)", "ru"),
            LanguageModel(R.drawable.ic_flag_korean, "Korean (한국인)", "ko"),
            LanguageModel(R.drawable.ic_flag_german, "German (Deutsch)", "de"),
            LanguageModel(R.drawable.ic_flag_turkish, "Turkish (Türkçe)", "tr"),
            LanguageModel(R.drawable.ic_flag_italian, "Italian (Italiana)", "it"),
            LanguageModel(R.drawable.ic_flag_vietnamese, "Vietnamese (Việt Nam)", "vi"),
            LanguageModel(R.drawable.ic_flag_japanese, "Japanese (日本語)", "ja"),
            LanguageModel(R.drawable.ic_flag_indonesian, "Indonesian (Indonesia)", "in"),
            LanguageModel(R.drawable.ic_flag_thai, "Thai (ไทย)", "th"),
            LanguageModel(R.drawable.ic_flag_polish, "Polish (Polski)", "pl"),
            LanguageModel(R.drawable.ic_flag_chiness, "Chinese (中国人)", "zh-rCN", "Traditional"),
            LanguageModel(R.drawable.ic_flag_chiness, "Chinese (中国人)", "zh", "Simplified"),
            LanguageModel(R.drawable.ic_flag_romanian, "Romanian (Română)", "ro"),
            LanguageModel(R.drawable.ic_flag_afrikaans, "Afrikaans (Afrikaans)", "af"),
            LanguageModel(R.drawable.ic_flag_hungarian, "Hungarian (Magyar)", "hu"),
            LanguageModel(R.drawable.ic_flag_ukrainian, "Ukrainian (українська)", "uk"),
            LanguageModel(R.drawable.ic_flag_filipino, "Filipino (Filipino)", "fil")
        )

        val deviceLangCode = Locale.getDefault().language
        val matchIndex = rawList.indexOfFirst {
            it.code.equals(deviceLangCode, ignoreCase = true) ||
            it.code.startsWith(deviceLangCode, ignoreCase = true) ||
            deviceLangCode.startsWith(it.code, ignoreCase = true)
        }

        if (matchIndex > 0) {
            val deviceItem = rawList.removeAt(matchIndex)
            rawList.add(0, deviceItem)
        }

        if (selectedCode.isNotEmpty()) {
            rawList.forEach { it.isSelected = (it.code.equals(selectedCode, ignoreCase = true)) }
        } else {
            rawList.forEach { it.isSelected = false }
        }

        return rawList
    }

    fun applyAppLanguage(context: Context, languageCode: String) {
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}

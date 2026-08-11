package com.tb.example.expensebook.data.model

import androidx.annotation.DrawableRes

data class LanguageModel(
    @DrawableRes val flagRes: Int,
    val name: String,
    val code: String,
    val subTitle: String = "",
    var isSelected: Boolean = false
) {
    val englishName: String
        get() = name.substringBefore(" (").trim()
}

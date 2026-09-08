package cashvexa.expensetracker.budget.money.data.model

import androidx.annotation.DrawableRes

data class LanguageModel(
    @DrawableRes val flagRes: Int,
    val name: String,
    val code: String,
    val subTitle: String = "",
    var isSelected: Boolean = false
) {

    val nativeName: String
        get() = if (name.contains(" (")) name.substringAfter(" (").substringBefore(")").trim() else name
}

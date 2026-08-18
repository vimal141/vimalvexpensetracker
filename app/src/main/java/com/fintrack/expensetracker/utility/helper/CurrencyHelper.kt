package com.fintrack.expensetracker.utility.helper

import java.text.NumberFormat
import java.util.Locale

object CurrencyHelper {

    fun formatAmount(amount: Double, currencyCode: String = PrefManager.currency): String {
        val locale = getLocaleForCurrency(currencyCode)
        val format = NumberFormat.getNumberInstance(locale).apply {
            val hasDecimals = !isZeroDecimalCurrency(currencyCode)
            minimumFractionDigits = if (hasDecimals) 2 else 0
            maximumFractionDigits = if (hasDecimals) 2 else 0
        }
        return format.format(amount)
    }

    private fun getLocaleForCurrency(currencyCode: String): Locale {
        return when (currencyCode.uppercase()) {
            "INR" -> Locale("en", "IN")
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "USD" -> Locale.US
            "JPY" -> Locale.JAPAN
            "KRW" -> Locale.KOREA
            "AUD" -> Locale("en", "AU")
            "CAD" -> Locale.CANADA
            "CHF" -> Locale("fr", "CH")
            else -> Locale.US
        }
    }

    private fun isZeroDecimalCurrency(currencyCode: String): Boolean {
        return currencyCode.uppercase() in listOf("JPY", "KRW")
    }
}

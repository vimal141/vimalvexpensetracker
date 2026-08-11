package com.tb.example.expensebook.utility.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object PrefManager {
    private const val PREF_NAME = "expense_tracker_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val KEY_CURRENT_ACCOUNT_ID = "current_account_id"
    private const val KEY_PRIVACY_ACCEPT = "PRIVACY_ACCEPT"
    private const val KEY_LANGUAGE = "language_code"
    private const val KEY_LANGUAGE_SET = "is_language_set"
    private const val KEY_CURRENCY_SET = "is_currency_set"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var themeMode: Int
        get() = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = prefs.edit { putInt(KEY_THEME_MODE, value) }

    var isOnboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_DONE, value) }

    var isLanguageSet: Boolean
        get() = prefs.getBoolean(KEY_LANGUAGE_SET, false)
        set(value) = prefs.edit { putBoolean(KEY_LANGUAGE_SET, value) }

    var isCurrencySet: Boolean
        get() = prefs.getBoolean(KEY_CURRENCY_SET, false)
        set(value) = prefs.edit { putBoolean(KEY_CURRENCY_SET, value) }

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, "INR") ?: "INR"
        set(value) = prefs.edit { putString(KEY_CURRENCY, value) }

    var currencySymbol: String
        get() = prefs.getString(KEY_CURRENCY_SYMBOL, "₹") ?: "₹"
        set(value) = prefs.edit { putString(KEY_CURRENCY_SYMBOL, value) }

    var languageCode: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    var currentAccountId: Int
        get() = prefs.getInt(KEY_CURRENT_ACCOUNT_ID, -1)
        set(value) = prefs.edit { putInt(KEY_CURRENT_ACCOUNT_ID, value) }

    var isPrivacyAccepted: Boolean
        get() = prefs.getBoolean(KEY_PRIVACY_ACCEPT, false)
        set(value) = prefs.edit { putBoolean(KEY_PRIVACY_ACCEPT, value) }

    fun currentAccountIdFlow(): Flow<Int> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_CURRENT_ACCOUNT_ID) {
                trySend(sharedPreferences.getInt(KEY_CURRENT_ACCOUNT_ID, -1))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getInt(KEY_CURRENT_ACCOUNT_ID, -1))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}
package com.tb.example.expensebook.utility.ads

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tb.example.expensebook.R

object RemoteConfigHelper {

    private const val TAG = "RemoteConfigHelper"

    // Master switch
    const val KEY_IS_ADS_ENABLED = "is_ads_enabled"

    // Screen/Placement Switches
    const val KEY_SHOW_SPLASH_APP_OPEN_AD = "show_splash_app_open_ad"
    const val KEY_SHOW_DASHBOARD_NATIVE_AD = "show_dashboard_native_ad"
    const val KEY_SHOW_HISTORY_NATIVE_AD = "show_history_native_ad"
    const val KEY_SHOW_REPORTS_NATIVE_AD = "show_reports_native_ad"
    const val KEY_SHOW_ADD_TRANSACTION_BANNER_AD = "show_add_transaction_banner_ad"
    const val KEY_SHOW_SAVE_TRANSACTION_INTERSTITIAL = "show_save_transaction_interstitial"
    const val KEY_SHOW_EXPORT_REPORT_INTERSTITIAL = "show_export_report_interstitial"
    const val KEY_SHOW_PDF_PREVIEW_BANNER_AD = "show_pdf_preview_banner_ad"
    const val KEY_SHOW_ACCOUNTS_NATIVE_AD = "show_accounts_native_ad"
    const val KEY_SHOW_SETTINGS_BANNER_AD = "show_settings_banner_ad"
    const val KEY_SHOW_LANGUAGE_SCREEN_NATIVE_AD = "show_language_screen_native_ad"
    const val KEY_SHOW_CURRENCY_SCREEN_NATIVE_AD = "show_currency_screen_native_ad"

    // Counter & Interval
    const val KEY_INTERSTITIAL_CLICK_INTERVAL = "interstitial_click_interval"
    const val KEY_INTERSTITIAL_TIME_INTERVAL_SECONDS = "interstitial_time_interval_seconds"

    // Ad Unit IDs
    const val KEY_ADMOB_APP_OPEN_ID = "admob_app_open_id"
    const val KEY_ADMOB_INTERSTITIAL_ID = "admob_interstitial_id"
    const val KEY_ADMOB_NATIVE_ID = "admob_native_id"
    const val KEY_ADMOB_BANNER_ID = "admob_banner_id"
    const val KEY_ADMOB_REWARDED_ID = "admob_rewarded_id"

    private var remoteConfig: FirebaseRemoteConfig? = null

    fun init(context: Context, onFetchComplete: (() -> Unit)? = null) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            val config = FirebaseRemoteConfig.getInstance()
            remoteConfig = config

            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour caching in production
                .build()

            config.setConfigSettingsAsync(configSettings)
            config.setDefaultsAsync(R.xml.remote_config_defaults)

            config.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Remote config fetched & activated successfully")
                    } else {
                        Log.w(TAG, "Remote config fetch failed: ${task.exception?.message}")
                    }
                    onFetchComplete?.invoke()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing RemoteConfig: ${e.message}", e)
            onFetchComplete?.invoke()
        }
    }

    fun isAdsEnabled(): Boolean {
        return try {
            remoteConfig?.getBoolean(KEY_IS_ADS_ENABLED) ?: true
        } catch (e: Exception) {
            true
        }
    }

    fun isScreenAdEnabled(screenKey: String): Boolean {
        if (!isAdsEnabled()) return false
        return try {
            remoteConfig?.getBoolean(screenKey) ?: true
        } catch (e: Exception) {
            true
        }
    }

    fun getInterstitialClickInterval(): Int {
        return try {
            val interval = remoteConfig?.getLong(KEY_INTERSTITIAL_CLICK_INTERVAL)?.toInt() ?: 3
            if (interval <= 0) 3 else interval
        } catch (e: Exception) {
            3
        }
    }

    fun getInterstitialTimeIntervalSeconds(): Long {
        return try {
            val seconds = remoteConfig?.getLong(KEY_INTERSTITIAL_TIME_INTERVAL_SECONDS) ?: 30L
            if (seconds < 5L) 5L else seconds
        } catch (e: Exception) {
            30L
        }
    }

    fun getAppOpenAdId(context: Context): String {
        return getAdUnitId(context, KEY_ADMOB_APP_OPEN_ID, R.string.admob_app_open_ad_unit_id)
    }

    fun getInterstitialAdId(context: Context): String {
        return getAdUnitId(context, KEY_ADMOB_INTERSTITIAL_ID, R.string.admob_interstitial_ad_unit_id)
    }

    fun getNativeAdId(context: Context): String {
        return getAdUnitId(context, KEY_ADMOB_NATIVE_ID, R.string.admob_native_ad_unit_id)
    }

    fun getBannerAdId(context: Context): String {
        return getAdUnitId(context, KEY_ADMOB_BANNER_ID, R.string.admob_banner_ad_unit_id)
    }

    fun getRewardedAdId(context: Context): String {
        return getAdUnitId(context, KEY_ADMOB_REWARDED_ID, R.string.admob_rewarded_ad_unit_id)
    }

    private fun getAdUnitId(context: Context, key: String, defaultResId: Int): String {
        return try {
            val remoteValue = remoteConfig?.getString(key)
            if (!remoteValue.isNullOrBlank() && remoteValue.startsWith("ca-app-pub-")) {
                remoteValue
            } else {
                context.getString(defaultResId)
            }
        } catch (e: Exception) {
            context.getString(defaultResId)
        }
    }
}

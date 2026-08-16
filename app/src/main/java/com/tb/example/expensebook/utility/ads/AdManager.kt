package com.tb.example.expensebook.utility.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.facebook.shimmer.ShimmerFrameLayout
import com.tb.example.expensebook.R
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {

    private const val TAG = "AdManager"

    private val isInitialized = AtomicBoolean(false)
    var isShowingFullScreenAd = false

    // App Open Ad
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenLoadTime: Long = 0L

    // Interstitial Ad
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var actionCounter = 0
    private var lastInterstitialShownTime = 0L

    fun init(application: Application) {
        if (isInitialized.getAndSet(true)) return

        RemoteConfigHelper.init(application)

        // Initialize Google Mobile Ads SDK on background thread
        Thread {
            try {
                // Configure test devices if needed
                val requestConfiguration = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)

                MobileAds.initialize(application) { status ->
                    Log.d(TAG, "MobileAds initialized: $status")
                    // Preload first interstitial ad once SDK is initialized
                    preloadInterstitial(application)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MobileAds: ${e.message}", e)
            }
        }.start()
    }

    // ==========================================
    // 1. APP OPEN AD (SPLASH / RESUME)
    // ==========================================

    fun loadAppOpenAd(context: Context, onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        if (!RemoteConfigHelper.isScreenAdEnabled(RemoteConfigHelper.KEY_SHOW_SPLASH_APP_OPEN_AD)) {
            onFailed?.invoke()
            return
        }

        if (isAppOpenLoading || isAppOpenAdAvailable()) {
            if (isAppOpenAdAvailable()) onLoaded?.invoke()
            return
        }

        isAppOpenLoading = true
        val adUnitId = RemoteConfigHelper.getAppOpenAdId(context)
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "AppOpenAd loaded successfully")
                    appOpenAd = ad
                    isAppOpenLoading = false
                    appOpenLoadTime = System.currentTimeMillis()
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "AppOpenAd failed to load: ${error.message}")
                    appOpenAd = null
                    isAppOpenLoading = false
                    onFailed?.invoke()
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        // App open ads expire after 4 hours
        val isNotExpired = (System.currentTimeMillis() - appOpenLoadTime) < (4 * 3600 * 1000L)
        return appOpenAd != null && isNotExpired
    }

    fun showSplashAppOpenAd(activity: Activity, onComplete: () -> Unit) {
        if (!RemoteConfigHelper.isScreenAdEnabled(RemoteConfigHelper.KEY_SHOW_SPLASH_APP_OPEN_AD)) {
            onComplete()
            return
        }

        if (!isAppOpenAdAvailable() || isShowingFullScreenAd) {
            onComplete()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowingFullScreenAd = true
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "AppOpenAd dismissed")
                appOpenAd = null
                isShowingFullScreenAd = false
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "AppOpenAd failed to show: ${error.message}")
                appOpenAd = null
                isShowingFullScreenAd = false
                onComplete()
            }
        }

        appOpenAd?.show(activity)
    }

    // =========================================================
    // 2. INTERSTITIAL ADS (SMART CAPPING & ANTI-LIMIT STRATEGY)
    // =========================================================

    fun preloadInterstitial(context: Context) {
        if (!RemoteConfigHelper.isAdsEnabled()) return
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val adUnitId = RemoteConfigHelper.getInterstitialAdId(context)
        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "InterstitialAd preloaded successfully")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "InterstitialAd failed to load: ${error.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    /**
     * Shows Interstitial Ad based on Action/Click Counter & Minimum Time Interval.
     * Prevents user annoyance and guarantees 100% AdMob Ad Limit protection!
     */
    fun showInterstitialWithCounter(
        activity: Activity,
        screenFlagKey: String = RemoteConfigHelper.KEY_SHOW_SAVE_TRANSACTION_INTERSTITIAL,
        onDismiss: () -> Unit
    ) {
        // 1. Check screen/global switch
        if (!RemoteConfigHelper.isScreenAdEnabled(screenFlagKey)) {
            onDismiss()
            return
        }

        // 2. Check Action Counter
        actionCounter++
        val clickInterval = RemoteConfigHelper.getInterstitialClickInterval()
        val isCounterReached = (actionCounter % clickInterval == 0)

        // 3. Check Minimum Time Interval
        val minTimeIntervalMs = RemoteConfigHelper.getInterstitialTimeIntervalSeconds() * 1000L
        val isTimeElapsed = (System.currentTimeMillis() - lastInterstitialShownTime) >= minTimeIntervalMs

        if (isCounterReached && isTimeElapsed && interstitialAd != null && !isShowingFullScreenAd) {
            val ad = interstitialAd!!
            interstitialAd = null // Consume preloaded ad

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                    lastInterstitialShownTime = System.currentTimeMillis()
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    isShowingFullScreenAd = false
                    preloadInterstitial(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${error.message}")
                    isShowingFullScreenAd = false
                    preloadInterstitial(activity.applicationContext)
                    onDismiss()
                }
            }

            ad.show(activity)
        } else {
            // If ad was null, try preloading for subsequent clicks
            if (interstitialAd == null) {
                preloadInterstitial(activity.applicationContext)
            }
            onDismiss()
        }
    }

    /**
     * Direct Interstitial Ad display for natural milestones (e.g. Exporting PDF).
     */
    fun showInterstitialDirect(
        activity: Activity,
        screenFlagKey: String = RemoteConfigHelper.KEY_SHOW_EXPORT_REPORT_INTERSTITIAL,
        onDismiss: () -> Unit
    ) {
        if (!RemoteConfigHelper.isScreenAdEnabled(screenFlagKey)) {
            onDismiss()
            return
        }

        val minTimeIntervalMs = 15_000L // Min 15s delay
        val isTimeElapsed = (System.currentTimeMillis() - lastInterstitialShownTime) >= minTimeIntervalMs

        if (isTimeElapsed && interstitialAd != null && !isShowingFullScreenAd) {
            val ad = interstitialAd!!
            interstitialAd = null

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                    lastInterstitialShownTime = System.currentTimeMillis()
                }

                override fun onAdDismissedFullScreenContent() {
                    isShowingFullScreenAd = false
                    preloadInterstitial(activity.applicationContext)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    isShowingFullScreenAd = false
                    preloadInterstitial(activity.applicationContext)
                    onDismiss()
                }
            }

            ad.show(activity)
        } else {
            if (interstitialAd == null) {
                preloadInterstitial(activity.applicationContext)
            }
            onDismiss()
        }
    }

    // ==========================================
    // ==========================================
    // NETWORK CONNECTIVITY CHECK
    // ==========================================

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    // ==========================================
    // 3. ADAPTIVE BANNER ADS (WITH SHIMMER)
    // ==========================================

    fun loadAdaptiveBanner(
        activity: Activity,
        container: ViewGroup,
        screenFlagKey: String = RemoteConfigHelper.KEY_SHOW_SETTINGS_BANNER_AD
    ) {
        // 1. If ads disabled globally/screen-wise OR no internet connection, hide immediately
        if (!isNetworkAvailable(activity) || !RemoteConfigHelper.isScreenAdEnabled(screenFlagKey)) {
            container.visibility = View.GONE
            container.removeAllViews()
            return
        }

        try {
            // 2. Show shimmer loading placeholder while ad loads
            val inflater = LayoutInflater.from(activity)
            val shimmerView = inflater.inflate(R.layout.layout_shimmer_banner, container, false) as? ShimmerFrameLayout
            container.removeAllViews()
            if (shimmerView != null) {
                container.addView(shimmerView)
                shimmerView.startShimmer()
            }
            container.visibility = View.VISIBLE

            val adView = AdView(activity)
            val adUnitId = RemoteConfigHelper.getBannerAdId(activity)
            adView.adUnitId = adUnitId

            val adSize = getAdSize(activity, container)
            adView.setAdSize(adSize)

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "BannerAd loaded successfully for $screenFlagKey")
                    shimmerView?.stopShimmer()
                    container.removeAllViews()
                    container.addView(adView)
                    container.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "BannerAd failed to load for $screenFlagKey: ${error.message}")
                    shimmerView?.stopShimmer()
                    container.removeAllViews()
                    container.visibility = View.GONE
                }
            }

            val request = AdRequest.Builder().build()
            adView.loadAd(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating adaptive banner: ${e.message}", e)
            container.visibility = View.GONE
            container.removeAllViews()
        }
    }

    private fun getAdSize(activity: Activity, container: ViewGroup): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        var adWidthPixels = container.width.toFloat()

        if (adWidthPixels == 0f) {
            adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
                windowMetrics.bounds.width().toFloat()
            } else {
                displayMetrics.widthPixels.toFloat()
            }
        }

        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    // ==========================================
    // 4. CUSTOM NATIVE ADVANCED ADS (WITH SHIMMER)
    // ==========================================

    private fun getShimmerLayoutForNative(layoutResId: Int): Int {
        return when (layoutResId) {
            R.layout.layout_native_ad_small -> R.layout.layout_shimmer_native_small
            R.layout.layout_native_ad_medium -> R.layout.layout_shimmer_native_medium
            else -> R.layout.layout_shimmer_native_card
        }
    }

    fun loadNativeAd(
        activity: Activity,
        container: ViewGroup,
        layoutResId: Int = R.layout.layout_native_ad_card,
        screenFlagKey: String = RemoteConfigHelper.KEY_SHOW_DASHBOARD_NATIVE_AD,
        onLoaded: (() -> Unit)? = null
    ) {
        // 1. If ads disabled globally/screen-wise OR no internet connection, hide immediately
        if (!isNetworkAvailable(activity) || !RemoteConfigHelper.isScreenAdEnabled(screenFlagKey)) {
            container.visibility = View.GONE
            container.removeAllViews()
            return
        }

        try {
            // 2. Show matching shimmer layout while ad loads
            val inflater = LayoutInflater.from(activity)
            val shimmerRes = getShimmerLayoutForNative(layoutResId)
            val shimmerView = inflater.inflate(shimmerRes, container, false) as? ShimmerFrameLayout
            container.removeAllViews()
            if (shimmerView != null) {
                container.addView(shimmerView)
                shimmerView.startShimmer()
            }
            container.visibility = View.VISIBLE

            val adUnitId = RemoteConfigHelper.getNativeAdId(activity)

            val adLoader = AdLoader.Builder(activity, adUnitId)
                .forNativeAd { nativeAd ->
                    if (activity.isFinishing || activity.isDestroyed) {
                        nativeAd.destroy()
                        return@forNativeAd
                    }

                    shimmerView?.stopShimmer()
                    val adView = inflater.inflate(layoutResId, container, false) as NativeAdView
                    populateNativeAdView(nativeAd, adView)

                    container.removeAllViews()
                    container.addView(adView)
                    container.visibility = View.VISIBLE
                    onLoaded?.invoke()
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "NativeAd failed to load for $screenFlagKey: ${error.message}")
                        shimmerView?.stopShimmer()
                        container.removeAllViews()
                        container.visibility = View.GONE
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setRequestMultipleImages(false)
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading native ad: ${e.message}", e)
            container.visibility = View.GONE
            container.removeAllViews()
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // App Icon
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        if (iconView != null) {
            if (nativeAd.icon != null) {
                iconView.setImageDrawable(nativeAd.icon?.drawable)
                iconView.visibility = View.VISIBLE
                adView.iconView = iconView
            } else {
                iconView.visibility = View.GONE
            }
        }

        // Headline
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        if (headlineView != null) {
            headlineView.text = nativeAd.headline
            adView.headlineView = headlineView
        }

        // Body
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        if (bodyView != null) {
            if (nativeAd.body != null) {
                bodyView.text = nativeAd.body
                bodyView.visibility = View.VISIBLE
                adView.bodyView = bodyView
            } else {
                bodyView.visibility = View.GONE
            }
        }

        // Call To Action
        val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
        if (ctaView != null) {
            if (nativeAd.callToAction != null) {
                ctaView.text = nativeAd.callToAction
                ctaView.visibility = View.VISIBLE
                adView.callToActionView = ctaView
            } else {
                ctaView.visibility = View.GONE
            }
        }

        // Advertiser
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
        if (advertiserView != null) {
            if (nativeAd.advertiser != null) {
                advertiserView.text = nativeAd.advertiser
                advertiserView.visibility = View.VISIBLE
                adView.advertiserView = advertiserView
            } else if (nativeAd.store != null) {
                advertiserView.text = nativeAd.store
                advertiserView.visibility = View.VISIBLE
                adView.advertiserView = advertiserView
            } else {
                advertiserView.visibility = View.GONE
            }
        }

        // Star Rating
        val starView = adView.findViewById<RatingBar>(R.id.ad_stars)
        if (starView != null) {
            if (nativeAd.starRating != null) {
                starView.rating = nativeAd.starRating!!.toFloat()
                starView.visibility = View.VISIBLE
                adView.starRatingView = starView
            } else {
                starView.visibility = View.GONE
            }
        }

        // Media View
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        if (mediaView != null) {
            adView.mediaView = mediaView
            if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()) {
                mediaView.visibility = View.VISIBLE
            } else if (nativeAd.images.isNotEmpty()) {
                mediaView.visibility = View.VISIBLE
            } else {
                mediaView.visibility = View.GONE
            }
        }

        adView.setNativeAd(nativeAd)
    }
}

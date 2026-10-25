package cashvexa.expensetracker.budget.money.utility.helper

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach

object EdgeToEdgeUtil {

    /**
     * Configures completely transparent edge-to-edge system bars (Status Bar & Navigation Bar)
     * across Android 11 (API 30) up to Android 15/16/17 (API 36+).
     *
     * Ensures:
     * 1. 100% transparent Status Bar & Navigation Bar (only screen background visible).
     * 2. No ugly scrim / contrast box on Android 10+ (API 29+).
     * 3. Seamless display cutout / camera notch handling.
     * 4. Correct light/dark icons depending on current theme.
     */
    fun setupEdgeToEdge(
        activity: ComponentActivity,
        customLightStatusBars: Boolean? = null,
        customLightNavBars: Boolean? = null
    ) {
        val window = activity.window
        val isDarkTheme = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val lightStatusBars = customLightStatusBars ?: !isDarkTheme
        val lightNavigationBars = customLightNavBars ?: !isDarkTheme

        // 1. AndroidX enableEdgeToEdge with transparent style
        val statusBarStyle = if (lightStatusBars) {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        } else {
            SystemBarStyle.dark(Color.TRANSPARENT)
        }

        val navigationBarStyle = if (lightNavigationBars) {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        } else {
            SystemBarStyle.dark(Color.TRANSPARENT)
        }

        activity.enableEdgeToEdge(
            statusBarStyle = statusBarStyle,
            navigationBarStyle = navigationBarStyle
        )

        // 2. Set decor fits system windows to false
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 3. Configure Display Cutout mode (short edges / always)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val params = window.attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = params
        }

        // 4. Disable translucent contrast enforcement on Android 10+ (API 29+)
        // This ensures the navigation bar and status bar are 100% transparent and only show the background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            window.isNavigationBarContrastEnforced = false
            @Suppress("DEPRECATION")
            window.isStatusBarContrastEnforced = false
        }

        // 5. Explicitly ensure transparent system bars on Android 11-14
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT

        // 6. Configure system bar icon contrast (dark icons on light bg, white icons on dark bg)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = lightStatusBars
        insetsController.isAppearanceLightNavigationBars = lightNavigationBars
    }

    /**
     * Applies safe insets padding to a view, preserving its initial XML padding.
     */
    fun applySystemBarInsets(
        view: View,
        applyTop: Boolean = true,
        applyBottom: Boolean = true,
        applyLeft: Boolean = true,
        applyRight: Boolean = true,
        includeIme: Boolean = false
    ) {
        val initialPaddingLeft = view.paddingLeft
        val initialPaddingTop = view.paddingTop
        val initialPaddingRight = view.paddingRight
        val initialPaddingBottom = view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val types = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            val barInsets = windowInsets.getInsets(types)
            val imeInsets = if (includeIme) windowInsets.getInsets(WindowInsetsCompat.Type.ime()) else Insets.NONE

            val top = if (applyTop) initialPaddingTop + barInsets.top else v.paddingTop
            val bottom = if (applyBottom) {
                if (includeIme) initialPaddingBottom + maxOf(barInsets.bottom, imeInsets.bottom)
                else initialPaddingBottom + barInsets.bottom
            } else v.paddingBottom
            val left = if (applyLeft) initialPaddingLeft + barInsets.left else v.paddingLeft
            val right = if (applyRight) initialPaddingRight + barInsets.right else v.paddingRight

            v.setPadding(left, top, right, bottom)
            windowInsets
        }

        if (view.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.doOnAttach { ViewCompat.requestApplyInsets(it) }
        }
    }

    /**
     * Applies safe insets margin to a view (e.g. FloatingActionButton).
     */
    fun applySystemBarMargin(
        view: View,
        applyTop: Boolean = false,
        applyBottom: Boolean = true,
        applyLeft: Boolean = false,
        applyRight: Boolean = false
    ) {
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val initialLeft = lp.leftMargin
        val initialTop = lp.topMargin
        val initialRight = lp.rightMargin
        val initialBottom = lp.bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val params = v.layoutParams as? ViewGroup.MarginLayoutParams ?: return@setOnApplyWindowInsetsListener windowInsets
            val types = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            val barInsets = windowInsets.getInsets(types)

            if (applyLeft) params.leftMargin = initialLeft + barInsets.left
            if (applyTop) params.topMargin = initialTop + barInsets.top
            if (applyRight) params.rightMargin = initialRight + barInsets.right
            if (applyBottom) params.bottomMargin = initialBottom + barInsets.bottom

            v.layoutParams = params
            windowInsets
        }

        if (view.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.doOnAttach { ViewCompat.requestApplyInsets(it) }
        }
    }

    /**
     * Applies safe bottom insets to bottom sheet dialogs / fragments so action buttons are never obscured.
     */
    fun applyBottomSheetInsets(view: View) {
        val initialBottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val navBars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                initialBottom + navBars.bottom
            )
            windowInsets
        }

        if (view.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.doOnAttach { ViewCompat.requestApplyInsets(it) }
        }
    }
}

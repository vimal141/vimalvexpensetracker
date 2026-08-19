package cashvexa.expensetracker.budget.money.utility.helper

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import java.util.Locale

@SuppressLint("ClickableViewAccessibility")
fun View.bounceOnClick(onClick: (() -> Unit)? = null) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(100)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(120)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
                if (event.action == MotionEvent.ACTION_UP) {
                    onClick?.invoke()
                }
            }
        }
        true
    }
}

fun View.slideUpFadeIn(duration: Long = 350L, delay: Long = 0L, startYOffset: Float = 50f) {
    this.translationY = startYOffset
    this.alpha = 0f
    this.animate()
        .translationY(0f)
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .start()
}

fun TextView.animateNumber(
    fromValue: Double,
    toValue: Double,
    prefix: String = "",
    suffix: String = "",
    duration: Long = 500L
) {
    val animator = ValueAnimator.ofFloat(fromValue.toFloat(), toValue.toFloat())
    animator.duration = duration
    animator.interpolator = DecelerateInterpolator()
    animator.addUpdateListener { animation ->
        val value = animation.animatedValue as Float
        this.text = String.format(Locale.US, "%s%.2f%s", prefix, value, suffix)
    }
    animator.start()
}

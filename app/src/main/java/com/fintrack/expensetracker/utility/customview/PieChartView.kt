package com.fintrack.expensetracker.utility.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.fintrack.expensetracker.utility.helper.CurrencyHelper
import com.fintrack.expensetracker.utility.helper.PrefManager
import com.fintrack.expensetracker.R

class PieChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    data class Slice(val value: Float, val color: Int)
    private var slices: List<Slice> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var animationProgress = 0f
    private var centerTitle = context.getString(R.string.text_total_expense_center)

    fun setData(newSlices: List<Slice>, newCenterTitle: String = "Total Expense") {
        slices = newSlices
        centerTitle = newCenterTitle
        startAnimation()
    }

    private fun startAnimation() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationProgress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) {
            val cx = width / 2f
            val cy = height / 2f

            paint.color = ContextCompat.getColor(context, R.color.divider_color)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 60f
            val radius = (minOf(width, height) / 2f) - (60f / 2f) - 16f
            rect.set(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawArc(rect, 0f, 360f, false, paint)

            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f
            paint.typeface = ResourcesCompat.getFont(context, R.font.medium)
            paint.textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._14ssp)
            paint.color = ContextCompat.getColor(context, R.color.subTextColor)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("No Data", cx, cy + context.resources.getDimension(com.intuit.sdp.R.dimen._5sdp), paint)
            return
        }

        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total == 0f) return

        val cx = width / 2f
        val cy = height / 2f
        val strokeWidth = 60f
        val radius = (minOf(width, height) / 2f) - (strokeWidth / 2f) - 16f

        rect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.BUTT

        var startAngle = -90f
        slices.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f * animationProgress
            paint.color = slice.color
            canvas.drawArc(rect, startAngle, sweepAngle - 1f, false, paint)
            startAngle += sweepAngle
        }

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.strokeWidth = 0f

        paint.typeface = ResourcesCompat.getFont(context, R.font.medium)
        paint.textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._12ssp)
        paint.color = ContextCompat.getColor(context, R.color.subTextColor)
        canvas.drawText(centerTitle, cx, cy - context.resources.getDimension(com.intuit.sdp.R.dimen._6sdp), paint)

        paint.typeface = ResourcesCompat.getFont(context, R.font.bold)
        paint.textSize = context.resources.getDimension(com.intuit.ssp.R.dimen._16ssp)
        paint.color = ContextCompat.getColor(context, R.color.TextColor)
        val symbol = PrefManager.currencySymbol
        val totalText = "$symbol ${CurrencyHelper.formatAmount(total.toDouble())}"
        canvas.drawText(totalText, cx, cy + context.resources.getDimension(com.intuit.sdp.R.dimen._18sdp), paint)
    }
}
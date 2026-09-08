package cashvexa.expensetracker.budget.money.utility.customview

import android.text.TextPaint
import android.text.TextUtils
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
import cashvexa.expensetracker.budget.money.utility.helper.CurrencyHelper
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import cashvexa.expensetracker.budget.money.R

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

            val noDataText = context.getString(R.string.no_data)
            val noDataYOffset = context.resources.getDimension(com.intuit.sdp.R.dimen._5sdp)
            val innerRadiusEmpty = (radius - (60f / 2f)).coerceAtLeast(0f)
            val horizontalMargin = context.resources.getDimension(com.intuit.sdp.R.dimen._12sdp)
            val noDataChordHalfWidth = if (innerRadiusEmpty > noDataYOffset) {
                kotlin.math.sqrt(innerRadiusEmpty * innerRadiusEmpty - noDataYOffset * noDataYOffset)
            } else {
                innerRadiusEmpty
            }
            val maxTextWidth = ((noDataChordHalfWidth - horizontalMargin) * 2f).coerceAtLeast(0f)
            val ellipsizedText = TextUtils.ellipsize(
                noDataText,
                TextPaint(paint),
                maxTextWidth,
                TextUtils.TruncateAt.END
            ).toString()

            canvas.drawText(ellipsizedText, cx, cy + noDataYOffset, paint)
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

        val innerRadius = (radius - (strokeWidth / 2f)).coerceAtLeast(0f)
        val horizontalMargin = context.resources.getDimension(com.intuit.sdp.R.dimen._12sdp)

        // Draw centerTitle
        val titleYOffset = context.resources.getDimension(com.intuit.sdp.R.dimen._6sdp)
        val titleChordHalfWidth = if (innerRadius > titleYOffset) {
            kotlin.math.sqrt(innerRadius * innerRadius - titleYOffset * titleYOffset)
        } else {
            innerRadius
        }
        val maxTitleWidth = ((titleChordHalfWidth - horizontalMargin) * 2f).coerceAtLeast(0f)

        paint.typeface = ResourcesCompat.getFont(context, R.font.medium)
        paint.color = ContextCompat.getColor(context, R.color.subTextColor)
        val origCenterTitleSize = context.resources.getDimension(com.intuit.ssp.R.dimen._12ssp)
        val minCenterTitleSize = context.resources.getDimension(com.intuit.ssp.R.dimen._9ssp)

        drawTextAutoFitAndEllipsize(
            canvas = canvas,
            text = centerTitle,
            cx = cx,
            cy = cy - titleYOffset,
            paint = paint,
            originalTextSize = origCenterTitleSize,
            minTextSize = minCenterTitleSize,
            maxWidth = maxTitleWidth
        )

        // Draw totalText (number inside the chart)
        val numberYOffset = context.resources.getDimension(com.intuit.sdp.R.dimen._18sdp)
        val numberChordHalfWidth = if (innerRadius > numberYOffset) {
            kotlin.math.sqrt(innerRadius * innerRadius - numberYOffset * numberYOffset)
        } else {
            innerRadius
        }
        val maxNumberWidth = ((numberChordHalfWidth - horizontalMargin) * 2f).coerceAtLeast(0f)

        paint.typeface = ResourcesCompat.getFont(context, R.font.bold)
        paint.color = ContextCompat.getColor(context, R.color.TextColor)
        val origTotalTextSize = context.resources.getDimension(com.intuit.ssp.R.dimen._16ssp)
        val minTotalTextSize = context.resources.getDimension(com.intuit.ssp.R.dimen._11ssp)

        val symbol = PrefManager.currencySymbol
        val totalText = "$symbol ${CurrencyHelper.formatAmount(total.toDouble())}"

        drawTextAutoFitAndEllipsize(
            canvas = canvas,
            text = totalText,
            cx = cx,
            cy = cy + numberYOffset,
            paint = paint,
            originalTextSize = origTotalTextSize,
            minTextSize = minTotalTextSize,
            maxWidth = maxNumberWidth
        )
    }

    private fun drawTextAutoFitAndEllipsize(
        canvas: Canvas,
        text: String,
        cx: Float,
        cy: Float,
        paint: Paint,
        originalTextSize: Float,
        minTextSize: Float,
        maxWidth: Float
    ) {
        paint.textSize = originalTextSize
        var currentSize = originalTextSize
        var textWidth = paint.measureText(text)

        while (textWidth > maxWidth && currentSize > minTextSize) {
            currentSize -= 1f
            paint.textSize = currentSize
            textWidth = paint.measureText(text)
        }

        val ellipsizedText = if (textWidth > maxWidth) {
            TextUtils.ellipsize(
                text,
                TextPaint(paint),
                maxWidth,
                TextUtils.TruncateAt.END
            ).toString()
        } else {
            text
        }

        canvas.drawText(ellipsizedText, cx, cy, paint)
    }
}
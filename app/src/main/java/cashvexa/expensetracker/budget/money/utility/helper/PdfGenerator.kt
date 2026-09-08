package cashvexa.expensetracker.budget.money.utility.helper

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import cashvexa.expensetracker.budget.money.data.model.Transaction
import cashvexa.expensetracker.budget.money.data.model.Account
import cashvexa.expensetracker.budget.money.utility.Constants
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import android.content.Context
import android.graphics.Canvas
import cashvexa.expensetracker.budget.money.R

object PdfGenerator {

    fun generateStatement(
        context: Context,
        account: Account,
        transactions: List<Transaction>,
        outputStream: OutputStream,
        themeColor: Int = "#4CAF50".toColorInt(),
        periodText: String = "All Time"
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var pageNumber = 1
        val paint = Paint()
        val brandPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
            color = themeColor
        }
        val statementTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 16f
            color = Color.DKGRAY
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.DKGRAY
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 11f
            color = Color.BLACK
        }
        val lightTextPaint = Paint(textPaint).apply { color = Color.GRAY }
        val tableHeaderPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.WHITE
        }
        val amountIncomePaint = Paint(textPaint).apply {
            color = "#388E3C".toColorInt()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val amountExpensePaint = Paint(textPaint).apply {
            color = "#D32F2F".toColorInt()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val summaryTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            color = Color.DKGRAY
        }
        val summaryAmountPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
            color = Color.BLACK
        }

        val gridPaint = Paint().apply {
            color = "#33000000".toColorInt()
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }

        var yPosition = 60f
        val leftMargin = 40f
        val rightMargin = 555f
        
        val drawFooter = { c: Canvas, pNum: Int ->
            val footerY = pageHeight - 30f
            c.drawText(context.getString(R.string.generated_securely), leftMargin, footerY, lightTextPaint)
            val pageStr = context.getString(R.string.page_number, pNum)
            val pWidth = lightTextPaint.measureText(pageStr)
            c.drawText(pageStr, rightMargin - pWidth, footerY, lightTextPaint)
        }

        canvas.drawText("ExpenseBook", leftMargin, yPosition, brandPaint)
        
        val statementText = context.getString(R.string.statement_of_account)
        val statementWidth = statementTitlePaint.measureText(statementText)
        canvas.drawText(statementText, rightMargin - statementWidth, yPosition - 10f, statementTitlePaint)
        
        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val generatedDateStr = "Generated on: ${sdfDate.format(Date())}"
        val dateWidth = lightTextPaint.measureText(generatedDateStr)
        canvas.drawText(generatedDateStr, rightMargin - dateWidth, yPosition + 10f, lightTextPaint)
        
        yPosition += 40f
        
        canvas.drawText(context.getString(R.string.account_name_label, account.name), leftMargin, yPosition, headerPaint)
        yPosition += 20f
        
        val rawAccNum = account.accountNumber
        val maskedAccNum = if (rawAccNum.length > 4) {
            "xxxx xxxx xxxx " + rawAccNum.substring(rawAccNum.length - 4)
        } else {
            rawAccNum
        }
        canvas.drawText(context.getString(R.string.account_number_label, maskedAccNum), leftMargin, yPosition, headerPaint)
        yPosition += 20f
        canvas.drawText(context.getString(R.string.period_label, periodText), leftMargin, yPosition, headerPaint)
        yPosition += 30f

        val totalIncome = transactions.filter { it.type.equals(Constants.TYPE_INCOME, true) }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type.equals(Constants.TYPE_EXPENSE, true) }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        val summaryBoxTop = yPosition
        val summaryBoxBottom = yPosition + 60f
        paint.color = "#F5F7FA".toColorInt()
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(leftMargin, summaryBoxTop, rightMargin, summaryBoxBottom, 8f, 8f, paint)
        
        val boxWidth = rightMargin - leftMargin
        val sectionWidth = boxWidth / 3f
        
        val incText = "${PrefManager.currencySymbol}${CurrencyHelper.formatAmount(totalIncome)}"
        val expText = "${PrefManager.currencySymbol}${CurrencyHelper.formatAmount(totalExpense)}"
        val balText = "${PrefManager.currencySymbol}${CurrencyHelper.formatAmount(netBalance)}"
        
        val drawCenteredInBox = { title: String, amt: String, sectionIndex: Int, color: Int ->
            val centerX = leftMargin + (sectionIndex * sectionWidth) + (sectionWidth / 2f)
            val tWidth = summaryTitlePaint.measureText(title)
            canvas.drawText(title, centerX - (tWidth/2f), summaryBoxTop + 25f, summaryTitlePaint)
            
            val amtP = Paint(summaryAmountPaint).apply { this.color = color }
            val aWidth = amtP.measureText(amt)
            canvas.drawText(amt, centerX - (aWidth/2f), summaryBoxTop + 45f, amtP)
        }
        
        drawCenteredInBox(context.getString(R.string.total_income), incText, 0, "#388E3C".toColorInt())
        drawCenteredInBox(context.getString(R.string.text_total_expense_center), expText, 1, "#D32F2F".toColorInt())
        drawCenteredInBox(context.getString(R.string.closing_balance), balText, 2, if(netBalance >= 0) "#388E3C".toColorInt() else "#D32F2F".toColorInt())
        
        yPosition = summaryBoxBottom + 30f

        val vCol3 = leftMargin + 105f
        val vCol4 = rightMargin - 150f

        val drawTableHeader = { c: Canvas, y: Float ->
            paint.color = themeColor
            paint.style = Paint.Style.FILL
            c.drawRect(leftMargin, y, rightMargin, y + 25f, paint)

            c.drawText(context.getString(R.string.date), leftMargin + 5f, y + 17f, tableHeaderPaint)
            c.drawText(context.getString(R.string.category), vCol3, y + 17f, tableHeaderPaint)
            c.drawText(context.getString(R.string.type), vCol4, y + 17f, tableHeaderPaint)
            val aHeader = context.getString(R.string.amount)
            val aW = tableHeaderPaint.measureText(aHeader)
            c.drawText(aHeader, rightMargin - aW - 5f, y + 17f, tableHeaderPaint)
        }

        drawTableHeader(canvas, yPosition)
        yPosition += 25f

        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        for (tx in transactions) {
            if (yPosition > pageHeight - 50f) {
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                yPosition = 40f
                drawTableHeader(canvas, yPosition)
                yPosition += 25f
            }

            val dateStr = sdf.format(Date(tx.date))
            val catStr = tx.category
            val typeStr = tx.type.uppercase()
            val isIncome = tx.type.equals(Constants.TYPE_INCOME, true)
            val prefix = if (isIncome) "+" else "-"
            val amtStr = "$prefix${PrefManager.currencySymbol}${CurrencyHelper.formatAmount(tx.amount)}"
            val amtPaint = if (isIncome) amountIncomePaint else amountExpensePaint

            val noteText = tx.notes ?: ""
            val itemRowHeight = if (noteText.isNotBlank()) 35f else 22f

            canvas.drawText(dateStr, leftMargin + 5f, yPosition + 15f, textPaint)

            var categoryDisplayText = catStr
            val maxCatWidth = (vCol4 - vCol3) - 10f
            if (textPaint.measureText(categoryDisplayText) > maxCatWidth) {
                while (categoryDisplayText.length > 3 && textPaint.measureText("$categoryDisplayText...") > maxCatWidth) {
                    categoryDisplayText = categoryDisplayText.substring(0, categoryDisplayText.length - 1)
                }
                categoryDisplayText += "..."
            }
            canvas.drawText(categoryDisplayText, vCol3, yPosition + 15f, textPaint)

            if (noteText.isNotBlank()) {
                var noteDisplayText = noteText
                val maxNoteWidth = (vCol4 - vCol3) - 10f
                if (lightTextPaint.measureText(noteDisplayText) > maxNoteWidth) {
                    while (noteDisplayText.length > 3 && lightTextPaint.measureText("$noteDisplayText...") > maxNoteWidth) {
                        noteDisplayText = noteDisplayText.substring(0, noteDisplayText.length - 1)
                    }
                    noteDisplayText += "..."
                }
                canvas.drawText(noteDisplayText, vCol3, yPosition + 28f, lightTextPaint)
            }

            canvas.drawText(typeStr, vCol4, yPosition + 15f, textPaint)

            val aW = amtPaint.measureText(amtStr)
            canvas.drawText(amtStr, rightMargin - aW - 5f, yPosition + 15f, amtPaint)

            canvas.drawLine(leftMargin, yPosition + itemRowHeight, rightMargin, yPosition + itemRowHeight, gridPaint)
            yPosition += itemRowHeight
        }

        drawFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
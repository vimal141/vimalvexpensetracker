package com.tb.example.expensebook.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tb.example.expensebook.databinding.ActivityPdfPreviewBinding
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import com.tb.example.expensebook.utility.helper.PdfGenerator
import com.tb.example.expensebook.utility.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class PdfPreviewActivity : BaseActivity<ActivityPdfPreviewBinding>() {
    private val viewModel: TransactionViewModel by viewModel()

    private var selectedThemeColor: Int = Color.parseColor("#4CAF50")
    private val themeColors = listOf(
        "#4CAF50",
        "#2196F3",
        "#F44336",
        "#9C27B0",
        "#FF9800",
        "#607D8B",
        "#000000"
    )

    private var startTime = 0L
    private var endTime = 0L
    private var periodText = "All Time"

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportDataToPdf(uri)
            }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityPdfPreviewBinding {
        return ActivityPdfPreviewBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        
        startTime = intent.getLongExtra(Constants.EXTRA_START_TIME, 0L)
        endTime = intent.getLongExtra(Constants.EXTRA_END_TIME, 0L)
        periodText = intent.getStringExtra(Constants.EXTRA_PERIOD_TEXT) ?: "All Time"

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupColorPicker()
        
        binding.btnExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, getString(com.tb.example.expensebook.R.string.default_pdf_name))
            }
            createDocumentLauncher.launch(intent)
        }

        updatePreview()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupColorPicker() {
        val size = (50 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()

        themeColors.forEach { colorHex ->
            val colorInt = Color.parseColor(colorHex)
            
            val view = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, margin, margin, margin)
                }
                
                val shape = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(colorInt)
                    setStroke(2, Color.LTGRAY)
                }
                background = shape
                
                setOnClickListener {
                    selectedThemeColor = colorInt
                    updatePreview()
                    updateSelectionUI(this)
                }
            }
            
            binding.colorPickerContainer.addView(view)
            
            if (colorInt == selectedThemeColor) {
                updateSelectionUI(view)
            }
        }
    }

    private fun updateSelectionUI(selectedView: View) {
        for (i in 0 until binding.colorPickerContainer.childCount) {
            val view = binding.colorPickerContainer.getChildAt(i)
            if (view is ImageView) {
                if (view == selectedView) {
                    view.setImageResource(android.R.drawable.ic_menu_edit)
                    view.setColorFilter(Color.WHITE)
                } else {
                    view.setImageDrawable(null)
                }
            }
        }
    }

    private fun updatePreview() {
        lifecycleScope.launch {
            val tempFile = File(cacheDir, "preview_statement.pdf")
            try {
                val account = withContext(Dispatchers.IO) { viewModel.getAccountDetails() }
                val transactions = withContext(Dispatchers.IO) { viewModel.getTransactionsForExport(startTime, endTime) }
                
                if (account == null) {
                    Toast.makeText(this@PdfPreviewActivity, getString(com.tb.example.expensebook.R.string.toast_error_account_missing), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    FileOutputStream(tempFile).use { outputStream ->
                        PdfGenerator.generateStatement(this@PdfPreviewActivity, account, transactions, outputStream, selectedThemeColor, periodText)
                    }
                }

                val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)
                
                if (pdfRenderer.pageCount > 0) {
                    val page = pdfRenderer.openPage(0)
                    val width = 1200
                    val height = (width.toFloat() / page.width * page.height).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    
                    bitmap.eraseColor(Color.WHITE)
                    
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    binding.pdfPreviewImage.setImageBitmap(bitmap)
                    page.close()
                }
                
                pdfRenderer.close()
                fileDescriptor.close()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun exportDataToPdf(uri: Uri) {
        lifecycleScope.launch {
            try {
                val account = withContext(Dispatchers.IO) { viewModel.getAccountDetails() }
                val transactions = withContext(Dispatchers.IO) { viewModel.getTransactionsForExport(startTime, endTime) }
                
                if (account == null) {
                    return@launch
                }
                
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        PdfGenerator.generateStatement(this@PdfPreviewActivity, account, transactions, outputStream, selectedThemeColor, periodText)
                    }
                }
                
                Toast.makeText(this@PdfPreviewActivity, getString(com.tb.example.expensebook.R.string.toast_pdf_exported), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PdfPreviewActivity, getString(com.tb.example.expensebook.R.string.toast_pdf_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
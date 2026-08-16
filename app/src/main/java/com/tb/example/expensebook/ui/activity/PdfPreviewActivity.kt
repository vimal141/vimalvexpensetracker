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
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tb.example.expensebook.R
import com.tb.example.expensebook.databinding.ActivityPdfPreviewBinding
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import com.tb.example.expensebook.utility.Constants
import com.tb.example.expensebook.utility.helper.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class PdfPreviewActivity : BaseActivity<ActivityPdfPreviewBinding>() {
    private val viewModel: TransactionViewModel by viewModel()

    private var selectedThemeColor: Int = Color.parseColor("#1B8A5A")
    private val themeColors = listOf(
        "#1B8A5A", // App Emerald Green
        "#2563EB", // Royal Azure
        "#7C3AED", // Violet
        "#C2410C", // Terracotta
        "#E11D48", // Crimson Rose
        "#0891B2", // Teal Cyan
        "#111827"  // Onyx Black
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

        binding.btnBack.setOnClickListener { finish() }

        setupColorPicker()

        binding.btnExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                putExtra(Intent.EXTRA_TITLE, getString(R.string.default_pdf_name))
            }
            createDocumentLauncher.launch(intent)
        }

        updatePreview()

        // Load adaptive banner ad
        com.tb.example.expensebook.utility.ads.AdManager.loadAdaptiveBanner(
            this,
            binding.flAdContainer,
            com.tb.example.expensebook.utility.ads.RemoteConfigHelper.KEY_SHOW_PDF_PREVIEW_BANNER_AD
        )
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupColorPicker() {
        binding.colorPickerContainer.removeAllViews()
        val swatchSize = (40 * resources.displayMetrics.density).toInt()
        val margin = (6 * resources.displayMetrics.density).toInt()

        themeColors.forEach { colorHex ->
            val colorInt = Color.parseColor(colorHex)

            val frameLayout = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(swatchSize, swatchSize).apply {
                    setMargins(margin, 0, margin, 0)
                }
            }

            val swatchView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                tag = colorInt
            }

            updateSwatchDrawable(swatchView, colorInt, colorInt == selectedThemeColor)

            frameLayout.setOnClickListener {
                if (selectedThemeColor != colorInt) {
                    selectedThemeColor = colorInt
                    updateColorPickerUI()
                    updatePreview()
                }
            }

            frameLayout.addView(swatchView)
            binding.colorPickerContainer.addView(frameLayout)
        }
    }

    private fun updateSwatchDrawable(view: ImageView, colorInt: Int, isSelected: Boolean) {
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(colorInt)
            if (isSelected) {
                setStroke((3 * resources.displayMetrics.density).toInt(), ContextCompat.getColor(this@PdfPreviewActivity, R.color.TextColor))
            } else {
                setStroke((1 * resources.displayMetrics.density).toInt(), Color.parseColor("#33000000"))
            }
        }
        view.background = shape

        if (isSelected) {
            view.setImageResource(R.drawable.ic_check)
            view.setColorFilter(Color.WHITE)
            val padding = (8 * resources.displayMetrics.density).toInt()
            view.setPadding(padding, padding, padding, padding)
        } else {
            view.setImageDrawable(null)
            view.setPadding(0, 0, 0, 0)
        }
    }

    private fun updateColorPickerUI() {
        for (i in 0 until binding.colorPickerContainer.childCount) {
            val frame = binding.colorPickerContainer.getChildAt(i) as? FrameLayout
            val swatchView = frame?.getChildAt(0) as? ImageView
            val colorInt = swatchView?.tag as? Int ?: continue
            updateSwatchDrawable(swatchView, colorInt, colorInt == selectedThemeColor)
        }
    }

    private fun updatePreview() {
        binding.progressBar.visibility = View.VISIBLE
        binding.pdfPreviewImage.visibility = View.INVISIBLE

        lifecycleScope.launch {
            val tempFile = File(cacheDir, "preview_statement.pdf")
            try {
                val account = withContext(Dispatchers.IO) { viewModel.getAccountDetails() }
                val transactions = withContext(Dispatchers.IO) { viewModel.getTransactionsForExport(startTime, endTime) }

                if (account == null) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@PdfPreviewActivity, getString(R.string.toast_error_account_missing), Toast.LENGTH_SHORT).show()
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

                binding.progressBar.visibility = View.GONE
                binding.pdfPreviewImage.visibility = View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
                binding.progressBar.visibility = View.GONE
                binding.pdfPreviewImage.visibility = View.VISIBLE
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

                Toast.makeText(this@PdfPreviewActivity, getString(R.string.toast_pdf_exported), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PdfPreviewActivity, getString(R.string.toast_pdf_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
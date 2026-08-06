package com.tb.example.expensebook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tb.example.expensebook.databinding.BottomSheetExportFilterBinding
import com.tb.example.expensebook.ui.activity.PdfPreviewActivity
import com.tb.example.expensebook.utility.Constants
import java.util.Calendar

class ExportFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetExportFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetExportFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            var startTime = 0L
            var endTime = 0L
            var periodText = getString(com.tb.example.expensebook.R.string.all_time)

            val cal = Calendar.getInstance()
            endTime = cal.timeInMillis

            when (binding.rgPeriod.checkedRadioButtonId) {
                binding.rbThisMonth.id -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    startTime = cal.timeInMillis
                    periodText = getString(com.tb.example.expensebook.R.string.this_month)
                }
                binding.rbLast3Months.id -> {
                    cal.add(Calendar.MONTH, -3)
                    startTime = cal.timeInMillis
                    periodText = getString(com.tb.example.expensebook.R.string.last_3_months)
                }
                binding.rbLast6Months.id -> {
                    cal.add(Calendar.MONTH, -6)
                    startTime = cal.timeInMillis
                    periodText = getString(com.tb.example.expensebook.R.string.last_6_months)
                }
                binding.rbThisYear.id -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    startTime = cal.timeInMillis
                    periodText = getString(com.tb.example.expensebook.R.string.this_year)
                }
                binding.rbAllTime.id -> {
                    startTime = 0L
                    endTime = 0L
                    periodText = getString(com.tb.example.expensebook.R.string.all_time)
                }
            }

            val intent = Intent(requireContext(), PdfPreviewActivity::class.java).apply {
                putExtra(Constants.EXTRA_START_TIME, startTime)
                putExtra(Constants.EXTRA_END_TIME, endTime)
                putExtra(Constants.EXTRA_PERIOD_TEXT, periodText)
            }
            startActivity(intent)
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

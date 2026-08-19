package cashvexa.expensetracker.budget.money.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cashvexa.expensetracker.budget.money.ui.activity.PdfPreviewActivity
import cashvexa.expensetracker.budget.money.ui.viewmodel.TransactionViewModel
import cashvexa.expensetracker.budget.money.utility.Constants
import cashvexa.expensetracker.budget.money.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import cashvexa.expensetracker.budget.money.databinding.BottomSheetExportFilterBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Calendar

class ExportFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetExportFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by activityViewModel()

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
            var periodText = getString(R.string.all_time)

            val cal = Calendar.getInstance()
            endTime = cal.timeInMillis

            when (binding.rgPeriod.checkedRadioButtonId) {
                binding.rbThisMonth.id -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    startTime = cal.timeInMillis
                    periodText = getString(R.string.this_month)
                }
                binding.rbLast3Months.id -> {
                    cal.add(Calendar.MONTH, -3)
                    startTime = cal.timeInMillis
                    periodText = getString(R.string.last_3_months)
                }
                binding.rbLast6Months.id -> {
                    cal.add(Calendar.MONTH, -6)
                    startTime = cal.timeInMillis
                    periodText = getString(R.string.last_6_months)
                }
                binding.rbThisYear.id -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    startTime = cal.timeInMillis
                    periodText = getString(R.string.this_year)
                }
                binding.rbAllTime.id -> {
                    startTime = 0L
                    endTime = 0L
                    periodText = getString(R.string.all_time)
                }
            }

            lifecycleScope.launch {
                val list = viewModel.getTransactionsForExport(startTime, endTime)
                if (list.isEmpty()) {
                    dismiss()
                    Toast.makeText(requireContext(), getString(R.string.toast_no_transactions_period), Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(requireContext(), PdfPreviewActivity::class.java).apply {
                        putExtra(Constants.EXTRA_START_TIME, startTime)
                        putExtra(Constants.EXTRA_END_TIME, endTime)
                        putExtra(Constants.EXTRA_PERIOD_TEXT, periodText)
                    }
                    startActivity(intent)
                    dismiss()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { d ->
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
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

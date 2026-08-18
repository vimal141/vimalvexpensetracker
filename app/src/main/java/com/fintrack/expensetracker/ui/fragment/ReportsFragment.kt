package com.fintrack.expensetracker.ui.fragment

import com.fintrack.expensetracker.R
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.fintrack.expensetracker.data.model.CategorySum
import com.fintrack.expensetracker.data.model.Transaction
import com.fintrack.expensetracker.ui.viewmodel.TransactionViewModel
import com.fintrack.expensetracker.utility.Constants
import com.fintrack.expensetracker.utility.customview.PieChartView
import com.fintrack.expensetracker.utility.helper.CategoryHelper
import com.fintrack.expensetracker.utility.helper.CurrencyHelper
import com.fintrack.expensetracker.utility.helper.PrefManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.fintrack.expensetracker.databinding.FragmentReportsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by activityViewModel()
    private var currentReportType: String = Constants.TYPE_EXPENSE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        updateReportTypeUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        updateCurrencyUI()
    }

    private fun updateCurrencyUI() {
        val symbol = PrefManager.currencySymbol

        val income = viewModel.reportTotalIncome.value
        binding.txtReportIncome.text = "$symbol ${CurrencyHelper.formatAmount(income)}"

        val expense = viewModel.reportTotalExpense.value
        binding.txtReportExpense.text = "$symbol ${CurrencyHelper.formatAmount(expense)}"

        updateReportData()
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener { viewModel.previousReportPeriod() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextReportPeriod() }

        val periodSelectorClickListener = View.OnClickListener {
            showPeriodPickerDialog()
        }
        binding.cardMonthSelector.setOnClickListener(periodSelectorClickListener)
        binding.txtSelectedMonth.setOnClickListener(periodSelectorClickListener)

        binding.tabReportExpense.setOnClickListener { selectReportType(Constants.TYPE_EXPENSE) }
        binding.tabReportIncome.setOnClickListener { selectReportType(Constants.TYPE_INCOME) }

        val tabs = mapOf(
            binding.tabDaily to Constants.PERIOD_DAILY,
            binding.tabWeekly to Constants.PERIOD_WEEKLY,
            binding.tabMonthly to Constants.PERIOD_MONTHLY,
            binding.tabYearly to Constants.PERIOD_YEARLY
        )
        tabs.forEach { (tab, type) ->
            tab.setOnClickListener {
                tabs.keys.forEach { t ->
                    if (t == tab) {
                        t.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                        t.setBackgroundResource(R.drawable.bg_tab)
                        t.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primaryColor)
                    } else {
                        t.setTextColor(ContextCompat.getColor(requireContext(), R.color.subTextColor))
                        t.background = null
                    }
                }
                viewModel.setReportPeriodType(type)
            }
        }
        binding.tabMonthly.performClick()
    }

    private fun showPeriodPickerDialog() {
        when (viewModel.reportPeriodType.value) {
            Constants.PERIOD_DAILY -> {
                val picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(viewModel.reportCalendar.value.timeInMillis)
                    .build()
                picker.addOnPositiveButtonClickListener { selection ->
                    val cal = Calendar.getInstance().apply { timeInMillis = selection }
                    viewModel.setReportDate(cal)
                }
                picker.show(childFragmentManager, "DAILY_PICKER")
            }
            Constants.PERIOD_WEEKLY -> {
                val currentCal = viewModel.reportCalendar.value
                val mondayCal = (currentCal.clone() as Calendar).apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                }
                val sundayCal = (mondayCal.clone() as Calendar).apply {
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                }
                val initialRange = Pair(mondayCal.timeInMillis, sundayCal.timeInMillis)

                val picker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select 1 Week (7 Days)")
                    .setSelection(initialRange)
                    .build()
                picker.addOnPositiveButtonClickListener { selectionPair ->
                    val startMillis = selectionPair.first
                    if (startMillis != null) {
                        viewModel.setReportWeekStartDate(startMillis)
                    }
                }
                picker.show(childFragmentManager, "WEEKLY_RANGE_PICKER")
            }
            Constants.PERIOD_YEARLY -> {
                showYearPickerDialog()
            }
            else -> { // PERIOD_MONTHLY
                showMonthYearPickerDialog()
            }
        }
    }

    private fun showMonthYearPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.picker_month)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.picker_year)

        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = months

        val currentCal = viewModel.reportCalendar.value
        val currentYear = currentCal.get(Calendar.YEAR)

        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100

        monthPicker.value = currentCal.get(Calendar.MONTH)
        yearPicker.value = currentYear

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_month))
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.select)) { _, _ ->
                viewModel.setReportMonthYear(monthPicker.value, yearPicker.value)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
        dialog.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.65f)
        }
    }

    private fun showYearPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_picker, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.picker_year)

        val currentCal = viewModel.reportCalendar.value
        val currentYear = currentCal.get(Calendar.YEAR)

        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Year")
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.select)) { _, _ ->
                viewModel.setReportYear(yearPicker.value)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
        dialog.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.65f)
        }
    }

    private fun selectReportType(type: String) {
        currentReportType = type
        updateReportTypeUI()
        updateReportData()
    }

    private fun updateReportTypeUI() {
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.subTextColor)
        val activeTextColor = ContextCompat.getColor(requireContext(), android.R.color.white)

        if (currentReportType == Constants.TYPE_EXPENSE) {
            binding.tabReportExpense.setTextColor(activeTextColor)
            binding.tabReportExpense.setBackgroundResource(R.drawable.bg_tab)
            binding.tabReportExpense.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.expense_red)

            binding.tabReportIncome.setTextColor(inactiveColor)
            binding.tabReportIncome.background = null

            binding.txtCatTitle.text = getString(R.string.expense_breakdown)
        } else {
            binding.tabReportIncome.setTextColor(activeTextColor)
            binding.tabReportIncome.setBackgroundResource(R.drawable.bg_tab)
            binding.tabReportIncome.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.income_green)

            binding.tabReportExpense.setTextColor(inactiveColor)
            binding.tabReportExpense.background = null

            binding.txtCatTitle.text = "Income Breakdown"
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reportCalendar.collectLatest { cal ->
                val period = viewModel.reportPeriodType.value
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val calYear = cal.get(Calendar.YEAR)
                val isCurrentYear = currentYear == calYear

                val fmt = when (period) {
                    Constants.PERIOD_DAILY -> {
                        if (isCurrentYear) SimpleDateFormat("dd MMM", Locale.US).format(cal.time)
                        else SimpleDateFormat("dd MMM yyyy", Locale.US).format(cal.time)
                    }
                    Constants.PERIOD_WEEKLY -> {
                        val custom = viewModel.reportCustomWeekRange.value
                        if (custom != null) {
                            val startCal = Calendar.getInstance().apply { timeInMillis = custom.first }
                            val endCal = Calendar.getInstance().apply { timeInMillis = custom.second }
                            val startStr = SimpleDateFormat("dd MMM", Locale.US).format(startCal.time)
                            val endFormat = if (endCal.get(Calendar.YEAR) == currentYear) "dd MMM" else "dd MMM yyyy"
                            val endStr = SimpleDateFormat(endFormat, Locale.US).format(endCal.time)
                            "$startStr - $endStr"
                        } else {
                            val c = cal.clone() as Calendar
                            c.firstDayOfWeek = Calendar.MONDAY
                            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            val start = SimpleDateFormat("dd MMM", Locale.US).format(c.time)
                            c.add(Calendar.DAY_OF_YEAR, 6)
                            val endYear = c.get(Calendar.YEAR)
                            val endFormat = if (endYear == currentYear) "dd MMM" else "dd MMM yyyy"
                            val end = SimpleDateFormat(endFormat, Locale.US).format(c.time)
                            "$start - $end"
                        }
                    }
                    Constants.PERIOD_YEARLY -> SimpleDateFormat("yyyy", Locale.US).format(cal.time)
                    else -> {
                        if (isCurrentYear) SimpleDateFormat("MMM", Locale.US).format(cal.time)
                        else SimpleDateFormat("MMM yyyy", Locale.US).format(cal.time)
                    }
                }
                binding.txtSelectedMonth.text = fmt
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reportTotalIncome.collectLatest { income ->
                binding.txtReportIncome.text = "${PrefManager.currencySymbol} ${CurrencyHelper.formatAmount(income)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reportTotalExpense.collectLatest { expense ->
                binding.txtReportExpense.text = "${PrefManager.currencySymbol} ${CurrencyHelper.formatAmount(expense)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactionsForReport.collectLatest { _ ->
                updateReportData()
            }
        }
    }

    private fun updateReportData() {
        val allTransactions = viewModel.transactionsForReport.value ?: emptyList()
        val filtered = allTransactions.filter {
            it.type.trim().equals(currentReportType, ignoreCase = true)
        }
        buildPieChart(filtered)
        updateCategoryBreakdown(filtered)
    }

    private fun buildPieChart(transactions: List<Transaction>?) {
        val centerTitle = if (currentReportType == Constants.TYPE_EXPENSE) {
            getString(R.string.text_total_expense_center)
        } else {
            getString(R.string.total_income)
        }

        if (transactions.isNullOrEmpty()) {
            binding.pieChartView.setData(emptyList(), centerTitle)
            return
        }

        val categoryMap = mutableMapOf<String, Double>()
        transactions.forEach { t ->
            categoryMap[t.category] = (categoryMap[t.category] ?: 0.0) + t.amount
        }

        val slices = categoryMap.map { (cat, sum) ->
            val color = CategoryHelper.getColor(requireContext(), cat)
            PieChartView.Slice(sum.toFloat(), color)
        }
        binding.pieChartView.setData(slices, centerTitle)
    }

    private fun updateCategoryBreakdown(transactions: List<Transaction>?) {
        binding.layoutCategories.removeAllViews()
        if (transactions.isNullOrEmpty()) {
            binding.cardCategory.visibility = View.GONE
            return
        }
        binding.cardCategory.visibility = View.VISIBLE

        val categorySums = transactions.groupBy { it.category }
            .map { (cat, transList) -> CategorySum(cat, transList.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        val totalVolume = categorySums.sumOf { it.total }
        val symbol = PrefManager.currencySymbol
        val df = DecimalFormat("0.##")

        categorySums.take(10).forEach { item ->
            val pctDouble = if (totalVolume > 0) (item.total / totalVolume * 100.0) else 0.0
            val pctText = "${df.format(pctDouble)}%"
            val pctInt = Math.round(pctDouble).toInt()
            val row = layoutInflater.inflate(R.layout.item_category_row, binding.layoutCategories, false)

            row.findViewById<View>(R.id.view_color_dot)
                .setBackgroundColor(CategoryHelper.getColor(requireContext(), item.category))
            row.findViewById<TextView>(R.id.txt_category_name).text = item.category
            row.findViewById<TextView>(R.id.txt_category_amount).text = "$symbol ${CurrencyHelper.formatAmount(item.total)}"
            row.findViewById<TextView>(R.id.txt_category_percent).text = pctText

            val progressBar = row.findViewById<ProgressBar>(R.id.progress_category)
            progressBar.progress = pctInt
            progressBar.progressTintList = ColorStateList.valueOf(
                CategoryHelper.getColor(requireContext(), item.category)
            )

            binding.layoutCategories.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

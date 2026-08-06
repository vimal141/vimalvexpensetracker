package com.tb.example.expensebook.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import com.tb.example.expensebook.data.model.CategorySum
import com.tb.example.expensebook.utility.helper.CategoryHelper
import com.tb.example.expensebook.utility.customview.PieChartView
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.data.model.Transaction
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import com.tb.example.expensebook.databinding.FragmentReportsBinding
import com.tb.example.expensebook.utility.Constants
import com.tb.example.expensebook.R
import java.text.SimpleDateFormat
import java.util.*

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
        binding.txtReportIncome.text = "$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(income)}"

        val expense = viewModel.reportTotalExpense.value
        binding.txtReportExpense.text = "$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(expense)}"

        updateReportData()
    }

    private fun setupListeners() {
        binding.btnPrevMonth.setOnClickListener { viewModel.previousReportPeriod() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextReportPeriod() }

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

    private fun selectReportType(type: String) {
        currentReportType = type
        updateReportTypeUI()
        updateReportData()
    }

    private fun updateReportTypeUI() {
        if (currentReportType == Constants.TYPE_EXPENSE) {
            binding.tabReportExpense.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            binding.tabReportExpense.setBackgroundResource(R.drawable.bg_tab)
            binding.tabReportIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.subTextColor))
            binding.tabReportIncome.background = null

            binding.txtCatTitle.text = getString(R.string.expense_breakdown)
        } else {
            binding.tabReportIncome.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            binding.tabReportIncome.setBackgroundResource(R.drawable.bg_tab)
            binding.tabReportExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.subTextColor))
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
                binding.txtReportIncome.text = "${PrefManager.currencySymbol} ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(income)}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reportTotalExpense.collectLatest { expense ->
                binding.txtReportExpense.text = "${PrefManager.currencySymbol} ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(expense)}"
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
        val df = java.text.DecimalFormat("0.##")

        categorySums.take(10).forEach { item ->
            val pctDouble = if (totalVolume > 0) (item.total / totalVolume * 100.0) else 0.0
            val pctText = "${df.format(pctDouble)}%"
            val pctInt = Math.round(pctDouble).toInt()
            val row = layoutInflater.inflate(R.layout.item_category_row, binding.layoutCategories, false)

            row.findViewById<View>(R.id.view_color_dot)
                .setBackgroundColor(CategoryHelper.getColor(requireContext(), item.category))
            row.findViewById<TextView>(R.id.txt_category_name).text = item.category
            row.findViewById<TextView>(R.id.txt_category_amount).text = "$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(item.total)}"
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

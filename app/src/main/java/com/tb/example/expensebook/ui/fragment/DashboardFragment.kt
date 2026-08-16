package com.tb.example.expensebook.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.Transaction
import com.tb.example.expensebook.ui.adapter.TransactionAdapter
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import com.tb.example.expensebook.databinding.FragmentDashboardBinding
import com.tb.example.expensebook.ui.activity.AccountsActivity
import com.tb.example.expensebook.ui.activity.AddTransactionActivity
import com.tb.example.expensebook.ui.activity.MainActivity
import com.tb.example.expensebook.utility.Constants
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import com.tb.example.expensebook.utility.helper.bounceOnClick
import com.tb.example.expensebook.utility.helper.slideUpFadeIn

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: TransactionViewModel by activityViewModel()
    private lateinit var adapter: TransactionAdapter
    private var isBalanceVisible = true
    private var currentBalanceText = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        animateCards()
        loadNativeAd()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentAccountId.collect {
                        val account = viewModel.getAccountDetails()
                        binding.txtCardHolderName.text = account?.name?.uppercase() ?: getString(R.string.card_holder)
                        
                        if (account != null && account.accountNumber.length >= 4) {
                            val last4 = account.accountNumber.takeLast(4)
                            binding.txtAccountNumber.text = getString(R.string.format_card_number, last4)
                        } else {
                            binding.txtAccountNumber.text = getString(R.string.default_card_number)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCurrencyUI()
    }

    private fun updateCurrencyUI() {
        val symbol = PrefManager.currencySymbol
        val balance = viewModel.totalBalance.value ?: 0.0
        val sign = if (balance < 0) "-" else ""
        currentBalanceText = "$sign$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(Math.abs(balance))}"
        updateBalanceVisibility()

        val income = viewModel.totalIncome.value ?: 0.0
        binding.txtIncomeValue.text = "$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(income)}"

        val expense = viewModel.totalExpense.value ?: 0.0
        binding.txtExpenseValue.text = "$symbol ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(expense)}"

        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun animateCards() {
        binding.cardBalance.slideUpFadeIn(400L, 50L)
        binding.cardIncome.slideUpFadeIn(400L, 150L)
    }

    private fun setupCardAnimation() {
        val distance = 8000 * resources.displayMetrics.density
        binding.cardBalance.cameraDistance = distance

        binding.cardBalance.cameraDistance = distance


        binding.cardBalance.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val baseZ = 10f * resources.displayMetrics.density
        val popZ = 25f * resources.displayMetrics.density

        binding.cardBalance.setOnTouchListener { view, event ->
            val centerX = view.width / 2f
            val centerY = view.height / 2f

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .translationZ(popZ)
                        .setDuration(100)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    val rotateX = ((centerY - event.y) / 25f).coerceIn(-12f, 12f)
                    val rotateY = ((event.x - centerX) / 25f).coerceIn(-12f, 12f)
                    view.rotationX = rotateX
                    view.rotationY = rotateY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val rotateX = ((centerY - event.y) / 25f).coerceIn(-12f, 12f)
                    val rotateY = ((event.x - centerX) / 25f).coerceIn(-12f, 12f)

                    view.rotationX = rotateX
                    view.rotationY = rotateY
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .rotationX(0f)
                        .rotationY(0f)
                        .translationZ(baseZ)
                        .setDuration(250)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    if (event.action == MotionEvent.ACTION_UP) {
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter =
            TransactionAdapter { transaction ->
                val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
                    putExtra(Constants.EXTRA_TRANSACTION, transaction)
                }
                startActivity(intent)
            }
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
        binding.rvTransactions.itemAnimator = DefaultItemAnimator()
    }

    private fun setupListeners() {
        binding.cardMonthSelector.setOnClickListener { showMonthYearPicker() }
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
        binding.btnViewAll.setOnClickListener {
            (requireActivity() as MainActivity).selectHistoryTab()
        }
        binding.ivVisibility.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateBalanceVisibility()
        }
        binding.ivMore.setOnClickListener {
            showManageAccountsScreen()
        }

        binding.fabAdd.bounceOnClick {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddExpanse.bounceOnClick {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            startActivity(intent)
        }


    }

    private fun updateBalanceVisibility() {
        if (isBalanceVisible) {
            binding.txtBalanceValue.text = currentBalanceText
            binding.ivVisibility.setImageResource(R.drawable.ic_visibility)
        } else {
            binding.txtBalanceValue.text = getString(R.string.text_balance_hidden)
            binding.ivVisibility.setImageResource(R.drawable.ic_visibility_off)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedCalendar.collectLatest { cal ->
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        val calYear = cal.get(Calendar.YEAR)
                        val fmt = if (calYear == currentYear) {
                            SimpleDateFormat("MMMM", Locale.US)
                        } else {
                            SimpleDateFormat("MMMM yyyy", Locale.US)
                        }
                        binding.txtSelectedMonth.text = fmt.format(cal.time)
                    }
                }

                launch {
                    viewModel.totalBalance.collectLatest { balance ->
                        val b = balance ?: 0.0
                        val sign = if (b < 0) "-" else ""
                        currentBalanceText = "$sign${PrefManager.currencySymbol} ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(Math.abs(b))}"
                        updateBalanceVisibility()
                    }
                }

                launch {
                    viewModel.totalIncome.collectLatest { income ->
                        binding.txtIncomeValue.text = "${PrefManager.currencySymbol} ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(income ?: 0.0)}"
                    }
                }

                launch {
                    viewModel.totalExpense.collectLatest { expense ->
                        binding.txtExpenseValue.text = "${PrefManager.currencySymbol} ${com.tb.example.expensebook.utility.helper.CurrencyHelper.formatAmount(expense ?: 0.0)}"
                    }
                }

                launch {
                    viewModel.transactionsForSelectedMonth.collectLatest { list ->
                        updateList(list ?: emptyList())
                    }
                }
            }
        }
    }

    private fun updateList(list: List<Transaction>) {
        if (list.isEmpty()) {
            binding.rvTransactions.visibility = View.GONE
            binding.clNoData.visibility = View.VISIBLE
            binding.btnViewAll.visibility = View.GONE
            binding.txtRecent.visibility = View.GONE
            binding.fabAdd.isVisible = false
        } else {
            binding.clNoData.visibility = View.GONE
            binding.btnViewAll.visibility = View.VISIBLE
            binding.txtRecent.visibility = View.VISIBLE
            binding.rvTransactions.visibility = View.VISIBLE
            val display = list.take(3)
            adapter.updateData(display)
            binding.btnViewAll.visibility = if (list.size > 3) View.VISIBLE else View.GONE
            binding.fabAdd.isVisible = true
        }
    }

    private fun showMonthYearPicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.picker_month)
        val yearPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.picker_year)

        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = months

        val currentCal = viewModel.selectedCalendar.value
        val currentYear = currentCal.get(Calendar.YEAR)
        
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        
        monthPicker.value = currentCal.get(Calendar.MONTH)
        yearPicker.value = currentYear

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_month))
            .setBackground(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.select)) { _, _ ->
                viewModel.setMonthYear(monthPicker.value, yearPicker.value)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.show()
        dialog.window?.apply {
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.65f)
        }
    }

    private fun showManageAccountsScreen() {
        startActivity(Intent(requireContext(), AccountsActivity::class.java))
    }

    private fun loadNativeAd() {
        activity?.let { act ->
            com.tb.example.expensebook.utility.ads.AdManager.loadNativeAd(
                activity = act,
                container = binding.flNativeAd,
                layoutResId = R.layout.layout_native_ad_card,
                screenFlagKey = com.tb.example.expensebook.utility.ads.RemoteConfigHelper.KEY_SHOW_DASHBOARD_NATIVE_AD
            )
        }
    }
}
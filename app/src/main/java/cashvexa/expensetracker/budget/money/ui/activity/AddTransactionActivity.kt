package cashvexa.expensetracker.budget.money.ui.activity

import cashvexa.expensetracker.budget.money.R
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import cashvexa.expensetracker.budget.money.data.model.Category
import cashvexa.expensetracker.budget.money.data.model.Transaction
import cashvexa.expensetracker.budget.money.ui.adapter.CategoryAdapter
import cashvexa.expensetracker.budget.money.ui.viewmodel.TransactionViewModel
import cashvexa.expensetracker.budget.money.utility.Constants
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import cashvexa.expensetracker.budget.money.utility.helper.bounceOnClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import cashvexa.expensetracker.budget.money.databinding.ActivityAddTransactionBinding
import cashvexa.expensetracker.budget.money.databinding.DialogBottomSheetDeleteBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : BaseActivity<ActivityAddTransactionBinding>() {
    override val isApplyKeyboardPadding = true
    private val viewModel: TransactionViewModel by viewModel()
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedType = Constants.TYPE_EXPENSE
    private var selectedCategory: Category? = null
    private val selectedCalendar = Calendar.getInstance()
    private var editTransaction: Transaction? = null

    override fun inflateBinding(inflater: LayoutInflater): ActivityAddTransactionBinding {
        return ActivityAddTransactionBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        editTransaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(Constants.EXTRA_TRANSACTION, Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(Constants.EXTRA_TRANSACTION) as? Transaction
        }

        setupUI()
        setupRecyclerView()
        setupListeners()
        observeData()

        if (editTransaction != null) {
            setupEditMode(editTransaction!!)
        } else {
            binding.btnDelete.visibility = View.GONE
            updateTypeUI()
        }
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupUI() {
        binding.txtCurrency.text = PrefManager.currencySymbol
        updateDateLabel()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                selectedCategory = category
                binding.layoutBottomContainer.visibility = View.VISIBLE
                binding.etAmount.post {
                    binding.etAmount.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(binding.etAmount, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        )
        binding.rvCategories.layoutManager = GridLayoutManager(this, 4)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.tabExpense.setOnClickListener { 
            selectedType = Constants.TYPE_EXPENSE
            updateTypeUI()
        }
        binding.tabIncome.setOnClickListener { 
            selectedType = Constants.TYPE_INCOME
            updateTypeUI()
        }

        binding.btnDate.setOnClickListener { showDatePicker() }
        
        binding.btnSubmit.bounceOnClick { saveTransaction() }
        binding.btnDelete.bounceOnClick { showDeleteConfirmation() }
    }


    private fun observeData() {
        lifecycleScope.launch {
            viewModel.categories.collectLatest { list ->
                categoryAdapter.submitList(list) {
                    binding.rvCategories.post {
                        binding.rvCategories.scrollToPosition(0)
                    }
                }
                
                editTransaction?.let { t ->
                    val cat = list.find { it.name == t.category }
                    cat?.let { 
                        selectedCategory = it
                        categoryAdapter.setSelectedCategory(it.id)
                        binding.layoutBottomContainer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateTypeUI() {
        viewModel.setCategoryType(selectedType)
        selectedCategory = null
        categoryAdapter.setSelectedCategory(-1)
        binding.layoutBottomContainer.visibility = View.GONE
        binding.rvCategories.post {
            binding.rvCategories.scrollToPosition(0)
        }
        
        val inactiveColor = ContextCompat.getColor(this, R.color.subTextColor)
        val activeTextColor = ContextCompat.getColor(this, android.R.color.white)
        
        binding.tabExpense.setTextColor(inactiveColor)
        binding.tabExpense.background = null
        binding.tabIncome.setTextColor(inactiveColor)
        binding.tabIncome.background = null

        when (selectedType) {
            Constants.TYPE_EXPENSE -> {
                binding.tabExpense.setTextColor(activeTextColor)
                binding.tabExpense.setBackgroundResource(R.drawable.bg_tab)
                binding.tabExpense.backgroundTintList = ContextCompat.getColorStateList(this, R.color.expense_red)
            }
            Constants.TYPE_INCOME -> {
                binding.tabIncome.setTextColor(activeTextColor)
                binding.tabIncome.setBackgroundResource(R.drawable.bg_tab)
                binding.tabIncome.backgroundTintList = ContextCompat.getColorStateList(this, R.color.income_green)
            }
        }
    }

    private fun setupEditMode(t: Transaction) {
        binding.txtTitle.text = getString(R.string.edit_transaction)
        binding.btnSubmit.text = getString(R.string.update_transaction)
        binding.btnDelete.visibility = View.VISIBLE
        binding.etAmount.setText(t.amount.toString())
        binding.etNotes.setText(t.notes ?: "")
        selectedType = t.type
        selectedCalendar.timeInMillis = t.date
        updateTypeUI()
        updateDateLabel()
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(selectedCalendar.timeInMillis)
            .setTheme(R.style.CustomDatePickerTheme)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val cal = Calendar.getInstance().apply { timeInMillis = selection }
            selectedCalendar.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            updateDateLabel()
        }
        picker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun updateDateLabel() {
        val now = Calendar.getInstance()
        if (now.get(Calendar.DATE) == selectedCalendar.get(Calendar.DATE) && 
            now.get(Calendar.MONTH) == selectedCalendar.get(Calendar.MONTH) &&
            now.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)) {
            binding.txtDate.text = getString(R.string.today)
        } else {
            val currentYear = now.get(Calendar.YEAR)
            if (selectedCalendar.get(Calendar.YEAR) == currentYear) {
                binding.txtDate.text = SimpleDateFormat("dd MMM", Locale.US).format(selectedCalendar.time)
            } else {
                binding.txtDate.text = SimpleDateFormat("dd MMM yyyy", Locale.US).format(selectedCalendar.time)
            }
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString()
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, getString(R.string.error_valid_amount), Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedCategory == null) {
            Toast.makeText(this, getString(R.string.error_select_category), Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = editTransaction?.id ?: 0,
            title = selectedCategory!!.name,
            amount = amount,
            type = selectedType,
            category = selectedCategory!!.name,
            date = selectedCalendar.timeInMillis,
            accountId = PrefManager.currentAccountId,
            notes = binding.etNotes.text.toString()
        )

        if (editTransaction != null) {
            viewModel.update(transaction)
            Toast.makeText(this, getString(R.string.toast_transaction_updated), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insert(transaction)
            Toast.makeText(this, getString(R.string.toast_transaction_saved), Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun showDeleteConfirmation() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogBottomSheetDeleteBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        cashvexa.expensetracker.budget.money.utility.helper.EdgeToEdgeUtil.applyBottomSheetInsets(dialogBinding.root)

        dialogBinding.txtDeleteTitle.text = getString(R.string.delete_transaction_title)
        dialogBinding.txtDeleteMessage.text = getString(R.string.delete_single_transaction_desc)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirmDelete.setOnClickListener {
            dialog.dismiss()
            editTransaction?.let { viewModel.delete(it) }
            Toast.makeText(this, getString(R.string.toast_transaction_deleted), Toast.LENGTH_SHORT).show()
            finish()
        }

        dialog.show()
    }
}

package com.tb.example.expensebook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tb.example.expensebook.data.local.AccountDao
import com.tb.example.expensebook.data.model.Account
import com.tb.example.expensebook.data.model.Category
import com.tb.example.expensebook.data.model.CategorySum
import com.tb.example.expensebook.data.model.Transaction
import com.tb.example.expensebook.data.repo.CategoryRepository
import com.tb.example.expensebook.data.repo.TransactionRepository
import com.tb.example.expensebook.utility.Constants
import com.tb.example.expensebook.utility.helper.PrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountDao: AccountDao,
    application: Application
) : AndroidViewModel(application) {

    val currentAccountId: StateFlow<Int> = PrefManager.currentAccountIdFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, PrefManager.currentAccountId)

    val selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val reportCalendar   = MutableStateFlow(Calendar.getInstance())
    val reportPeriodType = MutableStateFlow(Constants.PERIOD_MONTHLY)
    val searchQuery      = MutableStateFlow("")
    val filterType       = MutableStateFlow(Constants.FILTER_ALL)
    val filterCategory   = MutableStateFlow(Constants.FILTER_ALL)

    val transactionsForSelectedMonth: StateFlow<List<Transaction>> = combine(selectedCalendar, currentAccountId) { cal, accId ->
        cal to accId
    }.flatMapLatest { (cal, accId) ->
        repository.getTransactionsForRange(accId, startOfMonth(cal), endOfMonth(cal))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = transactionsForSelectedMonth.map { list ->
        list.filter { it.type.trim().equals(Constants.TYPE_INCOME, ignoreCase = true) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactionsForSelectedMonth.map { list ->
        list.filter { it.type.trim().equals(Constants.TYPE_EXPENSE, ignoreCase = true) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = transactionsForSelectedMonth.map { list ->
        val income = list.filter { it.type.trim().equals(Constants.TYPE_INCOME, ignoreCase = true) }.sumOf { it.amount }
        val expense = list.filter { it.type.trim().equals(Constants.TYPE_EXPENSE, ignoreCase = true) }.sumOf { it.amount }
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val filteredTransactions: StateFlow<List<Transaction>> = combine(searchQuery, filterType, filterCategory, currentAccountId) { q, t, c, id ->
        FilterState(q, t, c, id)
    }.flatMapLatest { state ->
        repository.searchWithFilters(state.accId, state.query, state.type, state.category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsForReport: StateFlow<List<Transaction>> = combine(reportPeriodType, reportCalendar, currentAccountId) { period, cal, accId ->
        ReportState(period, cal, accId)
    }.flatMapLatest { state ->
        val (start, end) = getRangeForReport(state.period, state.cal)
        repository.getTransactionsForRange(state.accId, start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportCategoryBreakdown: StateFlow<List<CategorySum>> = transactionsForReport.map { list ->
        list.groupBy { it.category }
            .map { (cat, trans) -> CategorySum(cat, trans.sumOf { it.amount }) }
            .sortedByDescending { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportTotalIncome: StateFlow<Double> = transactionsForReport.map { list ->
        list.filter { it.type.trim().equals(Constants.TYPE_INCOME, ignoreCase = true) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val reportTotalExpense: StateFlow<Double> = transactionsForReport.map { list ->
        list.filter { it.type.trim().equals(Constants.TYPE_EXPENSE, ignoreCase = true) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val selectedCategoryType = MutableStateFlow(Constants.TYPE_EXPENSE)
    val categories: StateFlow<List<Category>> = selectedCategoryType.flatMapLatest { type ->
        categoryRepository.getCategoriesByType(type)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkAndInsertDefaultCategories()
    }

    private fun checkAndInsertDefaultCategories() = viewModelScope.launch {
        if (categoryRepository.getCategoryCount() == 0) {
            val defaults = mutableListOf<Category>()
            val expenseCats = listOf(
                "Home Emi" to "ic_wallet", "Shopping" to "ic_shopping", "Food" to "ic_food", "Phone" to "ic_round_rupay",
                "Entertainment" to "ic_entertainment", "Education" to "ic_education", "Beauty" to "ic_person", "Sports" to "ic_others",
                "Social" to "ic_person", "Transportation" to "ic_transport", "Clothing" to "ic_shopping", "Car" to "ic_transport",
                "Alcohol" to "ic_others", "Cigarettes" to "ic_others", "Electronics" to "ic_others", "Travel" to "ic_travel",
                "Health" to "ic_health", "Pets" to "ic_others", "Repairs" to "ic_settings", "Housing" to "ic_others",
                "Home" to "ic_others", "Gifts" to "ic_others", "Donations" to "ic_others", "Lottery" to "ic_others",
                "Snacks" to "ic_food", "Kids" to "ic_person", "Vegetables" to "ic_food", "Fruits" to "ic_food"
            )
            expenseCats.forEach { (name, icon) ->
                defaults.add(Category(name = name, iconName = icon, type = Constants.TYPE_EXPENSE, isDefault = true))
            }
            val incomeCats = listOf(
                "old money" to "ic_round_rupay", "Salary" to "ic_salary", "Investments" to "ic_investment", "Part-Time" to "ic_freelance",
                "Bonus" to "ic_others", "Others" to "ic_others"
            )
            incomeCats.forEach { (name, icon) ->
                defaults.add(Category(name = name, iconName = icon, type = Constants.TYPE_INCOME, isDefault = true))
            }
            val transferCats = listOf("Bank Transfer" to "ic_account", "Wallet" to "ic_wallet", "Others" to "ic_others")
            transferCats.forEach { (name, icon) ->
                defaults.add(Category(name = name, iconName = icon, type = Constants.TYPE_TRANSFER, isDefault = true))
            }
            defaults.forEach { categoryRepository.insert(it) }
        }
    }

    fun setCategoryType(type: String) { selectedCategoryType.value = type }
    fun addCategory(category: Category) = viewModelScope.launch { categoryRepository.insert(category) }
    fun updateCategory(category: Category) = viewModelScope.launch { categoryRepository.update(category) }
    fun deleteCategory(category: Category) = viewModelScope.launch { categoryRepository.delete(category) }
    fun insert(transaction: Transaction) = viewModelScope.launch { repository.insert(transaction.copy(accountId = currentAccountId.value)) }
    fun update(transaction: Transaction) = viewModelScope.launch { repository.update(transaction) }
    fun delete(transaction: Transaction) = viewModelScope.launch { repository.delete(transaction) }
    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }

    suspend fun getAccountDetails(): Account? {
        val accId = currentAccountId.value
        if (accId == -1) return null
        return withContext(Dispatchers.IO) {
            accountDao.getAccountById(accId)
        }
    }

    fun nextMonth() {
        selectedCalendar.value = (selectedCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
    }

    fun previousMonth() {
        selectedCalendar.value = (selectedCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
    }

    suspend fun getTransactionsForExport(startTime: Long, endTime: Long): List<Transaction> {
        val accId = currentAccountId.value
        if (accId == -1) return emptyList()
        return withContext(Dispatchers.IO) {
            repository.getTransactionsForRange(accId, startTime, endTime).first()
        }
    }

    private fun startOfMonth(cal: Calendar): Long = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.toStartOfDay().timeInMillis
    private fun endOfMonth(cal: Calendar): Long = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) }.toEndOfDay().timeInMillis

    private fun getRangeForReport(period: String, cal: Calendar): Pair<Long, Long> {
        val clone = cal.clone() as Calendar
        return when (period) {
            Constants.PERIOD_DAILY -> clone.toStartOfDay().timeInMillis to clone.toEndOfDay().timeInMillis
            Constants.PERIOD_WEEKLY -> {
                clone.firstDayOfWeek = Calendar.MONDAY
                clone.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = clone.toStartOfDay().timeInMillis
                clone.add(Calendar.DAY_OF_YEAR, 6)
                start to clone.toEndOfDay().timeInMillis
            }
            Constants.PERIOD_YEARLY -> {
                clone.set(Calendar.DAY_OF_YEAR, 1)
                val start = clone.toStartOfDay().timeInMillis
                clone.set(Calendar.MONTH, Calendar.DECEMBER)
                clone.set(Calendar.DAY_OF_MONTH, 31)
                start to clone.toEndOfDay().timeInMillis
            }
            else -> startOfMonth(cal) to endOfMonth(cal)
        }
    }

    private fun Calendar.toStartOfDay(): Calendar = (clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private fun Calendar.toEndOfDay(): Calendar = (clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }

    fun setMonthYear(month: Int, year: Int) { selectedCalendar.value = Calendar.getInstance().apply { set(Calendar.YEAR, year); set(Calendar.MONTH, month) } }
    fun setReportPeriodType(type: String) { reportPeriodType.value = type; reportCalendar.value = Calendar.getInstance() }
    fun nextReportPeriod() { reportCalendar.value = (reportCalendar.value.clone() as Calendar).apply { 
        when (reportPeriodType.value) { Constants.PERIOD_DAILY -> add(Calendar.DAY_OF_YEAR, 1); Constants.PERIOD_WEEKLY -> add(Calendar.WEEK_OF_YEAR, 1); Constants.PERIOD_YEARLY -> add(Calendar.YEAR, 1); else -> add(Calendar.MONTH, 1) } 
    } }
    fun previousReportPeriod() { reportCalendar.value = (reportCalendar.value.clone() as Calendar).apply { 
        when (reportPeriodType.value) { Constants.PERIOD_DAILY -> add(Calendar.DAY_OF_YEAR, -1); Constants.PERIOD_WEEKLY -> add(Calendar.WEEK_OF_YEAR, -1); Constants.PERIOD_YEARLY -> add(Calendar.YEAR, -1); else -> add(Calendar.MONTH, -1) } 
    } }
    fun setSearchQuery(query: String)  { searchQuery.value = query }
    fun setFilterType(type: String)    { filterType.value = type }
    fun clearFilters() { searchQuery.value = ""; filterType.value = Constants.FILTER_ALL; filterCategory.value = Constants.FILTER_ALL }

    private data class FilterState(val query: String, val type: String, val category: String, val accId: Int)
    private data class ReportState(val period: String, val cal: Calendar, val accId: Int)
}

package cashvexa.expensetracker.budget.money.di

import cashvexa.expensetracker.budget.money.data.local.AppDatabase
import cashvexa.expensetracker.budget.money.data.repo.CategoryRepository
import cashvexa.expensetracker.budget.money.data.repo.TransactionRepository
import cashvexa.expensetracker.budget.money.ui.viewmodel.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(androidApplication()) }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().accountDao() }
    single { get<AppDatabase>().categoryDao() }
    single { TransactionRepository(get()) }
    single { CategoryRepository(get()) }
    viewModel { TransactionViewModel(get(), get(), get(), androidApplication()) }
}

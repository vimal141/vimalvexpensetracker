package com.fintrack.expensetracker.di

import com.fintrack.expensetracker.data.local.AppDatabase
import com.fintrack.expensetracker.data.repo.CategoryRepository
import com.fintrack.expensetracker.data.repo.TransactionRepository
import com.fintrack.expensetracker.ui.viewmodel.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.Companion.getDatabase(androidApplication()) }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().accountDao() }
    single { get<AppDatabase>().categoryDao() }
    
    single { TransactionRepository(get()) }
    single { CategoryRepository(get()) }
    
    viewModel { TransactionViewModel(get(), get(), get(), androidApplication()) }
}

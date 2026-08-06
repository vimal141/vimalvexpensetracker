package com.tb.example.expensebook.di

import com.tb.example.expensebook.data.local.AppDatabase
import com.tb.example.expensebook.data.repo.CategoryRepository
import com.tb.example.expensebook.data.repo.TransactionRepository
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
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

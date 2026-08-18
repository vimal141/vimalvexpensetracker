package com.fintrack.expensetracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.fintrack.expensetracker.di.appModule
import com.fintrack.expensetracker.utility.helper.PrefManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PrefManager.init(this)

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }

        val mode = PrefManager.themeMode
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
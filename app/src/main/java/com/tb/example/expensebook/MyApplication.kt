package com.tb.example.expensebook

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.tb.example.expensebook.di.appModule
import com.tb.example.expensebook.utility.helper.PrefManager
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
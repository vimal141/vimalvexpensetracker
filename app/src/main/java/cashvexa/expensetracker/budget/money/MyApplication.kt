package cashvexa.expensetracker.budget.money

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import cashvexa.expensetracker.budget.money.di.appModule
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
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
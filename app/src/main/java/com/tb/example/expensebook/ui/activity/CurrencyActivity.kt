package com.tb.example.expensebook.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tb.example.expensebook.R
import com.tb.example.expensebook.databinding.ActivityCurrencyBinding
import com.tb.example.expensebook.ui.adapter.CurrencyAdapter
import com.tb.example.expensebook.ui.adapter.CurrencyItem
import com.tb.example.expensebook.utility.helper.PrefManager

class CurrencyActivity : BaseActivity<ActivityCurrencyBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityCurrencyBinding {
        return ActivityCurrencyBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupUI()
        setupListeners()

        com.tb.example.expensebook.utility.ads.AdManager.loadNativeAd(
            activity = this,
            container = binding.flAdContainer,
            layoutResId = R.layout.layout_native_ad_small,
            screenFlagKey = com.tb.example.expensebook.utility.ads.RemoteConfigHelper.KEY_SHOW_CURRENCY_SCREEN_NATIVE_AD
        )
    }

    override fun onBackPressedAction() {
        val isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        if (isFirstLaunch) {
            PrefManager.isCurrencySet = true
            val intent = android.content.Intent(this, AddAccountActivity::class.java).apply {
                putExtra("isFirstLaunch", true)
            }
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    private fun setupUI() {
        val isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        binding.btnBack.visibility = if (isFirstLaunch) View.GONE else View.VISIBLE

        val symbols = arrayOf("₹", "$", "€", "£", "¥", "A$", "C$", "₣", "₩")
        val codes = arrayOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "KRW")
        val names = arrayOf("Indian Rupee", "US Dollar", "Euro", "British Pound", "Japanese Yen", "Australian Dollar", "Canadian Dollar", "Swiss Franc", "South Korean Won")

        val currencies = (0 until symbols.size).map { i ->
            CurrencyItem(symbols[i], codes[i], names[i])
        }
        val currentSelectedCode = PrefManager.currency

        val adapter = CurrencyAdapter(currencies, currentSelectedCode)
        binding.rvCurrency.adapter = adapter

        binding.btnDone.setOnClickListener {
            val selectedCurrency = currencies.find { it.code == adapter.selectedCode }
            if (selectedCurrency != null) {
                PrefManager.currencySymbol = selectedCurrency.symbol
                PrefManager.currency = selectedCurrency.code

                if (isFirstLaunch) {
                    PrefManager.isCurrencySet = true
                    val intent = android.content.Intent(this, AddAccountActivity::class.java).apply {
                        putExtra("isFirstLaunch", true)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.toast_currency_updated), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

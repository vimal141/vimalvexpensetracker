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
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupUI() {
        val symbols = arrayOf("₹", "$", "€", "£", "¥", "A$", "C$", "₣", "₩")
        val codes = arrayOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "KRW")
        val names = arrayOf("Indian Rupee", "US Dollar", "Euro", "British Pound", "Japanese Yen", "Australian Dollar", "Canadian Dollar", "Swiss Franc", "South Korean Won")

        val currencies = (0 until symbols.size).map { i ->
            CurrencyItem(symbols[i], codes[i], names[i])
        }
        val currentSelectedCode = PrefManager.currency

        val adapter = CurrencyAdapter(currencies, currentSelectedCode)
        binding.rvCurrency.adapter = adapter

        findViewById<
                View>(R.id.btn_done).setOnClickListener {
            val selectedCurrency = currencies.find { it.code == adapter.selectedCode }
            if (selectedCurrency != null) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.change_currency))
                    .setMessage(getString(R.string.change_currency_desc, selectedCurrency.name, selectedCurrency.symbol))
                    .setPositiveButton(getString(R.string.done)) { _, _ ->
                        PrefManager.currencySymbol = selectedCurrency.symbol
                        PrefManager.currency = selectedCurrency.code
                        
                        Toast.makeText(this, getString(R.string.toast_currency_updated), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

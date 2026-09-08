package cashvexa.expensetracker.budget.money.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ActivityCurrencyBinding
import cashvexa.expensetracker.budget.money.ui.adapter.CurrencyAdapter
import cashvexa.expensetracker.budget.money.ui.adapter.CurrencyItem
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import java.util.Locale
import androidx.core.view.isVisible

class CurrencyActivity : BaseActivity<ActivityCurrencyBinding>() {

    private var isFirstLaunch: Boolean = false
    private var currenciesList = listOf<CurrencyItem>()

    override fun inflateBinding(inflater: LayoutInflater): ActivityCurrencyBinding {
        return ActivityCurrencyBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupUI()
        setupListeners()
    }

    override fun onBackPressedAction() {
        if (isFirstLaunch) {
            val deviceCurrency = getDeviceCurrencyCode()
            val matchedItem = currenciesList.find { it.code.equals(deviceCurrency, ignoreCase = true) } ?: currenciesList.first()
            PrefManager.currencySymbol = matchedItem.symbol
            PrefManager.currency = matchedItem.code
            PrefManager.isCurrencySet = true
            
            val intent = Intent(this, AddAccountActivity::class.java).apply {
                putExtra("isFirstLaunch", true)
            }
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    private fun getDeviceCurrencyCode(): String {
        val country = Locale.getDefault().country.uppercase()
        return when (country) {
            "IN" -> "INR"
            "BR" -> "BRL"
            "SA", "AE", "EG" -> "SAR"
            "RU" -> "RUB"
            "TR" -> "TRY"
            "VN" -> "VND"
            "ID" -> "IDR"
            "TH" -> "THB"
            "PL" -> "PLN"
            "TW" -> "TWD"
            "CN" -> "CNY"
            "RO" -> "RON"
            "ZA" -> "ZAR"
            "HU" -> "HUF"
            "UA" -> "UAH"
            "PH" -> "PHP"
            "US" -> "USD"
            "GB" -> "GBP"
            "JP" -> "JPY"
            "KR" -> "KRW"
            "AU" -> "AUD"
            "CA" -> "CAD"
            "CH" -> "CHF"
            "DE", "FR", "ES", "IT", "PT", "NL", "BE", "AT", "FI", "GR", "IE" -> "EUR"
            else -> "USD"
        }
    }

    private fun setupUI() {
        isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        binding.btnBack.visibility = if (isFirstLaunch) View.GONE else View.VISIBLE

        val symbols = arrayOf("₹", "$", "€", "£", "¥", "A$", "C$", "₣", "₩", "R$", "ر.س", "₽", "₺", "₫", "Rp", "฿", "zł", "NT$", "¥", "lei", "R", "Ft", "₴", "₱")
        val codes = arrayOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "KRW", "BRL", "SAR", "RUB", "TRY", "VND", "IDR", "THB", "PLN", "TWD", "CNY", "RON", "ZAR", "HUF", "UAH", "PHP")
        val names = arrayOf(
            "Indian Rupee", "US Dollar", "Euro", "British Pound", "Japanese Yen", "Australian Dollar", "Canadian Dollar", "Swiss Franc", "South Korean Won",
            "Brazilian Real", "Saudi Riyal", "Russian Ruble", "Turkish Lira", "Vietnamese Dong", "Indonesian Rupiah", "Thai Baht", "Polish Zloty",
            "New Taiwan Dollar", "Chinese Yuan", "Romanian Leu", "South African Rand", "Hungarian Forint", "Ukrainian Hryvnia", "Philippine Peso"
        )

        val rawCurrencies = (0 until symbols.size).map { i ->
            CurrencyItem(symbols[i], codes[i], names[i])
        }

        val deviceCurrency = getDeviceCurrencyCode()
        val mutableCurrencies = rawCurrencies.toMutableList()
        val matchIndex = mutableCurrencies.indexOfFirst { it.code.equals(deviceCurrency, ignoreCase = true) }
        if (matchIndex > 0) {
            val deviceItem = mutableCurrencies.removeAt(matchIndex)
            mutableCurrencies.add(0, deviceItem)
        }
        currenciesList = mutableCurrencies

        val currentSelectedCode = if (isFirstLaunch && !PrefManager.isCurrencySet) "" else PrefManager.currency

        if (isFirstLaunch) {
            binding.btnDone.setText(R.string.next)
            if (!PrefManager.isCurrencySet) {
                binding.btnDone.visibility = View.GONE
            } else {
                binding.btnDone.visibility = View.VISIBLE
                startButtonAnimation()
            }
        } else {
            binding.btnDone.setText(R.string.done)
            binding.btnDone.visibility = View.VISIBLE
            stopButtonAnimation()
        }

        val shouldShowHint = isFirstLaunch && !PrefManager.isCurrencySet
        val adapter = CurrencyAdapter(currenciesList, currentSelectedCode, shouldShowHint) { _ ->
            binding.btnDone.visibility = View.VISIBLE
            if (isFirstLaunch) {
                startButtonAnimation()
            }
        }
        binding.rvCurrency.adapter = adapter

        binding.btnDone.setOnClickListener {
            val selectedCode = adapter.selectedCode.ifEmpty { currenciesList.first().code }
            val selectedCurrency = currenciesList.find { it.code == selectedCode }
            if (selectedCurrency != null) {
                PrefManager.currencySymbol = selectedCurrency.symbol
                PrefManager.currency = selectedCurrency.code

                if (isFirstLaunch) {
                    PrefManager.isCurrencySet = true
                    val intent = Intent(this, AddAccountActivity::class.java).apply {
                        putExtra("isFirstLaunch", true)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    finish()
                }
            }
        }
    }

    private fun startButtonAnimation() {
        if (isFirstLaunch && binding.btnDone.isVisible) {
            if (binding.btnDone.animation == null) {
                val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.anim_btn_pulse)
                binding.btnDone.startAnimation(pulseAnim)
            }
        }
    }

    private fun stopButtonAnimation() {
        binding.btnDone.clearAnimation()
    }

    override fun onDestroy() {
        stopButtonAnimation()
        super.onDestroy()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

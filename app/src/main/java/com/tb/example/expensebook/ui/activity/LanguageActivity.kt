package com.tb.example.expensebook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.model.LanguageModel
import com.tb.example.expensebook.databinding.ActivityLanguageBinding
import com.tb.example.expensebook.ui.adapter.LanguageAdapter
import com.tb.example.expensebook.utility.helper.LanguageHelper
import com.tb.example.expensebook.utility.helper.PrefManager

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {

    private lateinit var adapter: LanguageAdapter
    private var isFirstLaunch: Boolean = false
    private var languages = listOf<LanguageModel>()

    override fun inflateBinding(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupUI()
        setupListeners()

        com.tb.example.expensebook.utility.ads.AdManager.loadNativeAd(
            activity = this,
            container = binding.flAdContainer,
            layoutResId = R.layout.layout_native_ad_card,
            screenFlagKey = com.tb.example.expensebook.utility.ads.RemoteConfigHelper.KEY_SHOW_LANGUAGE_SCREEN_NATIVE_AD
        )
    }

    override fun onBackPressedAction() {
        if (isFirstLaunch) {
            val selectedCode = adapter.selectedCode.ifEmpty { languages.first().code }
            PrefManager.languageCode = selectedCode
            PrefManager.isLanguageSet = true
            LanguageHelper.applyAppLanguage(this, selectedCode)

            val intent = Intent(this, OnBoardingActivity::class.java).apply {
                putExtra("isFirstLaunch", true)
            }
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    private fun setupUI() {
        isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        binding.btnBack.visibility = if (isFirstLaunch) View.GONE else View.VISIBLE

        val currentSelectedCode = if (isFirstLaunch && !PrefManager.isLanguageSet) "" else PrefManager.languageCode
        languages = LanguageHelper.getLanguageList(currentSelectedCode)

        if (isFirstLaunch && !PrefManager.isLanguageSet) {
            binding.btnDone.visibility = View.GONE
        } else {
            binding.btnDone.visibility = View.VISIBLE
        }

        adapter = LanguageAdapter(languages, currentSelectedCode) { _ ->
            binding.btnDone.visibility = View.VISIBLE
        }
        binding.rvLanguage.adapter = adapter

        binding.btnDone.setOnClickListener {
            val selectedCode = adapter.selectedCode.ifEmpty { languages.first().code }
            val selectedItem = languages.find { it.code.equals(selectedCode, ignoreCase = true) } ?: languages.first()

            PrefManager.languageCode = selectedItem.code
            LanguageHelper.applyAppLanguage(this, selectedItem.code)

            if (isFirstLaunch) {
                PrefManager.isLanguageSet = true
                val intent = Intent(this, OnBoardingActivity::class.java).apply {
                    putExtra("isFirstLaunch", true)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.toast_language_updated), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

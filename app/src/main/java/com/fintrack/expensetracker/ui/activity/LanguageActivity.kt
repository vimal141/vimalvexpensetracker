package com.fintrack.expensetracker.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.fintrack.expensetracker.R
import com.fintrack.expensetracker.data.model.LanguageModel
import com.fintrack.expensetracker.databinding.ActivityLanguageBinding
import com.fintrack.expensetracker.ui.adapter.LanguageAdapter
import com.fintrack.expensetracker.utility.helper.LanguageHelper
import com.fintrack.expensetracker.utility.helper.PrefManager

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
    }

    override fun onBackPressedAction() {
        if (isFirstLaunch) {
            val deviceLanguageCode = LanguageHelper.getDeviceLanguageCode()
            PrefManager.languageCode = deviceLanguageCode
            PrefManager.isLanguageSet = true
            LanguageHelper.applyAppLanguage(this, deviceLanguageCode)

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

        if (isFirstLaunch) {
            binding.btnDone.setText(R.string.next)
            if (!PrefManager.isLanguageSet) {
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

        val shouldShowHint = isFirstLaunch && !PrefManager.isLanguageSet
        adapter = LanguageAdapter(languages, currentSelectedCode, shouldShowHint) { _ ->
            binding.btnDone.visibility = View.VISIBLE
            if (isFirstLaunch) {
                startButtonAnimation()
            }
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

    private fun startButtonAnimation() {
        if (isFirstLaunch && binding.btnDone.visibility == View.VISIBLE) {
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

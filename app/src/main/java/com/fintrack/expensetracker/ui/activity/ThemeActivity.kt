package com.fintrack.expensetracker.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import com.fintrack.expensetracker.utility.helper.PrefManager
import com.fintrack.expensetracker.databinding.ActivityThemeBinding

class ThemeActivity : BaseActivity<ActivityThemeBinding>() {

    private var selectedThemeMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    override fun inflateBinding(inflater: LayoutInflater): ActivityThemeBinding {
        return ActivityThemeBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        selectedThemeMode = PrefManager.themeMode
        setupUI()
        setupListeners()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupUI() {
        updateRadioSelection(selectedThemeMode)
    }

    private fun updateRadioSelection(mode: Int) {
        selectedThemeMode = mode
        binding.radioLight.isChecked = (mode == AppCompatDelegate.MODE_NIGHT_NO)
        binding.radioDark.isChecked = (mode == AppCompatDelegate.MODE_NIGHT_YES)
        binding.radioSystem.isChecked = (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.layoutLight.setOnClickListener {
            updateRadioSelection(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.layoutDark.setOnClickListener {
            updateRadioSelection(AppCompatDelegate.MODE_NIGHT_YES)
        }

        binding.layoutSystem.setOnClickListener {
            updateRadioSelection(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding.btnDone.setOnClickListener {
            if (PrefManager.themeMode != selectedThemeMode) {
                PrefManager.themeMode = selectedThemeMode
                AppCompatDelegate.setDefaultNightMode(selectedThemeMode)
            }
            finish()
        }
    }
}

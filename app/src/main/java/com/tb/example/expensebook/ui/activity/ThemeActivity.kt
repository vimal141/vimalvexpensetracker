package com.tb.example.expensebook.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.databinding.ActivityThemeBinding

class ThemeActivity : BaseActivity<ActivityThemeBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityThemeBinding {
        return ActivityThemeBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupUI()
        setupListeners()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupUI() {
        val currentMode = PrefManager.themeMode
        binding.icCheckLight.visibility = if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) View.VISIBLE else View.GONE
        binding.icCheckDark.visibility = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) View.VISIBLE else View.GONE
        binding.icCheckSystem.visibility = if (currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.layoutLight.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.layoutDark.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        binding.layoutSystem.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setThemeMode(mode: Int) {
        if (PrefManager.themeMode != mode) {
            PrefManager.themeMode = mode
            AppCompatDelegate.setDefaultNightMode(mode)
            setupUI()
        }
    }
}

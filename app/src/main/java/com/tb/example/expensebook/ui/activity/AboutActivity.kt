package com.tb.example.expensebook.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.tb.example.expensebook.R
import com.tb.example.expensebook.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupUI()
        setupListeners()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupUI() {
        binding.txtAppVersion.text = getString(R.string.version)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCheckUpdate.setOnClickListener {
            Toast.makeText(this, "You are using the latest version (v1.0.0)", Toast.LENGTH_SHORT).show()
        }
    }
}

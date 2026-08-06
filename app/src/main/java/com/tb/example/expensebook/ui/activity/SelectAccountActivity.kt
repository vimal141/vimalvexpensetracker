package com.tb.example.expensebook.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tb.example.expensebook.data.local.AccountDao
import com.tb.example.expensebook.data.model.Account
import com.tb.example.expensebook.databinding.ActivitySelectAccountBinding
import com.tb.example.expensebook.ui.adapter.DialogAccountAdapter
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SelectAccountActivity : BaseActivity<ActivitySelectAccountBinding>() {

    private lateinit var adapter: DialogAccountAdapter
    private var accountsList: List<Account> = emptyList()
    private val accountDao: AccountDao by inject()

    override fun inflateBinding(inflater: LayoutInflater): ActivitySelectAccountBinding {
        return ActivitySelectAccountBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupListeners()
        loadAccounts()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun loadAccounts() {
        lifecycleScope.launch {
            accountsList = accountDao.getAllAccounts().first()
            
            val currentAccountId = PrefManager.currentAccountId
            adapter = DialogAccountAdapter(currentAccountId)
            adapter.submitList(accountsList)
            
            binding.rvAccounts.layoutManager = LinearLayoutManager(this@SelectAccountActivity)
            binding.rvAccounts.adapter = adapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDone.setOnClickListener {
            if (!::adapter.isInitialized) return@setOnClickListener
            
            val selectedAccount = accountsList.find { it.id == adapter.currentAccountId }
            if (selectedAccount != null) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.change_account))
                    .setMessage(getString(R.string.change_account_desc, selectedAccount.name))
                    .setPositiveButton(getString(R.string.done)) { _, _ ->
                        PrefManager.currentAccountId = selectedAccount.id
                        Toast.makeText(this, getString(R.string.toast_account_changed, selectedAccount.name), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        }
    }
}

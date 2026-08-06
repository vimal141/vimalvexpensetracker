package com.tb.example.expensebook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tb.example.expensebook.data.local.AccountDao
import com.tb.example.expensebook.databinding.ActivityAccountsBinding
import com.tb.example.expensebook.ui.adapter.AccountAdapter
import com.tb.example.expensebook.utility.helper.PrefManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AccountsActivity : BaseActivity<ActivityAccountsBinding>() {

    private val accountDao: AccountDao by inject()
    private lateinit var adapter: AccountAdapter

    override fun inflateBinding(inflater: LayoutInflater): ActivityAccountsBinding {
        return ActivityAccountsBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener { finish() }

        binding.rvAccounts.layoutManager = LinearLayoutManager(this)

        binding.fabAddAccount.setOnClickListener {
            startActivity(Intent(this, AddAccountActivity::class.java))
        }

        loadAccounts()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun loadAccounts() {
        lifecycleScope.launch {
            accountDao.getAllAccounts().collectLatest { accounts ->
                val currentAccountId = PrefManager.currentAccountId
                adapter = AccountAdapter(currentAccountId) { selectedAccount ->
                    PrefManager.currentAccountId = selectedAccount.id
                    val intent = Intent(this@AccountsActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                adapter.submitList(accounts)
                binding.rvAccounts.adapter = adapter
            }
        }
    }
}

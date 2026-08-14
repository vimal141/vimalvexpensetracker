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
    private var selectedAccountId: Int = -1

    override fun inflateBinding(inflater: LayoutInflater): ActivityAccountsBinding {
        return ActivityAccountsBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        selectedAccountId = PrefManager.currentAccountId

        binding.btnBack.setOnClickListener { finish() }

        binding.btnDone.setOnClickListener {
            if (selectedAccountId != -1) {
                PrefManager.currentAccountId = selectedAccountId
            }
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("target_tab", 3)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

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
                if (selectedAccountId == -1 && accounts.isNotEmpty()) {
                    selectedAccountId = PrefManager.currentAccountId.takeIf { id -> accounts.any { it.id == id } }
                        ?: accounts.first().id
                }
                adapter = AccountAdapter(selectedAccountId) { selectedAccount ->
                    selectedAccountId = selectedAccount.id
                }
                adapter.submitList(accounts)
                binding.rvAccounts.adapter = adapter
            }
        }
    }
}

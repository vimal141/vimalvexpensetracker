package cashvexa.expensetracker.budget.money.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cashvexa.expensetracker.budget.money.data.local.AccountDao
import cashvexa.expensetracker.budget.money.ui.adapter.AccountAdapter
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import cashvexa.expensetracker.budget.money.databinding.ActivityAccountsBinding
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

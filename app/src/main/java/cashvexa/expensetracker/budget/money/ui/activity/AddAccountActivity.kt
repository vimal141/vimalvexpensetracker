package cashvexa.expensetracker.budget.money.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.data.local.AccountDao
import cashvexa.expensetracker.budget.money.data.model.Account
import cashvexa.expensetracker.budget.money.databinding.ActivityAddAccountBinding
import cashvexa.expensetracker.budget.money.ui.fragment.ExitAppBottomSheet
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import kotlin.random.Random

class AddAccountActivity : BaseActivity<ActivityAddAccountBinding>() {

    override val isApplyKeyboardPadding = true
    private val accountDao: AccountDao by inject()

    override fun inflateBinding(inflater: LayoutInflater): ActivityAddAccountBinding {
        return ActivityAddAccountBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {

        val showBack = PrefManager.currentAccountId != -1
        binding.btnBack.visibility = if (showBack) View.VISIBLE else View.GONE

        binding.btnBack.setOnClickListener {
            if (PrefManager.currentAccountId != -1) {
                finish()
            } else {
                ExitAppBottomSheet {
                    finishAffinity()
                }.show(supportFragmentManager, "ExitAppBottomSheet")
            }
        }

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.etAccountName.text.toString().trim()

            if (name.isEmpty()) {
                binding.layoutAccountName.error = ContextCompat.getString(this@AddAccountActivity,R.string.strEmptyError)
                return@setOnClickListener
            }

            binding.layoutAccountName.error = null
            binding.btnCreateAccount.isEnabled = false

            lifecycleScope.launch {
                val existingAccount = withContext(Dispatchers.IO) {
                    accountDao.getAccountByName(name)
                }

                if (existingAccount != null) {
                    binding.layoutAccountName.error = ContextCompat.getString(this@AddAccountActivity,R.string.strAccountExist)
                    binding.btnCreateAccount.isEnabled = true
                } else {

                    val part1 = Random.nextInt(1000, 9999)
                    val part2 = Random.nextInt(1000, 9999)
                    val part3 = Random.nextInt(1000, 9999)
                    val part4 = Random.nextInt(1000, 9999)
                    val accountNumber = "$part1$part2$part3$part4"

                    val newAccount = Account(name = name, accountNumber = accountNumber)

                    val insertedId = withContext(Dispatchers.IO) {
                        accountDao.insertAccount(newAccount)
                    }

                    PrefManager.currentAccountId = insertedId.toInt()
                    PrefManager.isOnboardingDone = true

                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etAccountName.windowToken, 0)
                    binding.etAccountName.clearFocus()

                    val intent = Intent(this@AddAccountActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.etAccountName.post {
            binding.etAccountName.requestFocus()
        }

        binding.etAccountName.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.etAccountName.clearFocus()
                true
            } else {
                false
            }
        }

        binding.etAccountName.doOnTextChanged { text, _, _, _ ->
            if (!text.isNullOrBlank()) {
                binding.layoutAccountName.error = null
                binding.layoutAccountName.isErrorEnabled = false
            }
        }

    }

    override fun onBackPressedAction() {
        binding.btnBack.performClick()
    }
}

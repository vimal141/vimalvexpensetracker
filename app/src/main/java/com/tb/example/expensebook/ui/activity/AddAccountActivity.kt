package com.tb.example.expensebook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.tb.example.expensebook.R
import com.tb.example.expensebook.data.local.AccountDao
import com.tb.example.expensebook.data.model.Account
import com.tb.example.expensebook.databinding.ActivityAddAccountBinding
import com.tb.example.expensebook.utility.helper.PrefManager
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

       binding.btnBack.visibility = if (PrefManager.currentAccountId == -1) View.GONE else View.VISIBLE

        binding.btnBack.setOnClickListener {
            if (PrefManager.currentAccountId != -1) {
                finish()
            } else {
                Toast.makeText(this@AddAccountActivity, getString(R.string.toast_account_first), Toast.LENGTH_SHORT).show()
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

                    Toast.makeText(this@AddAccountActivity, getString(R.string.toast_account_created), Toast.LENGTH_SHORT).show()

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

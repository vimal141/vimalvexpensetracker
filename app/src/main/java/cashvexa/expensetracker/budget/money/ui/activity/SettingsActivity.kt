package cashvexa.expensetracker.budget.money.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ActivitySettingsBinding
import cashvexa.expensetracker.budget.money.ui.fragment.ClearDataBottomSheet
import cashvexa.expensetracker.budget.money.ui.fragment.ExportFilterBottomSheet
import cashvexa.expensetracker.budget.money.ui.viewmodel.TransactionViewModel
import cashvexa.expensetracker.budget.money.utility.Constants
import cashvexa.expensetracker.budget.money.utility.helper.LanguageHelper
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private val viewModel: TransactionViewModel by viewModel()

    override fun inflateBinding(inflater: LayoutInflater): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupUI() {
        val themeMode = PrefManager.themeMode
        val themeText = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.light)
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.dark)
            else -> getString(R.string.system_default)
        }
        binding.txtThemeValue.text = themeText
        binding.txtCurrencyValue.text = "${PrefManager.currencySymbol} ${PrefManager.currency}"

        val selectedLang = LanguageHelper.getLanguageList(PrefManager.languageCode)
            .find { it.code.equals(PrefManager.languageCode, ignoreCase = true) }
        binding.txtLanguageValue.text = selectedLang?.nativeName ?: "English"

        val versionName = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
        binding.txtAppVersion.text = getString(R.string.version, versionName)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.layoutTheme.setOnClickListener {
            startActivity(Intent(this, ThemeActivity::class.java))
        }

        binding.layoutManageAccounts.setOnClickListener {
            startActivity(Intent(this, AccountsActivity::class.java))
        }

        binding.layoutCurrency.setOnClickListener {
            startActivity(Intent(this, CurrencyActivity::class.java))
        }

        binding.layoutLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }

        binding.layoutExport.setOnClickListener {
            ExportFilterBottomSheet().show(supportFragmentManager, "ExportFilterBottomSheet")
        }

        binding.layoutClearData.setOnClickListener {
            ClearDataBottomSheet {
                viewModel.deleteAll()
                Toast.makeText(this, getString(R.string.toast_all_data_cleared), Toast.LENGTH_SHORT).show()
            }.show(supportFragmentManager, "ClearDataBottomSheet")
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.layoutPrivacy.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Constants.PRIVACY_URI.toUri()))
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.toast_error_browser), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutRate.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "market://details?id=${packageName}".toUri()
                    )
                )
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.toast_thanks_support_emoji), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val shareMessage =
                    "${getString(R.string.share_app_desc)}\n\nhttps://play.google.com/store/apps/details?id=${packageName}"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }
}

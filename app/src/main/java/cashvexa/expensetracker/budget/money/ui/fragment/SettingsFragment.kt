package cashvexa.expensetracker.budget.money.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import cashvexa.expensetracker.budget.money.ui.activity.AboutActivity
import cashvexa.expensetracker.budget.money.ui.activity.AccountsActivity
import cashvexa.expensetracker.budget.money.ui.activity.CurrencyActivity
import cashvexa.expensetracker.budget.money.ui.activity.LanguageActivity
import cashvexa.expensetracker.budget.money.ui.activity.ThemeActivity
import cashvexa.expensetracker.budget.money.ui.viewmodel.TransactionViewModel
import cashvexa.expensetracker.budget.money.utility.helper.LanguageHelper
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.FragmentSettingsBinding
import kotlin.text.equals
import androidx.core.net.toUri
import cashvexa.expensetracker.budget.money.utility.Constants

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    private fun setupUI() {
        val themeMode = PrefManager.themeMode
        val themeText = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            else -> "System Default"
        }
        binding.txtThemeValue.text = themeText
        binding.txtCurrencyValue.text = "${PrefManager.currencySymbol} ${PrefManager.currency}"

        val selectedLang = LanguageHelper.getLanguageList(PrefManager.languageCode)
            .find { it.code.equals(PrefManager.languageCode, ignoreCase = true) }
        binding.txtLanguageValue.text = selectedLang?.nativeName ?: "English"

        val versionName = try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
        binding.txtAppVersion.text = getString(R.string.version, versionName)
    }

    private fun setupListeners() {
        binding.layoutTheme.setOnClickListener {
            startActivity(Intent(requireContext(), ThemeActivity::class.java))
        }

        binding.layoutManageAccounts.setOnClickListener {
            startActivity(Intent(requireContext(), AccountsActivity::class.java))
        }

        binding.layoutCurrency.setOnClickListener {
            startActivity(Intent(requireContext(), CurrencyActivity::class.java))
        }

        binding.layoutLanguage.setOnClickListener {
            startActivity(Intent(requireContext(), LanguageActivity::class.java))
        }

        binding.layoutExport.setOnClickListener {
            ExportFilterBottomSheet().show(parentFragmentManager, "ExportFilterBottomSheet")
        }

        binding.layoutClearData.setOnClickListener {
            ClearDataBottomSheet {
                viewModel.deleteAll()
                Toast.makeText(requireContext(), getString(R.string.toast_all_data_cleared), Toast.LENGTH_SHORT).show()
            }.show(parentFragmentManager, "ClearDataBottomSheet")
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        binding.layoutPrivacy.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Constants.PRIVACY_URI.toUri()))
            } catch (_: Exception) {
                Toast.makeText(requireContext(), getString(R.string.toast_error_browser), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutRate.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    "market://details?id=${requireContext().packageName}".toUri()))
            } catch (_: Exception) {
                Toast.makeText(requireContext(), getString(R.string.toast_thanks_support_emoji), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                val shareMessage = "${getString(R.string.share_app_desc)}\n\nhttps://play.google.com/store/apps/details?id=${requireContext().packageName}"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

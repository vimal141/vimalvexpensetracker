package com.fintrack.expensetracker.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.fintrack.expensetracker.ui.activity.AboutActivity
import com.fintrack.expensetracker.ui.activity.AccountsActivity
import com.fintrack.expensetracker.ui.activity.CurrencyActivity
import com.fintrack.expensetracker.ui.activity.LanguageActivity
import com.fintrack.expensetracker.ui.activity.ThemeActivity
import com.fintrack.expensetracker.ui.viewmodel.TransactionViewModel
import com.fintrack.expensetracker.utility.helper.LanguageHelper
import com.fintrack.expensetracker.utility.helper.PrefManager
import com.fintrack.expensetracker.R
import com.fintrack.expensetracker.databinding.FragmentSettingsBinding
import kotlin.text.equals

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
        binding.txtLanguageValue.text = selectedLang?.englishName ?: "English"

        binding.txtAppVersion.text = getString(R.string.version)
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
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.toast_error_browser), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutRate.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}")))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.toast_thanks_support_emoji), Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_title))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_desc))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

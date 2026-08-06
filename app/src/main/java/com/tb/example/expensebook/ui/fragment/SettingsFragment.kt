package com.tb.example.expensebook.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.lifecycle.lifecycleScope
import com.tb.example.expensebook.utility.helper.PdfGenerator
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.R
import com.tb.example.expensebook.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import com.tb.example.expensebook.databinding.FragmentSettingsBinding
import com.tb.example.expensebook.ui.activity.AccountsActivity
import com.tb.example.expensebook.ui.activity.ThemeActivity

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
            startActivity(Intent(requireContext(), com.tb.example.expensebook.ui.activity.CurrencyActivity::class.java))
        }

        binding.layoutExport.setOnClickListener {
            ExportFilterBottomSheet().show(parentFragmentManager, "ExportFilterBottomSheet")
        }

        binding.layoutClearData.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.warning_clear_all_data))
                .setMessage(getString(R.string.desc_clear_all_data))
                .setPositiveButton(getString(R.string.delete_all)) { _, _ ->
                    viewModel.deleteAll()
                    Toast.makeText(requireContext(), getString(R.string.toast_all_data_cleared), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        binding.layoutAbout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.app_title))
                .setMessage(getString(R.string.app_about_desc))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
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

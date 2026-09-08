package cashvexa.expensetracker.budget.money.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity<ActivityAboutBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityAboutBinding {
        return ActivityAboutBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupUI()
        setupListeners()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun getAppVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    private fun setupUI() {
        binding.txtAppVersion.text = getString(R.string.version, getAppVersionName())
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCheckUpdate.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.about_latest_version, getAppVersionName()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

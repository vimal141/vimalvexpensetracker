package cashvexa.expensetracker.budget.money.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cashvexa.expensetracker.budget.money.ui.fragment.DashboardFragment
import cashvexa.expensetracker.budget.money.ui.fragment.HistoryFragment
import cashvexa.expensetracker.budget.money.ui.fragment.ReportsFragment
import cashvexa.expensetracker.budget.money.ui.fragment.SettingsFragment
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val isEdgeToEdgeEnabled: Boolean = false
    override val isAutoApplyInsetsPadding: Boolean = false

    private var doubleBackToExitPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())
    private val resetBackPressRunnable = Runnable { doubleBackToExitPressedOnce = false }

    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { _, insets ->
            insets
        }
        setupViewPager()
        setupBottomNavigation()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val targetTab = intent?.getIntExtra("target_tab", -1) ?: -1
        if (targetTab in 0..3) {
            binding.viewPager.setCurrentItem(targetTab, false)
        }
    }

    override fun onBackPressedAction() {
        if (binding.viewPager.currentItem != 0) {
            selectDashboardTab()
            return
        }

        if (doubleBackToExitPressedOnce) {
            backPressHandler.removeCallbacks(resetBackPressRunnable)
            finish()
            return
        }

        doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.toast_press_back_again_to_exit), Toast.LENGTH_SHORT).show()
        backPressHandler.postDelayed(resetBackPressRunnable, 2000)
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.isUserInputEnabled = true

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboardFragment -> binding.viewPager.setCurrentItem(0, false)
                R.id.historyFragment -> binding.viewPager.setCurrentItem(1, false)
                R.id.reportsFragment -> binding.viewPager.setCurrentItem(2, false)
                R.id.settingsFragment -> binding.viewPager.setCurrentItem(3, false)
            }
            true
        }

        val menu = binding.bottomNavigation.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val view = binding.bottomNavigation.findViewById<View>(menuItem.itemId)
            view?.setOnLongClickListener { true }
        }
    }

    fun selectDashboardTab() {
        binding.viewPager.setCurrentItem(0, false)
    }

    fun selectHistoryTab() {
        binding.viewPager.setCurrentItem(1, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressHandler.removeCallbacks(resetBackPressRunnable)
    }

    private class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                1 -> HistoryFragment()
                2 -> ReportsFragment()
                3 -> SettingsFragment()
                else -> DashboardFragment()
            }
        }
    }
}
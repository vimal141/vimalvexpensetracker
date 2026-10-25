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
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ActivityMainBinding
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.size

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val isEdgeToEdgeEnabled: Boolean = true
    override val isAutoApplyInsetsPadding: Boolean = false

    private var doubleBackToExitPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())
    private val resetBackPressRunnable = Runnable { doubleBackToExitPressedOnce = false }

    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        // 1. Apply status bar insets as top padding to ViewPager so fragments stay below the status bar,
        // while activity_main background (@color/bgColor) fills the status bar area seamlessly.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val sysBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.viewPager.setPadding(
                sysBars.left,
                sysBars.top,
                sysBars.right,
                0
            )
            insets
        }

        // 2. Apply navigation bar insets as bottom padding to BottomNavigationView
        // so its background (@color/nav_background) reaches the bottom edge through the navigation bar,
        // while all icons and labels sit cleanly above the 3-button navigation / gesture pill!
        val initialBottomNavPadding = binding.bottomNavigation.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val navBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                initialBottomNavPadding + navBars.bottom
            )
            insets
        }

        ViewCompat.requestApplyInsets(binding.root)
        ViewCompat.requestApplyInsets(binding.bottomNavigation)

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
        if (targetTab in 0..2) {
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
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.isUserInputEnabled = true

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigation.menu[position].isChecked = true
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboardFragment -> binding.viewPager.setCurrentItem(0, false)
                R.id.historyFragment -> binding.viewPager.setCurrentItem(1, false)
                R.id.reportsFragment -> binding.viewPager.setCurrentItem(2, false)
            }
            true
        }

        val menu = binding.bottomNavigation.menu
        for (i in 0 until menu.size) {
            val menuItem = menu[i]
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
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DashboardFragment()
                1 -> HistoryFragment()
                2 -> ReportsFragment()
                else -> DashboardFragment()
            }
        }
    }
}
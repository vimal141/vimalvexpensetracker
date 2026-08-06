package com.tb.example.expensebook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tb.example.expensebook.R
import com.tb.example.expensebook.databinding.ActivityMainBinding
import com.tb.example.expensebook.ui.fragment.DashboardFragment
import com.tb.example.expensebook.ui.fragment.HistoryFragment
import com.tb.example.expensebook.ui.fragment.ReportsFragment
import com.tb.example.expensebook.ui.fragment.SettingsFragment

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val isEdgeToEdgeEnabled: Boolean = false
    override val isAutoApplyInsetsPadding: Boolean = false

    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { _, insets ->
            insets
        }
        setupViewPager()
        setupBottomNavigation()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.isUserInputEnabled = false

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
            val view = binding.bottomNavigation.findViewById<android.view.View>(menuItem.itemId)
            view?.setOnLongClickListener { true }
        }
    }


    fun selectDashboardTab() {
        binding.viewPager.setCurrentItem(0, false)
    }

    fun selectHistoryTab() {
        binding.viewPager.setCurrentItem(1, false)
    }

    private  class MainPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
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
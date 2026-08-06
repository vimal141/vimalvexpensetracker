package com.tb.example.expensebook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tb.example.expensebook.data.model.OnBoardingItem
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.R
import com.tb.example.expensebook.databinding.ActivityOnBoardingBinding
import com.tb.example.expensebook.ui.adapter.OnBoardingAdapter

class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {

    private lateinit var onBoardingAdapter: OnBoardingAdapter

    override fun inflateBinding(inflater: LayoutInflater): ActivityOnBoardingBinding {
        return ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        setupViewPager()
        setupListeners()
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setupViewPager() {
        val onboardingItems = listOf(
            OnBoardingItem(
                R.drawable.step1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
            ),
            OnBoardingItem(
                R.drawable.step2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
            ),
            OnBoardingItem(
                R.drawable.step3,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
            )
        )

        onBoardingAdapter = OnBoardingAdapter(onboardingItems)
        binding.viewPager.adapter = onBoardingAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onboardingItems.size - 1) {
                    binding.tvNext.text = getString(R.string.get_started)
                    binding.btnSkip.visibility = View.GONE
                } else {
                    binding.tvNext.text =  getString(R.string.next)
                    binding.btnSkip.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < onBoardingAdapter.itemCount) {
                binding.viewPager.currentItem += 1
            } else {
                navigateToMain()
            }
        }

        binding.btnSkip.setOnClickListener {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        PrefManager.isOnboardingDone = true
        startActivity(Intent(this, AddAccountActivity::class.java))
        finish()
    }
}
package cashvexa.expensetracker.budget.money.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import cashvexa.expensetracker.budget.money.data.model.OnBoardingItem
import cashvexa.expensetracker.budget.money.ui.adapter.OnBoardingAdapter
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import com.google.android.material.tabs.TabLayoutMediator
import cashvexa.expensetracker.budget.money.R
import cashvexa.expensetracker.budget.money.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {

    private lateinit var onBoardingAdapter: OnBoardingAdapter

    override fun inflateBinding(inflater: LayoutInflater): ActivityOnBoardingBinding {
        return ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        setupViewPager()
        setupListeners()
    }

    override fun onBackPressedAction() {
        val isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        if (isFirstLaunch) {
            navigateToMain()
        } else {
            finish()
        }
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
        val isFirstLaunch = intent.getBooleanExtra("isFirstLaunch", false)
        PrefManager.isOnboardingDone = true
        val intent = Intent(this, CurrencyActivity::class.java).apply {
            if (isFirstLaunch) {
                putExtra("isFirstLaunch", true)
            }
        }
        startActivity(intent)
        finish()
    }
}
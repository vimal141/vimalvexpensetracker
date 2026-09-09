package cashvexa.expensetracker.budget.money.ui.activity

import cashvexa.expensetracker.budget.money.R
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import cashvexa.expensetracker.budget.money.utility.helper.PrefManager
import cashvexa.expensetracker.budget.money.databinding.ActivitySplashBinding
import androidx.core.net.toUri
import cashvexa.expensetracker.budget.money.utility.Constants

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 800
        }
        binding.imgLogo.startAnimation(fadeIn)

        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in_left).apply {
            duration = 600
            startOffset = 400
        }

        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (PrefManager.isPrivacyAccepted) {
                        moveNext()
                    }
                }, 200)
            }
        })

        binding.txtAppName.startAnimation(slideUp)
        binding.txtTagline.startAnimation(slideUp)

        if (PrefManager.isPrivacyAccepted) {
            binding.clPrivacyPolicy.visibility = View.GONE
        } else {
            binding.clPrivacyPolicy.visibility = View.VISIBLE
            setPolicyTxt()
            binding.cvAgreeContinue.setOnClickListener {
                if (binding.policyCheckBox.isChecked) {
                    PrefManager.isPrivacyAccepted = true
                    moveNext()
                } else {
                    Toast.makeText(this@SplashActivity, getString(R.string.toast_agree_privacy),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressedAction() {
        finish()
    }

    private fun setPolicyTxt() {
        val fullText = getString(R.string.privacy_policy_agreement)
        val spannableString = SpannableString(fullText)

        val localizedPrivacy = getString(R.string.privacy_policy)
        var privacyStart = fullText.indexOf(localizedPrivacy, ignoreCase = true)
        var privacyEnd = if (privacyStart != -1) privacyStart + localizedPrivacy.length else -1

        if (privacyStart == -1) {
            val fallbackPrivacy = "Privacy policy"
            privacyStart = fullText.indexOf(fallbackPrivacy, ignoreCase = true)
            if (privacyStart != -1) {
                privacyEnd = privacyStart + fallbackPrivacy.length
            }
        }

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Constants.PRIVACY_URI.toUri())
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SplashActivity, R.color.primaryColor)
                ds.isUnderlineText = true
            }
        }

        if (privacyStart >= 0 && privacyEnd > privacyStart && privacyEnd <= fullText.length) {
            spannableString.setSpan(
                UnderlineSpan(),
                privacyStart,
                privacyEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this@SplashActivity,
                        R.color.primaryColor
                    )
                ), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(clickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            spannableString.setSpan(
                UnderlineSpan(),
                0,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(clickableSpan, 0, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.txtPolicy.movementMethod = LinkMovementMethod.getInstance()
        binding.txtPolicy.text = spannableString
    }


    fun moveNext() {
        val next = when {
            !PrefManager.isLanguageSet -> {
                Intent(this, LanguageActivity::class.java).apply {
                    putExtra("isFirstLaunch", true)
                }
            }
            !PrefManager.isOnboardingDone -> {
                Intent(this, OnBoardingActivity::class.java).apply {
                    putExtra("isFirstLaunch", true)
                }
            }
            !PrefManager.isCurrencySet -> {
                Intent(this, CurrencyActivity::class.java).apply {
                    putExtra("isFirstLaunch", true)
                }
            }
            PrefManager.currentAccountId == -1 -> {
                Intent(this, AddAccountActivity::class.java).apply {
                    putExtra("isFirstLaunch", true)
                }
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
        startActivity(next)
        finish()
    }

}
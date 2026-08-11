package com.tb.example.expensebook.ui.activity

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tb.example.expensebook.R
import com.tb.example.expensebook.utility.helper.PrefManager
import com.tb.example.expensebook.databinding.ActivitySplashBinding
import androidx.core.net.toUri
import com.tb.example.expensebook.utility.helper.Const

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreateActivity(savedInstanceState: Bundle?) {

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 800
        }
        binding.imgLogo.startAnimation(fadeIn)

        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left).apply {
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

        val privacyText = "Privacy policy"
        val privacyStart = fullText.indexOf(privacyText)
        val privacyEnd = privacyStart + privacyText.length

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

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(Intent.ACTION_VIEW, Const.PRIVACY_URI.toUri())
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SplashActivity, R.color.primaryColor)
                ds.isUnderlineText = true
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

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
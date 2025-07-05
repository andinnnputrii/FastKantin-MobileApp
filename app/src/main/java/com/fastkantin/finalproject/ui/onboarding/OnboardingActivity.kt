package com.fastkantin.finalproject.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.databinding.ActivityOnboardingBinding
import com.fastkantin.finalproject.ui.auth.AuthActivity
import com.fastkantin.finalproject.utils.SessionManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(getOnboardingItems())
        binding.viewPager.adapter = onboardingAdapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonText(position)
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateButtonText(position: Int) {
        if (position == onboardingAdapter.itemCount - 1) {
            binding.btnNext.text = getString(R.string.get_started)
            binding.btnSkip.text = ""
        } else {
            binding.btnNext.text = getString(R.string.next)
            binding.btnSkip.text = getString(R.string.skip)
        }
    }

    private fun finishOnboarding() {
        sessionManager.setFirstTime(false)
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }

    private fun getOnboardingItems(): List<OnboardingItem> {
        return listOf(
            OnboardingItem(
                title = getString(R.string.onboarding_title_1),
                description = getString(R.string.onboarding_desc_1),
                image = R.drawable.onboarding_1
            ),
            OnboardingItem(
                title = getString(R.string.onboarding_title_2),
                description = getString(R.string.onboarding_desc_2),
                image = R.drawable.onboarding_2
            ),
            OnboardingItem(
                title = getString(R.string.onboarding_title_3),
                description = getString(R.string.onboarding_desc_3),
                image = R.drawable.onboarding_3
            )
        )
    }
}

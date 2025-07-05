package com.fastkantin.finalproject.ui.splash

import com.fastkantin.finalproject.ui.main.MainActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.fastkantin.finalproject.databinding.ActivitySplashBinding
import com.fastkantin.finalproject.ui.onboarding.OnboardingActivity
import com.fastkantin.finalproject.ui.auth.AuthActivity
import com.fastkantin.finalproject.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000)
    }

    private fun navigateToNextScreen() {
        val intent = when {
            sessionManager.isFirstTime() -> {
                Intent(this, OnboardingActivity::class.java)
            }
            sessionManager.isLoggedIn() -> {
                Intent(this, MainActivity::class.java)
            }
            else -> {
                Intent(this, AuthActivity::class.java)
            }
        }
        startActivity(intent)
        finish()
    }
}

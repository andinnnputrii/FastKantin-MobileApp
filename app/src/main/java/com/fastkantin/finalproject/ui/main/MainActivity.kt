package com.fastkantin.finalproject.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            navHostFragment?.let { fragment ->
                val navController = fragment.navController
                binding.bottomNavigation.setupWithNavController(navController)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: setup manual navigation if needed
            setupManualNavigation()
        }
    }

    private fun setupManualNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to home fragment manually if needed
                    true
                }
                R.id.nav_search -> {
                    // Navigate to search fragment manually if needed
                    true
                }
                R.id.nav_cart -> {
                    // Navigate to cart fragment manually if needed
                    true
                }
                R.id.nav_orders -> {
                    // Navigate to orders fragment manually if needed
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile fragment manually if needed
                    true
                }
                else -> false
            }
        }
    }
}

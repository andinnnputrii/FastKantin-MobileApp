package com.fastkantin.finalproject.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fastkantin.finalproject.data.entity.User
import com.fastkantin.finalproject.databinding.ActivityEditProfileBinding
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.AuthViewModel

class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditProfileActivity"
    }

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var authViewModel: AuthViewModel

    private var currentUser: User? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityEditProfileBinding.inflate(layoutInflater)
            setContentView(binding.root)

            sessionManager = SessionManager(this)
            authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

            // Get user ID dari intent atau session
            userId = intent.getIntExtra("user_id", -1)
            if (userId <= 0) {
                userId = sessionManager.getUserId()
            }

            Log.d(TAG, "Edit profile for userId: $userId")

            if (userId <= 0) {
                Log.e(TAG, "Invalid user ID: $userId")
                Toast.makeText(this, "Error: Data user tidak valid", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            setupToolbar()
            setupClickListeners()
            observeViewModel()
            loadCurrentUserData()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error loading edit profile", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "Edit Profil"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun loadCurrentUserData() {
        try {
            if (userId > 0) {
                authViewModel.getUserById(userId)
            } else {
                Log.e(TAG, "Cannot load user data: invalid userId $userId")
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data", e)
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnSave.setOnClickListener {
                try {
                    if (validateInput()) {
                        updateProfile()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in save button click", e)
                    Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnCancel.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun validateInput(): Boolean {
        try {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.etFullName.error = "Nama lengkap tidak boleh kosong"
                return false
            }

            if (email.isEmpty()) {
                binding.etEmail.error = "Email tidak boleh kosong"
                return false
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Format email tidak valid"
                return false
            }

            if (username.isEmpty()) {
                binding.etUsername.error = "Username tidak boleh kosong"
                return false
            }

            if (phone.isEmpty()) {
                binding.etPhone.error = "Nomor telepon tidak boleh kosong"
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error validating input", e)
            return false
        }
    }

    private fun updateProfile() {
        try {
            currentUser?.let { user ->
                val updatedUser = user.copy(
                    full_name = binding.etFullName.text.toString().trim(),
                    email = binding.etEmail.text.toString().trim(),
                    username = binding.etUsername.text.toString().trim(),
                    phone = binding.etPhone.text.toString().trim()
                )

                Log.d(TAG, "Updating user: ${updatedUser.user_id}")
                authViewModel.updateUser(updatedUser)
            } ?: run {
                Log.e(TAG, "Current user is null, cannot update")
                Toast.makeText(this, "Error: Data user tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        try {
            authViewModel.currentUser.observe(this) { user ->
                try {
                    user?.let {
                        Log.d(TAG, "Current user loaded: ${it.user_id}")
                        currentUser = it
                        populateFields(it)
                    } ?: run {
                        Log.e(TAG, "User data is null")
                        Toast.makeText(this, "Error: Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling current user", e)
                }
            }

            authViewModel.updateResult.observe(this) { success ->
                try {
                    if (success == true) {
                        // Update session dengan data baru
                        currentUser?.let { user ->
                            sessionManager.createLoginSession(
                                user.user_id,
                                user.username,
                                user.email,
                                user.full_name
                            )
                        }

                        Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling update result", e)
                }
            }

            authViewModel.errorMessage.observe(this) { message ->
                try {
                    if (!message.isNullOrEmpty()) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }

            authViewModel.isLoading.observe(this) { isLoading ->
                try {
                    binding.btnSave.isEnabled = isLoading != true
                    binding.progressBar.visibility = if (isLoading == true) android.view.View.VISIBLE else android.view.View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    private fun populateFields(user: User) {
        try {
            binding.apply {
                etFullName.setText(user.full_name ?: "")
                etEmail.setText(user.email ?: "")
                etUsername.setText(user.username ?: "")
                etPhone.setText(user.phone ?: "")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating fields", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

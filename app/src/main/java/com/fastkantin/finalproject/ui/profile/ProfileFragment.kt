package com.fastkantin.finalproject.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fastkantin.finalproject.databinding.FragmentProfileBinding
import com.fastkantin.finalproject.ui.auth.AuthActivity
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.AuthViewModel

class ProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sessionManager = SessionManager(requireContext())
            authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

            setupUI()
            setupClickListeners()
            observeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        try {
            binding.apply {
                tvFullName.text = sessionManager.getFullName() ?: "Nama tidak tersedia"
                tvEmail.text = sessionManager.getEmail() ?: "Email tidak tersedia"
                tvUsername.text = sessionManager.getUsername() ?: "Username tidak tersedia"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnEditProfile.setOnClickListener {
                try {
                    val userId = sessionManager.getUserId()
                    Log.d(TAG, "Edit profile clicked, userId: $userId")

                    if (userId > 0 && context != null && isAdded) {
                        val intent = Intent(requireContext(), EditProfileActivity::class.java).apply {
                            putExtra("user_id", userId)
                        }
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Invalid user data: userId=$userId, context=$context, isAdded=$isAdded")
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Error: Data user tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling edit profile click", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error membuka edit profil", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.btnLogout.setOnClickListener {
                try {
                    sessionManager.logout()
                    if (context != null && isAdded) {
                        startActivity(Intent(requireContext(), AuthActivity::class.java))
                        requireActivity().finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling logout", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun observeViewModel() {
        try {
            val userId = sessionManager.getUserId()
            if (userId > 0) {
                authViewModel.getUserByIdLiveData(userId).observe(viewLifecycleOwner) { user ->
                    try {
                        user?.let {
                            Log.d(TAG, "User data updated: ${it.full_name}")

                            // Update session data jika ada perubahan dari database
                            sessionManager.createLoginSession(
                                it.user_id,
                                it.username,
                                it.email,
                                it.full_name
                            )

                            // Update UI
                            binding.apply {
                                tvFullName.text = it.full_name
                                tvEmail.text = it.email
                                tvUsername.text = it.username
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating user data", e)
                    }
                }
            } else {
                Log.e(TAG, "Invalid user ID: $userId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            // Refresh data ketika kembali dari edit profile
            setupUI()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

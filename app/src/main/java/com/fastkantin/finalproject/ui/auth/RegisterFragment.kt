package com.fastkantin.finalproject.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.entity.User
import com.fastkantin.finalproject.databinding.FragmentRegisterBinding
import com.fastkantin.finalproject.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword, fullName, phone)) {
                val user = User(
                    username = username,
                    email = email,
                    password = password,
                    full_name = fullName,
                    phone = phone
                )
                authViewModel.register(user)
            }
        }

        binding.tvLogin.setOnClickListener {
            (activity as AuthActivity).replaceFragment(LoginFragment())
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phone: String
    ): Boolean {
        if (username.isEmpty()) {
            binding.etUsername.error = getString(R.string.error_empty_field)
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_empty_field)
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_invalid_email)
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_empty_field)
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            return false
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = getString(R.string.error_empty_field)
            return false
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = getString(R.string.error_password_mismatch)
            return false
        }

        if (fullName.isEmpty()) {
            binding.etFullName.error = getString(R.string.error_empty_field)
            return false
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = getString(R.string.error_empty_field)
            return false
        }

        return true
    }

    private fun observeViewModel() {
        authViewModel.registerResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "Registrasi berhasil! Silakan login.",
                    Toast.LENGTH_SHORT
                ).show()
                (activity as AuthActivity).replaceFragment(LoginFragment())
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnRegister.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.fastkantin.finalproject.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.databinding.FragmentLoginBinding
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            (activity as AuthActivity).replaceFragment(RegisterFragment())
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
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

        return true
    }

    private fun observeViewModel() {
        authViewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                sessionManager.createLoginSession(
                    user.user_id,
                    user.username,
                    user.email,
                    user.full_name
                )
                (activity as AuthActivity).navigateToMain()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_login_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

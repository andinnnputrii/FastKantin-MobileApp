package com.fastkantin.finalproject.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.fastkantin.finalproject.databinding.FragmentHomeBinding
import com.fastkantin.finalproject.ui.menu.MenuListActivity
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.TenantViewModel

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var tenantViewModel: TenantViewModel
    private lateinit var tenantAdapter: TenantAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sessionManager = SessionManager(requireContext())
            tenantViewModel = ViewModelProvider(this)[TenantViewModel::class.java]

            setupRecyclerView()
            setupUI()
            observeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading home page", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            tenantAdapter = TenantAdapter { tenant ->
                // Navigate to menu list dengan tenant_id - dengan null safety
                try {
                    Log.d(TAG, "Navigating to menu list for tenant: ${tenant.tenant_id}")

                    if (tenant.tenant_id > 0) {
                        val intent = Intent(requireContext(), MenuListActivity::class.java).apply {
                            putExtra("tenant_id", tenant.tenant_id)
                            putExtra("tenant_name", tenant.tenant_name ?: "Menu")
                        }

                        // Validasi context sebelum start activity
                        if (context != null && isAdded) {
                            startActivity(intent)
                        } else {
                            Log.e(TAG, "Context is null or fragment not added")
                        }
                    } else {
                        Log.e(TAG, "Invalid tenant ID: ${tenant.tenant_id}")
                        Toast.makeText(requireContext(), "Error: Data tenant tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to menu list", e)
                    Toast.makeText(requireContext(), "Error membuka menu", Toast.LENGTH_SHORT).show()
                }
            }

            binding.rvTenants.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = tenantAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupUI() {
        try {
            val fullName = sessionManager.getFullName() ?: "User"
            binding.tvWelcome.text = "Selamat Datang, $fullName"
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
            binding.tvWelcome.text = "Selamat Datang"
        }
    }

    private fun observeViewModel() {
        try {
            tenantViewModel.allTenants.observe(viewLifecycleOwner) { tenants ->
                try {
                    Log.d(TAG, "Received ${tenants?.size ?: 0} tenants")

                    if (!tenants.isNullOrEmpty()) {
                        tenantAdapter.submitList(tenants)
                        binding.rvTenants.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                    } else {
                        binding.rvTenants.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating tenant list", e)
                }
            }

            tenantViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }

            tenantViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
                try {
                    if (!message.isNullOrEmpty() && isAdded) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

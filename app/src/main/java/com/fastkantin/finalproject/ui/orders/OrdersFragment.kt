package com.fastkantin.finalproject.ui.orders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastkantin.finalproject.databinding.FragmentOrdersBinding
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.OrderViewModel

class OrdersFragment : Fragment() {

    companion object {
        private const val TAG = "OrdersFragment"
    }

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sessionManager = SessionManager(requireContext())
            orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]

            setupRecyclerView()
            observeViewModel()
            loadOrderData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading orders", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            orderAdapter = OrderAdapter { order ->
                // Handle order item click - navigate to order detail
                try {
                    Log.d(TAG, "Order clicked: ${order.order_id}")

                    if (order.order_id > 0 && context != null && isAdded) {
                        val intent = Intent(requireContext(), OrderDetailActivity::class.java).apply {
                            putExtra("order_id", order.order_id)
                            putExtra("order", order) // Pass entire order object
                        }
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Invalid order data: orderId=${order.order_id}, context=$context, isAdded=$isAdded")
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Error: Data pesanan tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling order click", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error membuka detail pesanan", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.rvOrders.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = orderAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun observeViewModel() {
        try {
            orderViewModel.ordersByUser.observe(viewLifecycleOwner) { orders ->
                try {
                    Log.d(TAG, "Orders received: ${orders?.size ?: 0}")

                    if (!orders.isNullOrEmpty()) {
                        orderAdapter.submitList(orders)
                        updateUI(false)
                    } else {
                        orderAdapter.submitList(emptyList())
                        updateUI(true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating orders list", e)
                }
            }

            orderViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }

            orderViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
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

    private fun loadOrderData() {
        try {
            val userId = sessionManager.getUserId()
            if (userId > 0) {
                Log.d(TAG, "Loading orders for user: $userId")
                orderViewModel.getOrdersByUser(userId)
            } else {
                Log.e(TAG, "Invalid user ID: $userId")
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error: User tidak valid", Toast.LENGTH_SHORT).show()
                }
                updateUI(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading order data", e)
            updateUI(true)
        }
    }

    private fun updateUI(isEmpty: Boolean) {
        try {
            if (isEmpty) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvOrders.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvOrders.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            // Refresh data saat kembali ke fragment
            loadOrderData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.fastkantin.finalproject.ui.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.databinding.FragmentCartBinding
import com.fastkantin.finalproject.ui.checkout.CheckoutActivity
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.*

class CartFragment : Fragment() {

    companion object {
        private const val TAG = "CartFragment"
    }

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var sessionManager: SessionManager

    private var currentCartTotal: Double = 0.0
    private var currentItemCount: Int = 0

    // Activity result launcher untuk checkout
    private val checkoutLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Checkout berhasil, refresh data keranjang
            Log.d(TAG, "Checkout successful, refreshing cart data")
            loadCartData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sessionManager = SessionManager(requireContext())
            cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

            setupRecyclerView()
            setupClickListeners()
            observeViewModel()
            loadCartData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error loading cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            cartAdapter = CartAdapter(
                onQuantityChanged = { cart, newQuantity ->
                    try {
                        Log.d(TAG, "Quantity changed: ${cart.menu_id} -> $newQuantity")

                        if (newQuantity > 0) {
                            val updatedCart = cart.copy(quantity = newQuantity)
                            cartViewModel.updateCartItem(updatedCart)
                        } else {
                            // Jika quantity 0, hapus item
                            cartViewModel.removeFromCart(cart)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling quantity change", e)
                        Toast.makeText(requireContext(), "Error updating quantity", Toast.LENGTH_SHORT).show()
                    }
                },
                onRemoveItem = { cart ->
                    try {
                        Log.d(TAG, "Remove item: ${cart.menu_id}")
                        cartViewModel.removeFromCart(cart)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error removing item", e)
                        Toast.makeText(requireContext(), "Error removing item", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            binding.rvCartItems.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = cartAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnCheckout.setOnClickListener {
                handleCheckoutClick()
            }

            binding.btnClearCart.setOnClickListener {
                handleClearCartClick()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun handleCheckoutClick() {
        try {
            Log.d(TAG, "Checkout clicked - Total: $currentCartTotal, Items: $currentItemCount")

            // Validasi sebelum checkout
            if (currentItemCount <= 0) {
                Toast.makeText(requireContext(), "Keranjang kosong", Toast.LENGTH_SHORT).show()
                return
            }

            if (currentCartTotal <= 0) {
                Toast.makeText(requireContext(), "Total pesanan tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            val userId = sessionManager.getUserId()
            if (userId <= 0) {
                Toast.makeText(requireContext(), "Error: User tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            // Validasi context dan fragment state
            if (context == null || !isAdded) {
                Log.e(TAG, "Context is null or fragment not added")
                return
            }

            // Navigate to checkout dengan activity result launcher
            val intent = Intent(requireContext(), CheckoutActivity::class.java).apply {
                putExtra("user_id", userId)
                putExtra("total_amount", currentCartTotal)
                putExtra("item_count", currentItemCount)
            }

            checkoutLauncher.launch(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling checkout click", e)
            Toast.makeText(requireContext(), "Error processing checkout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleClearCartClick() {
        try {
            val userId = sessionManager.getUserId()
            if (userId > 0) {
                Log.d(TAG, "Clear cart for user: $userId")
                cartViewModel.clearCart(userId)
            } else {
                Toast.makeText(requireContext(), "Error: User tidak valid", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart", e)
            Toast.makeText(requireContext(), "Error clearing cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        try {
            val userId = sessionManager.getUserId()
            if (userId > 0) {
                // Observe cart items
                cartViewModel.getCartByUser(userId).observe(viewLifecycleOwner) { cartItems ->
                    try {
                        Log.d(TAG, "Cart items updated: ${cartItems?.size ?: 0}")

                        if (!cartItems.isNullOrEmpty()) {
                            cartAdapter.submitList(cartItems)
                            updateUI(false)
                        } else {
                            cartAdapter.submitList(emptyList())
                            updateUI(true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating cart items", e)
                    }
                }

                // Observe cart total
                cartViewModel.getCartTotal(userId).observe(viewLifecycleOwner) { total ->
                    try {
                        currentCartTotal = total ?: 0.0
                        val formattedTotal = if (currentCartTotal > 0) {
                            NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(currentCartTotal)
                        } else {
                            "Rp 0"
                        }

                        binding.tvTotal.text = formattedTotal
                        updateCheckoutButton()

                        Log.d(TAG, "Cart total updated: $currentCartTotal")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating cart total", e)
                    }
                }

                // Observe item count
                cartViewModel.getCartItemCount(userId).observe(viewLifecycleOwner) { count ->
                    try {
                        currentItemCount = count ?: 0
                        binding.tvItemCount.text = "$currentItemCount item"
                        updateCheckoutButton()

                        Log.d(TAG, "Cart item count updated: $currentItemCount")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating item count", e)
                    }
                }
            } else {
                Log.e(TAG, "Invalid user ID: $userId")
                Toast.makeText(requireContext(), "Error: User tidak valid", Toast.LENGTH_SHORT).show()
            }

            // Observe operation results
            cartViewModel.operationResult.observe(viewLifecycleOwner) { success ->
                try {
                    if (success == true) {
                        Log.d(TAG, "Cart operation successful")
                        // Data akan ter-update otomatis via LiveData
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling operation result", e)
                }
            }

            cartViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
                try {
                    if (!message.isNullOrEmpty() && isAdded) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }

            cartViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    private fun updateCheckoutButton() {
        try {
            val isEnabled = currentItemCount > 0 && currentCartTotal > 0
            binding.btnCheckout.isEnabled = isEnabled
            binding.btnCheckout.alpha = if (isEnabled) 1.0f else 0.5f

            Log.d(TAG, "Checkout button enabled: $isEnabled (items: $currentItemCount, total: $currentCartTotal)")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating checkout button", e)
        }
    }

    private fun loadCartData() {
        try {
            // Data akan di-load otomatis via LiveData observers
            Log.d(TAG, "Cart data will be loaded via LiveData observers")
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadCartData", e)
        }
    }

    private fun updateUI(isEmpty: Boolean) {
        try {
            if (isEmpty) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.layoutCart.visibility = View.GONE
                binding.btnClearCart.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutCart.visibility = View.VISIBLE
                binding.btnClearCart.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            // Refresh data saat kembali ke fragment
            loadCartData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

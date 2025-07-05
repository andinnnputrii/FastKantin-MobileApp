package com.fastkantin.finalproject.ui.checkout

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.entity.Order
import com.fastkantin.finalproject.data.entity.OrderDetail
import com.fastkantin.finalproject.databinding.ActivityCheckoutBinding
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.CartViewModel
import com.fastkantin.finalproject.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CheckoutActivity"
    }

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel

    private var userId: Int = -1
    private var totalAmount: Double = 0.0
    private var itemCount: Int = 0
    private var selectedPaymentMethod: String = "Cash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityCheckoutBinding.inflate(layoutInflater)
            setContentView(binding.root)

            sessionManager = SessionManager(this)
            cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
            orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]

            // Get data dari intent dengan null safety
            userId = intent.getIntExtra("user_id", -1)
            totalAmount = intent.getDoubleExtra("total_amount", 0.0)
            itemCount = intent.getIntExtra("item_count", 0)

            Log.d(TAG, "Checkout data - UserId: $userId, Total: $totalAmount, Items: $itemCount")

            // Validasi data
            if (userId <= 0) {
                userId = sessionManager.getUserId()
            }

            if (userId <= 0 || totalAmount <= 0 || itemCount <= 0) {
                Log.e(TAG, "Invalid checkout data - UserId: $userId, Total: $totalAmount, Items: $itemCount")
                Toast.makeText(this, "Error: Data checkout tidak valid", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            setupToolbar()
            setupUI()
            setupPaymentMethodListener()
            setupClickListeners()
            observeViewModel()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error loading checkout", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Checkout"
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupUI() {
        try {
            val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalAmount)

            binding.apply {
                tvItemCount.text = "$itemCount item"
                tvTotalAmount.text = formattedTotal
                tvFinalTotal.text = formattedTotal

                // Set pickup time (15-20 menit dari sekarang)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, 15)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvPickupTime.text = "Sekitar ${timeFormat.format(calendar.time)} (15-20 menit)"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
        }
    }

    private fun setupPaymentMethodListener() {
        try {
            binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
                selectedPaymentMethod = when (checkedId) {
                    R.id.rb_qris -> "QRIS"
                    R.id.rb_cash -> "Cash"
                    else -> "Cash"
                }
                Log.d(TAG, "Payment method selected: $selectedPaymentMethod")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up payment method listener", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnPlaceOrder.setOnClickListener {
                processCheckout()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun processCheckout() {
        try {
            Log.d(TAG, "Processing checkout...")

            // Disable button dan show loading
            binding.btnPlaceOrder.isEnabled = false
            binding.progressBar.visibility = android.view.View.VISIBLE

            // Generate pickup time (15-20 menit dari sekarang)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 15)
            val pickupTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
            val orderDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Create order object
            val order = Order(
                user_id = userId,
                total_price = totalAmount,
                order_date = orderDate,
                pickup_time = pickupTime,
                payment_method = selectedPaymentMethod,
                payment_status = "Pending",
                status = "Pending"
            )

            Log.d(TAG, "Creating order: $order")

            // Process order via ViewModel
            orderViewModel.createOrderFromCart(order, userId)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing checkout", e)
            Toast.makeText(this, "Error memproses pesanan", Toast.LENGTH_SHORT).show()

            // Re-enable button dan hide loading
            binding.btnPlaceOrder.isEnabled = true
            binding.progressBar.visibility = android.view.View.GONE
        }
    }

    private fun observeViewModel() {
        try {
            orderViewModel.orderCreationResult.observe(this) { success ->
                try {
                    binding.progressBar.visibility = android.view.View.GONE

                    if (success == true) {
                        Log.d(TAG, "Order created successfully")
                        Toast.makeText(this, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()

                        // Kembali ke halaman utama
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e(TAG, "Failed to create order")
                        Toast.makeText(this, "Gagal membuat pesanan", Toast.LENGTH_SHORT).show()
                        binding.btnPlaceOrder.isEnabled = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling order creation result", e)
                }
            }

            orderViewModel.errorMessage.observe(this) { message ->
                try {
                    if (!message.isNullOrEmpty()) {
                        Log.e(TAG, "Order error: $message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        binding.progressBar.visibility = android.view.View.GONE
                        binding.btnPlaceOrder.isEnabled = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }

            orderViewModel.isLoading.observe(this) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) android.view.View.VISIBLE else android.view.View.GONE
                    binding.btnPlaceOrder.isEnabled = isLoading != true
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

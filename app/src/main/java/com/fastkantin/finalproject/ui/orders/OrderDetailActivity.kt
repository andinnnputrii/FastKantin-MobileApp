package com.fastkantin.finalproject.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastkantin.finalproject.data.entity.Order
import com.fastkantin.finalproject.databinding.ActivityOrderDetailBinding
import com.fastkantin.finalproject.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OrderDetailActivity"
    }

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderDetailAdapter: OrderDetailAdapter

    private var currentOrder: Order? = null
    private var orderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityOrderDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]

            // Get order data dari intent
            currentOrder = intent.getParcelableExtra("order")
            orderId = intent.getIntExtra("order_id", -1)

            Log.d(TAG, "Received order: $currentOrder, orderId: $orderId")

            if (currentOrder == null && orderId > 0) {
                // Load order from database if object is null
                loadOrderFromDatabase()
            } else if (currentOrder == null) {
                Toast.makeText(this, "Error: Data pesanan tidak valid", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            setupToolbar()
            setupRecyclerView()
            setupUI()
            observeViewModel()
            loadOrderDetails()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error loading order detail", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "Detail Pesanan"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            orderDetailAdapter = OrderDetailAdapter()

            binding.rvOrderDetails.apply {
                layoutManager = LinearLayoutManager(this@OrderDetailActivity)
                adapter = orderDetailAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupUI() {
        try {
            currentOrder?.let { order ->
                binding.apply {
                    // Order ID
                    tvOrderId.text = "Pesanan #${String.format("%03d", order.order_id)}"

                    // Order Date
                    val orderDate = formatDate(order.order_date)
                    tvOrderDate.text = orderDate

                    // Status
                    tvStatus.text = getStatusText(order.status, order.payment_status)

                    // Payment Method
                    tvPaymentMethod.text = order.payment_method ?: "Cash"

                    // Pickup Time
                    val pickupTime = formatTime(order.pickup_time)
                    tvPickupTime.text = pickupTime

                    // Total
                    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(order.total_price)
                    tvTotal.text = formattedTotal
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
        }
    }

    private fun loadOrderFromDatabase() {
        try {
            // Implementation untuk load order dari database jika diperlukan
            Log.d(TAG, "Loading order from database: $orderId")
            // orderViewModel.getOrderById(orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading order from database", e)
        }
    }

    private fun loadOrderDetails() {
        try {
            val orderIdToLoad = currentOrder?.order_id ?: orderId
            if (orderIdToLoad > 0) {
                Log.d(TAG, "Loading order details for order: $orderIdToLoad")
                orderViewModel.getOrderDetailsByOrderId(orderIdToLoad)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading order details", e)
        }
    }

    private fun observeViewModel() {
        try {
            orderViewModel.orderDetails.observe(this) { orderDetails ->
                try {
                    Log.d(TAG, "Order details received: ${orderDetails?.size ?: 0}")

                    if (!orderDetails.isNullOrEmpty()) {
                        orderDetailAdapter.submitList(orderDetails)
                        binding.tvEmptyState.visibility = android.view.View.GONE
                        binding.rvOrderDetails.visibility = android.view.View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = android.view.View.VISIBLE
                        binding.rvOrderDetails.visibility = android.view.View.GONE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating order details", e)
                }
            }

            orderViewModel.isLoading.observe(this) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) android.view.View.VISIBLE else android.view.View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }

            orderViewModel.errorMessage.observe(this) { message ->
                try {
                    if (!message.isNullOrEmpty()) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
        }
    }

    private fun formatDate(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) return "Tanggal tidak tersedia"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))

            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: "Tanggal tidak valid"
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date: $dateString", e)
            "Tanggal tidak valid"
        }
    }

    private fun formatTime(timeString: String?): String {
        return try {
            if (timeString.isNullOrEmpty()) return "Waktu tidak tersedia"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            val time = inputFormat.parse(timeString)
            time?.let { outputFormat.format(it) } ?: "Waktu tidak valid"
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: $timeString", e)
            "Waktu tidak valid"
        }
    }

    private fun getStatusText(status: String?, paymentStatus: String?): String {
        return when {
            status == "Completed" -> "Selesai"
            status == "Cancelled" -> "Dibatalkan"
            paymentStatus == "Paid" -> "Dibayar"
            else -> "Menunggu"
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

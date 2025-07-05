package com.fastkantin.finalproject.ui.orders

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.entity.Order
import com.fastkantin.finalproject.databinding.ItemOrderBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    companion object {
        private const val TAG = "OrderAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        try {
            val order = getItem(position)
            if (order != null) {
                holder.bind(order)
            } else {
                Log.e(TAG, "Order at position $position is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding order at position $position", e)
        }
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            try {
                binding.apply {
                    // Set order ID
                    tvOrderId.text = "Pesanan #${String.format("%03d", order.order_id)}"

                    // Format and set order date
                    val orderDate = formatOrderDate(order.order_date)
                    tvOrderDate.text = orderDate

                    // Format and set total payment
                    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(order.total_price)
                    tvTotalPayment.text = formattedTotal

                    // Set payment method
                    tvPaymentMethod.text = order.payment_method ?: "Cash"

                    // Format and set pickup time
                    val pickupTime = formatPickupTime(order.pickup_time)
                    tvPickupTime.text = pickupTime

                    // Set status with appropriate color
                    setOrderStatus(order.status, order.payment_status)

                    // Set click listener
                    root.setOnClickListener {
                        try {
                            Log.d(TAG, "Order clicked: ${order.order_id}")
                            onOrderClick(order)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error handling order click", e)
                        }
                    }

                    btnViewDetail.setOnClickListener {
                        try {
                            Log.d(TAG, "View detail clicked: ${order.order_id}")
                            onOrderClick(order)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error handling view detail click", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in bind method for order ${order.order_id}", e)
            }
        }

        private fun formatOrderDate(dateString: String?): String {
            return try {
                if (dateString.isNullOrEmpty()) return "Tanggal tidak tersedia"

                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))

                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: "Tanggal tidak valid"
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting order date: $dateString", e)
                "Tanggal tidak valid"
            }
        }

        private fun formatPickupTime(timeString: String?): String {
            return try {
                if (timeString.isNullOrEmpty()) return "Waktu tidak tersedia"

                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                val time = inputFormat.parse(timeString)
                time?.let { outputFormat.format(it) } ?: "Waktu tidak valid"
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting pickup time: $timeString", e)
                "Waktu tidak valid"
            }
        }

        private fun setOrderStatus(status: String?, paymentStatus: String?) {
            try {
                val context = binding.root.context

                when {
                    status == "Completed" -> {
                        binding.tvStatus.text = "Selesai"
                        binding.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.status_completed_background)
                    }
                    status == "Cancelled" -> {
                        binding.tvStatus.text = "Dibatalkan"
                        binding.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.status_cancelled_background)
                    }
                    paymentStatus == "Paid" -> {
                        binding.tvStatus.text = "Dibayar"
                        binding.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.status_paid_background)
                    }
                    else -> {
                        binding.tvStatus.text = "Menunggu"
                        binding.tvStatus.background = ContextCompat.getDrawable(context, R.drawable.status_pending_background)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting order status", e)
                binding.tvStatus.text = "Status tidak diketahui"
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.order_id == newItem.order_id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}

package com.fastkantin.finalproject.ui.orders

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.dao.OrderDetailWithMenu
import com.fastkantin.finalproject.databinding.ItemOrderDetailBinding
import java.text.NumberFormat
import java.util.*

class OrderDetailAdapter : ListAdapter<OrderDetailWithMenu, OrderDetailAdapter.OrderDetailViewHolder>(OrderDetailDiffCallback()) {

    companion object {
        private const val TAG = "OrderDetailAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        try {
            val orderDetail = getItem(position)
            if (orderDetail != null) {
                holder.bind(orderDetail)
            } else {
                Log.e(TAG, "OrderDetail at position $position is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding order detail at position $position", e)
        }
    }

    inner class OrderDetailViewHolder(
        private val binding: ItemOrderDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(orderDetail: OrderDetailWithMenu) {
            try {
                binding.apply {
                    // Set menu name
                    tvMenuName.text = orderDetail.menu_name ?: "Menu tidak tersedia"

                    // Set quantity
                    tvQuantity.text = "x${orderDetail.quantity}"

                    // Set price per item
                    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(orderDetail.price)
                    tvPrice.text = formattedPrice

                    // Calculate and set total price for this item
                    val totalPrice = orderDetail.price * orderDetail.quantity
                    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalPrice)
                    tvTotalPrice.text = formattedTotal

                    // Load menu image
                    try {
                        Glide.with(itemView.context)
                            .load(orderDetail.menu_image)
                            .placeholder(R.drawable.placeholder_food)
                            .error(R.drawable.placeholder_food)
                            .centerCrop()
                            .into(ivMenu)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image", e)
                        ivMenu.setImageResource(R.drawable.placeholder_food)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in bind method", e)
            }
        }
    }

    class OrderDetailDiffCallback : DiffUtil.ItemCallback<OrderDetailWithMenu>() {
        override fun areItemsTheSame(oldItem: OrderDetailWithMenu, newItem: OrderDetailWithMenu): Boolean {
            return oldItem.detail_id == newItem.detail_id
        }

        override fun areContentsTheSame(oldItem: OrderDetailWithMenu, newItem: OrderDetailWithMenu): Boolean {
            return oldItem == newItem
        }
    }
}

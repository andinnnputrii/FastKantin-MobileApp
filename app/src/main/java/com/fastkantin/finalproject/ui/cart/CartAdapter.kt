package com.fastkantin.finalproject.ui.cart

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.dao.CartWithMenu
import com.fastkantin.finalproject.data.entity.Cart
import com.fastkantin.finalproject.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val onQuantityChanged: (Cart, Int) -> Unit,
    private val onRemoveItem: (Cart) -> Unit
) : ListAdapter<CartWithMenu, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    companion object {
        private const val TAG = "CartAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        try {
            val cartWithMenu = getItem(position)
            if (cartWithMenu != null) {
                holder.bind(cartWithMenu)
            } else {
                Log.e(TAG, "CartWithMenu at position $position is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding cart item at position $position", e)
        }
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartWithMenu: CartWithMenu) {
            try {
                binding.apply {
                    // Set data dengan null safety
                    tvMenuName.text = cartWithMenu.menu_name ?: "Menu tidak tersedia"
                    tvQuantity.text = cartWithMenu.quantity.toString()

                    val price = cartWithMenu.menu_price
                    val totalPrice = price * cartWithMenu.quantity

                    // Format harga
                    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(price)
                    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalPrice)

                    tvPrice.text = formattedPrice
                    tvTotalPrice.text = formattedTotal

                    // Handle note visibility
                    if (!cartWithMenu.note.isNullOrEmpty()) {
                        tvNote.text = "Catatan: ${cartWithMenu.note}"
                        tvNote.visibility = android.view.View.VISIBLE
                    } else {
                        tvNote.visibility = android.view.View.GONE
                    }

                    // --- LOGIKA UTAMA UNTUK MEMUAT GAMBAR DENGAN GLIDE (Disempurnakan untuk Cart) ---
                    val imagePathFromDb = cartWithMenu.menu_image // Mengambil image_path dari data gabungan
                    val context = itemView.context // Dapatkan context dari itemView

                    Log.d(TAG, "Cart Item: Menu ID=${cartWithMenu.menu_id}, Name=${cartWithMenu.menu_name}, Image Path from DB: '$imagePathFromDb'")

                    try {
                        // Coba muat sebagai drawable resource
                        val resourceId = context.resources.getIdentifier(
                            imagePathFromDb, "drawable", context.packageName
                        )

                        if (resourceId != 0) { // Jika nama drawable ditemukan dan dikonversi ke ID
                            Log.d(TAG, "Loading cart item image from drawable resource ID: $resourceId for name: '$imagePathFromDb'")
                            Glide.with(context)
                                .load(resourceId) // Memuat berdasarkan Resource ID
                                .placeholder(R.drawable.placeholder_food) // Placeholder saat memuat
                                .error(R.drawable.placeholder_food)     // Gambar jika terjadi error
                                .centerCrop() // Atur bagaimana gambar akan menyesuaikan ImageView
                                .into(ivMenu)
                        } else { // Jika tidak ditemukan sebagai drawable, asumsikan itu adalah path file atau URL
                            Log.d(TAG, "Drawable resource not found for cart item name: '$imagePathFromDb'. Attempting to load as file path/URL.")
                            Glide.with(context)
                                .load(imagePathFromDb) // Memuat berdasarkan String (path file atau URL)
                                .placeholder(R.drawable.placeholder_food)
                                .error(R.drawable.placeholder_food)
                                .centerCrop()
                                .into(ivMenu)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image for cart item ${cartWithMenu.menu_id} from path '$imagePathFromDb'", e)
                        ivMenu.setImageResource(R.drawable.placeholder_food) // Tampilkan placeholder jika ada exception
                    }
                    // --- AKHIR LOGIKA GLIDE ---

                    // Convert CartWithMenu to Cart for operations
                    val cart = Cart(
                        cart_id = cartWithMenu.cart_id,
                        user_id = cartWithMenu.user_id,
                        menu_id = cartWithMenu.menu_id,
                        quantity = cartWithMenu.quantity,
                        note = cartWithMenu.note ?: ""
                    )

                    // Setup quantity controls dengan logika yang benar
                    setupQuantityControls(cart, cartWithMenu.quantity)

                    // Setup remove button
                    btnRemove.setOnClickListener {
                        try {
                            Log.d(TAG, "Remove item clicked: ${cartWithMenu.menu_name}")
                            onRemoveItem(cart)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error removing item", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in bind method", e)
            }
        }

        private fun setupQuantityControls(cart: Cart, currentQuantity: Int) {
            try {
                binding.apply {
                    // Setup decrease button
                    btnDecrease.setOnClickListener {
                        try {
                            if (currentQuantity > 1) {
                                val newQuantity = currentQuantity - 1
                                Log.d(TAG, "Decrease quantity: $currentQuantity -> $newQuantity")
                                onQuantityChanged(cart, newQuantity)
                            } else {
                                Log.d(TAG, "Cannot decrease quantity below 1. Consider removing item.")
                                // Quantity sudah 1, tidak bisa dikurangi lagi
                                // User harus menggunakan tombol hapus jika ingin menghapus item
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error decreasing quantity", e)
                        }
                    }

                    // Setup increase button
                    btnIncrease.setOnClickListener {
                        try {
                            val newQuantity = currentQuantity + 1
                            Log.d(TAG, "Increase quantity: $currentQuantity -> $newQuantity")
                            onQuantityChanged(cart, newQuantity)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error increasing quantity", e)
                        }
                    }

                    // Disable/enable decrease button berdasarkan quantity
                    btnDecrease.isEnabled = currentQuantity > 1
                    btnDecrease.alpha = if (currentQuantity > 1) 1.0f else 0.5f

                    // Increase button selalu enabled
                    btnIncrease.isEnabled = true
                    btnIncrease.alpha = 1.0f
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up quantity controls", e)
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartWithMenu>() {
        override fun areItemsTheSame(oldItem: CartWithMenu, newItem: CartWithMenu): Boolean {
            return oldItem.cart_id == newItem.cart_id
        }

        override fun areContentsTheSame(oldItem: CartWithMenu, newItem: CartWithMenu): Boolean {
            // Compare all relevant content fields, especially those displayed
            return oldItem.quantity == newItem.quantity &&
                    oldItem.note == newItem.note &&
                    oldItem.menu_name == newItem.menu_name &&
                    oldItem.menu_price == newItem.menu_price &&
                    oldItem.menu_image == newItem.menu_image // Include image path for content comparison
        }
    }
}
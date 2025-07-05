package com.fastkantin.finalproject.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.entity.Cart
import com.fastkantin.finalproject.data.entity.Menu
import com.fastkantin.finalproject.databinding.ActivityMenuDetailBinding
import com.fastkantin.finalproject.utils.SessionManager
import com.fastkantin.finalproject.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.*

class MenuDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MenuDetailActivity"
    }

    private lateinit var binding: ActivityMenuDetailBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var sessionManager: SessionManager

    private var currentMenu: Menu? = null
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMenuDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            sessionManager = SessionManager(this)

            // PASTIKAN ANDA MENGGUNAKAN ViewModelFactory ANDA DI SINI!
            // Jika CartViewModel Anda memiliki dependency (seperti Repository),
            // Anda HARUS menggunakan ViewModelFactory custom Anda.
            // Contoh (jika Anda memiliki CartViewModelFactory):
            // val factory = CartViewModelFactory(application) // Asumsi Anda membuat CartViewModelFactory
            // cartViewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
            // Jika CartViewModel tidak punya dependency, maka baris ini sudah benar:
            cartViewModel = ViewModelProvider(this)[CartViewModel::class.java] // Biarkan ini jika CartViewModel tidak ada dependensi

            // Get menu data dari intent
            currentMenu = intent.getParcelableExtra("menu")
            val menuId = intent.getIntExtra("menu_id", -1) // Ini mungkin tidak perlu jika selalu pakai Parcelable Menu

            Log.d(TAG, "Received menu: $currentMenu, menuId: $menuId")

            if (currentMenu == null) { // Simplify this check, no need for menuId > 0 if you always pass 'menu' object
                Toast.makeText(this, "Error: Data menu tidak valid", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            setupToolbar()
            setupUI()
            setupClickListeners()
            observeViewModel()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error loading menu detail", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = currentMenu?.name ?: "Detail Menu"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupUI() {
        try {
            currentMenu?.let { menu ->
                binding.apply {
                    tvMenuName.text = menu.name
                    tvDescription.text = menu.description
                    tvCategory.text = menu.category

                    val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(menu.price)
                    tvPrice.text = formattedPrice

                    tvQuantity.text = quantity.toString()

                    // --- PERUBAHAN PENTING DI SINI UNTUK MEMUAT GAMBAR LOKAL ---
                    val imageResId = resources.getIdentifier(
                        menu.image_path, // Nama file gambar dari entitas Menu (misal: "nasi_goreng")
                        "drawable",    // Tipe resource
                        packageName    // Nama package aplikasi Anda
                    )

                    if (imageResId != 0) { // Pastikan ID resource ditemukan (bukan 0)
                        Glide.with(this@MenuDetailActivity)
                            .load(imageResId) // Load dari ID resource Int
                            .placeholder(R.drawable.placeholder_food)
                            .error(R.drawable.placeholder_food)
                            .centerCrop()
                            .into(ivMenu)
                    } else {
                        // Jika gambar tidak ditemukan berdasarkan nama file, atau nama file kosong/invalid
                        Glide.with(this@MenuDetailActivity)
                            .load(R.drawable.placeholder_food) // Load placeholder default
                            .centerCrop()
                            .into(ivMenu)
                        Log.w(TAG, "Image resource not found for: ${menu.image_path}. Displaying placeholder.")
                    }
                    // --- AKHIR PERUBAHAN PENTING ---

                    updateTotalPrice()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.apply {
                btnDecrease.setOnClickListener {
                    if (quantity > 1) {
                        quantity--
                        tvQuantity.text = quantity.toString()
                        updateTotalPrice()
                    }
                }

                btnIncrease.setOnClickListener {
                    quantity++
                    tvQuantity.text = quantity.toString()
                    updateTotalPrice()
                }

                btnAddToCart.setOnClickListener {
                    addToCart()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun updateTotalPrice() {
        try {
            currentMenu?.let { menu ->
                val totalPrice = menu.price * quantity
                val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalPrice)
                binding.tvTotalPrice.text = formattedTotal
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating total price", e)
        }
    }

    private fun addToCart() {
        try {
            val userId = sessionManager.getUserId()
            if (userId == -1) {
                Toast.makeText(this, "Error: User tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            currentMenu?.let { menu ->
                val note = binding.etNote.text.toString().trim()

                val cart = Cart(
                    user_id = userId,
                    menu_id = menu.menu_id,
                    quantity = quantity,
                    note = note
                )

                cartViewModel.addToCart(cart)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart", e)
            Toast.makeText(this, "Error menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        try {
            cartViewModel.operationResult.observe(this) { success ->
                if (success == true) {
                    Toast.makeText(this, "Berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            cartViewModel.errorMessage.observe(this) { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }

            cartViewModel.isLoading.observe(this) { isLoading ->
                binding.btnAddToCart.isEnabled = isLoading != true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
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
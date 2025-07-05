package com.fastkantin.finalproject.ui.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fastkantin.finalproject.databinding.ActivityMenuListBinding
import com.fastkantin.finalproject.viewmodel.MenuViewModel

class MenuListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MenuListActivity"
    }

    private lateinit var binding: ActivityMenuListBinding
    private lateinit var menuViewModel: MenuViewModel
    private lateinit var menuAdapter: MenuAdapter

    private var tenantId: Int = -1
    private var tenantName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMenuListBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Get data dari intent dengan null safety
            tenantId = intent.getIntExtra("tenant_id", -1)
            tenantName = intent.getStringExtra("tenant_name") ?: "Menu"

            Log.d(TAG, "Received tenant_id: $tenantId, tenant_name: $tenantName")

            // Validasi data yang diterima
            if (tenantId <= 0) {
                Log.e(TAG, "Invalid tenant_id: $tenantId")
                Toast.makeText(this, "Error: Data tenant tidak valid", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            setupToolbar()
            setupRecyclerView()
            setupViewModel()
            loadMenuData()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = tenantName
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up toolbar", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            menuAdapter = MenuAdapter { menu ->
                // Handle menu item click - navigate to detail dengan null safety
                try {
                    Log.d(TAG, "Menu clicked: ID=${menu.menu_id}, Name=${menu.name}")

                    if (menu.menu_id > 0) {
                        val intent = Intent(this, MenuDetailActivity::class.java).apply {
                            putExtra("menu_id", menu.menu_id)
                            putExtra("menu", menu) // Pass entire menu object
                        }
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Invalid menu ID: ${menu.menu_id}")
                        Toast.makeText(this, "Error: Data menu tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling menu click", e)
                    Toast.makeText(this, "Error membuka detail menu", Toast.LENGTH_SHORT).show()
                }
            }

            binding.rvMenus.apply {
                layoutManager = LinearLayoutManager(this@MenuListActivity)
                adapter = menuAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupViewModel() {
        try {
            menuViewModel = ViewModelProvider(this)[MenuViewModel::class.java]

            menuViewModel.menusByTenant.observe(this) { menus ->
                try {
                    Log.d(TAG, "Received ${menus?.size ?: 0} menus for tenant $tenantId")

                    if (!menus.isNullOrEmpty()) {
                        menuAdapter.submitList(menus)
                        binding.tvEmptyState.visibility = android.view.View.GONE
                        binding.rvMenus.visibility = android.view.View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = android.view.View.VISIBLE
                        binding.rvMenus.visibility = android.view.View.GONE
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating menu list", e)
                }
            }

            menuViewModel.isLoading.observe(this) { isLoading ->
                try {
                    binding.progressBar.visibility = if (isLoading == true) android.view.View.VISIBLE else android.view.View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating loading state", e)
                }
            }

            menuViewModel.errorMessage.observe(this) { message ->
                try {
                    if (!message.isNullOrEmpty()) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error message", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ViewModel", e)
        }
    }

    private fun loadMenuData() {
        try {
            if (tenantId > 0) {
                menuViewModel.getMenuByTenant(tenantId)
            } else {
                Log.e(TAG, "Cannot load menu data: invalid tenant_id $tenantId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading menu data", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

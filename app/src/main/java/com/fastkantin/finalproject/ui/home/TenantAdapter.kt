package com.fastkantin.finalproject.ui.home

import android.util.Log // Tambahkan import Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.entity.Tenant
import com.fastkantin.finalproject.databinding.ItemTenantBinding

class TenantAdapter(
    private val onTenantClick: (Tenant) -> Unit
) : ListAdapter<Tenant, TenantAdapter.TenantViewHolder>(TenantDiffCallback()) {

    companion object {
        private const val TAG = "TenantAdapter" // Pastikan TAG ini ada
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val binding = ItemTenantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TenantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        try {
            val tenant = getItem(position)
            if (tenant != null) {
                holder.bind(tenant)
            } else {
                Log.e(TAG, "Tenant at position $position is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding tenant at position $position", e)
        }
    }

    inner class TenantViewHolder(
        private val binding: ItemTenantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tenant: Tenant) {
            try {
                binding.apply {
                    tvTenantName.text = tenant.tenant_name
                    tvDescription.text = tenant.description
                    tvLocation.text = tenant.location

                    // === BAGIAN INI SANGAT PENTING: LOG DEBUG DAN LOGIKA GLIDE ===
                    val imagePathFromDb = tenant.image_path
                    Log.d(TAG, "Tenant ID: ${tenant.tenant_id}, Name: ${tenant.tenant_name}, Image Path from DB: '$imagePathFromDb'")

                    val context = itemView.context // Dapatkan context dari itemView

                    try {
                        // Coba muat sebagai drawable resource
                        val resourceId = context.resources.getIdentifier(
                            imagePathFromDb, "drawable", context.packageName
                        )

                        if (resourceId != 0) { // Jika nama drawable ditemukan dan dikonversi ke ID
                            Log.d(TAG, "Loading image from drawable resource ID: $resourceId for name: '$imagePathFromDb'")
                            Glide.with(context)
                                .load(resourceId) // Memuat berdasarkan Resource ID
                                .placeholder(R.drawable.placeholder_food) // Placeholder saat memuat
                                .error(R.drawable.placeholder_food)     // Gambar jika terjadi error
                                .centerCrop() // Atur bagaimana gambar akan menyesuaikan ImageView
                                .into(ivTenant)
                        } else { // Jika tidak ditemukan sebagai drawable, asumsikan itu adalah path file atau URL
                            Log.d(TAG, "Drawable resource not found for name: '$imagePathFromDb'. Attempting to load as file path/URL.")
                            Glide.with(context)
                                .load(imagePathFromDb) // Memuat berdasarkan String (path file atau URL)
                                .placeholder(R.drawable.placeholder_food)
                                .error(R.drawable.placeholder_food)
                                .centerCrop()
                                .into(ivTenant)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading image for tenant ${tenant.tenant_id} from path '$imagePathFromDb'", e)
                        ivTenant.setImageResource(R.drawable.placeholder_food) // Tampilkan placeholder jika ada exception
                    }
                    // === AKHIR BAGIAN PENTING ===

                    root.setOnClickListener {
                        Log.d(TAG, "Tenant clicked: ID=${tenant.tenant_id}, Name=${tenant.tenant_name}")
                        onTenantClick(tenant)
                    }
                    root.isClickable = true
                    root.isFocusable = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in bind method for tenant ${tenant.tenant_id}", e)
            }
        }
    }

    class TenantDiffCallback : DiffUtil.ItemCallback<Tenant>() {
        override fun areItemsTheSame(oldItem: Tenant, newItem: Tenant): Boolean {
            return oldItem.tenant_id == newItem.tenant_id
        }

        override fun areContentsTheSame(oldItem: Tenant, newItem: Tenant): Boolean {
            return oldItem == newItem
        }
    }
}
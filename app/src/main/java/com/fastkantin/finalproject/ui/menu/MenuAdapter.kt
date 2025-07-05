package com.fastkantin.finalproject.ui.menu

import android.util.Log // Tambahkan import Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fastkantin.finalproject.R
import com.fastkantin.finalproject.data.entity.Menu
import com.fastkantin.finalproject.databinding.ItemMenuBinding
import java.text.NumberFormat
import java.util.*

class MenuAdapter(
    private val onMenuClick: (Menu) -> Unit
) : ListAdapter<Menu, MenuAdapter.MenuViewHolder>(MenuDiffCallback()) {

    companion object {
        private const val TAG = "MenuAdapter" // Tambahkan TAG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = getItem(position)
        if (menu != null) {
            holder.bind(menu)
        } else {
            Log.e(TAG, "Menu item at position $position is null") // Tambahkan log error
        }
    }

    inner class MenuViewHolder(
        private val binding: ItemMenuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(menu: Menu) {
            binding.apply {
                tvMenuName.text = menu.name
                tvDescription.text = menu.description
                tvCategory.text = menu.category

                // Format harga
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(menu.price)
                tvPrice.text = formattedPrice

                // --- LOGIKA UTAMA UNTUK MEMUAT GAMBAR DENGAN GLIDE (Disempurnakan) ---
                val imagePathFromDb = menu.image_path
                val context = itemView.context // Dapatkan context dari itemView

                Log.d(TAG, "Menu ID: ${menu.menu_id}, Name: ${menu.name}, Image Path from DB: '$imagePathFromDb'")

                try {
                    // Coba muat sebagai drawable resource
                    val resourceId = context.resources.getIdentifier(
                        imagePathFromDb, "drawable", context.packageName
                    )

                    if (resourceId != 0) { // Jika nama drawable ditemukan dan dikonversi ke ID
                        Log.d(TAG, "Loading menu image from drawable resource ID: $resourceId for name: '$imagePathFromDb'")
                        Glide.with(context)
                            .load(resourceId) // Memuat berdasarkan Resource ID
                            .placeholder(R.drawable.placeholder_food) // Placeholder saat memuat
                            .error(R.drawable.placeholder_food)     // Gambar jika terjadi error
                            .centerCrop() // Atur bagaimana gambar akan menyesuaikan ImageView
                            .into(ivMenu)
                    } else { // Jika tidak ditemukan sebagai drawable, asumsikan itu adalah path file atau URL
                        Log.d(TAG, "Drawable resource not found for menu name: '$imagePathFromDb'. Attempting to load as file path/URL.")
                        Glide.with(context)
                            .load(imagePathFromDb) // Memuat berdasarkan String (path file atau URL)
                            .placeholder(R.drawable.placeholder_food)
                            .error(R.drawable.placeholder_food)
                            .centerCrop()
                            .into(ivMenu)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image for menu ${menu.menu_id} from path '$imagePathFromDb'", e)
                    ivMenu.setImageResource(R.drawable.placeholder_food) // Tampilkan placeholder jika ada exception
                }
                // --- AKHIR LOGIKA GLIDE ---

                // Click listener
                root.setOnClickListener {
                    onMenuClick(menu)
                }
            }
        }
    }

    class MenuDiffCallback : DiffUtil.ItemCallback<Menu>() {
        override fun areItemsTheSame(oldItem: Menu, newItem: Menu): Boolean {
            return oldItem.menu_id == newItem.menu_id
        }

        override fun areContentsTheSame(oldItem: Menu, newItem: Menu): Boolean {
            return oldItem == newItem
        }
    }
}
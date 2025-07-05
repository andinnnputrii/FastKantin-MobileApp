package com.fastkantin.finalproject.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fastkantin.finalproject.data.dao.*
import com.fastkantin.finalproject.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Tenant::class, Menu::class, Cart::class, Order::class, OrderDetail::class],
    version = 1,
    exportSchema = false
)
abstract class FastKantinDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tenantDao(): TenantDao
    abstract fun menuDao(): MenuDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun orderDetailDao(): OrderDetailDao

    companion object {
        @Volatile
        private var INSTANCE: FastKantinDatabase? = null

        fun getDatabase(context: Context): FastKantinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FastKantinDatabase::class.java,
                    "fastkantin_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext)) // <-- Passed context here
                    .fallbackToDestructiveMigration() // Add this for development
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Pass context to the DatabaseCallback
        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database, context) // Pass context to populateDatabase
                    }
                }
            }
        }

        // Receive context in populateDatabase
        private suspend fun populateDatabase(database: FastKantinDatabase, context: Context) {
            val tenantDao = database.tenantDao()
            val menuDao = database.menuDao()

            try {
                // Pastikan nama-nama file di sini cocok PERSIS dengan nama file di res/drawable
                // TANPA EKSTENSI (.jpg/.png) karena kita akan memuatnya sebagai resource name
                val tenants = listOf(
                    Tenant(tenant_name = "Warung Bu Sari", description = "Makanan tradisional Indonesia", location = "Lantai 1", image_path = "tenant_warung_bu_sari"), // Nama file: tenant_warung_bu_sari.png/jpg
                    Tenant(tenant_name = "Kedai Mie Ayam", description = "Mie ayam dan bakso spesial", location = "Lantai 1", image_path = "tenant_kedai_mie_ayam"), // Nama file: tenant_kedai_mie_ayam.png/jpg
                    Tenant(tenant_name = "Nasi Gudeg Jogja", description = "Gudeg asli Yogyakarta", location = "Lantai 2", image_path = "tenant_nasi_gudeg_jogja"), // Nama file: tenant_nasi_gudeg_jogja.png/jpg
                    Tenant(tenant_name = "Ayam Geprek Bensu", description = "Ayam geprek pedas mantap", location = "Lantai 2", image_path = "tenant_ayam_geprek_bensu"), // Nama file: tenant_ayam_geprek_bensu.png/jpg
                    Tenant(tenant_name = "Es Teh Manis", description = "Minuman segar dan jus buah", location = "Lantai 1", image_path = "tenant_es_teh_manis") // Nama file: tenant_es_teh_manis.png/jpg
                )
                // Assuming insertAllTenants is available in TenantDao. If not, use insertTenant in a loop.
                tenants.forEach { tenantDao.insertTenant(it) } // Changed to single insert if insertAllTenants is not there

                // Insert sample menu items
                val menus = listOf(
                    // Warung Bu Sari (tenant_id = 1)
                    Menu(tenant_id = 1, name = "Nasi Gudeg", description = "Nasi dengan gudeg khas Jogja", price = 15000.0, category = "Makanan Utama", image_path = "menu_nasi_gudeg"),
                    Menu(tenant_id = 1, name = "Soto Ayam", description = "Soto ayam dengan kuah bening", price = 12000.0, category = "Makanan Utama", image_path = "menu_soto_ayam"),
                    Menu(tenant_id = 1, name = "Gado-gado", description = "Sayuran dengan bumbu kacang", price = 10000.0, category = "Makanan Utama", image_path = "menu_gado_gado"),

                    // Kedai Mie Ayam (tenant_id = 2)
                    Menu(tenant_id = 2, name = "Mie Ayam Bakso", description = "Mie ayam dengan bakso", price = 13000.0, category = "Makanan Utama", image_path = "menu_mie_ayam_bakso"),
                    Menu(tenant_id = 2, name = "Bakso Urat", description = "Bakso urat dengan kuah hangat", price = 11000.0, category = "Makanan Utama", image_path = "menu_bakso_urat"),
                    Menu(tenant_id = 2, name = "Pangsit Goreng", description = "Pangsit goreng crispy", price = 8000.0, category = "Snack", image_path = "menu_pangsit_goreng"),

                    // Nasi Gudeg Jogja (tenant_id = 3)
                    Menu(tenant_id = 3, name = "Gudeg Komplit", description = "Gudeg dengan ayam dan telur", price = 18000.0, category = "Makanan Utama", image_path = "menu_gudeg_komplit"),
                    Menu(tenant_id = 3, name = "Gudeg Vegetarian", description = "Gudeg tanpa daging", price = 14000.0, category = "Makanan Utama", image_path = "menu_gudeg_vegetarian"),

                    // Ayam Geprek Bensu (tenant_id = 4)
                    Menu(tenant_id = 4, name = "Ayam Geprek Level 1", description = "Ayam geprek tidak pedas", price = 16000.0, category = "Makanan Utama", image_path = "menu_ayam_geprek_lv1"),
                    Menu(tenant_id = 4, name = "Ayam Geprek Level 5", description = "Ayam geprek super pedas", price = 16000.0, category = "Makanan Utama", image_path = "menu_ayam_geprek_lv5"),
                    Menu(tenant_id = 4, name = "Tahu Tempe Geprek", description = "Tahu tempe geprek", price = 12000.0, category = "Makanan Utama", image_path = "menu_tahu_tempe_geprek"),

                    // Es Teh Manis (tenant_id = 5)
                    Menu(tenant_id = 5, name = "Es Teh Manis", description = "Es teh manis segar", price = 5000.0, category = "Minuman", image_path = "menu_es_teh_manis"),
                    Menu(tenant_id = 5, name = "Es Jeruk", description = "Es jeruk segar", price = 7000.0, category = "Minuman", image_path = "menu_es_jeruk"),
                    Menu(tenant_id = 5, name = "Jus Alpukat", description = "Jus alpukat creamy", price = 10000.0, category = "Minuman", image_path = "menu_jus_alpukat"),
                    Menu(tenant_id = 5, name = "Kopi Hitam", description = "Kopi hitam pahit", price = 6000.0, category = "Minuman", image_path = "menu_kopi_hitam")
                )
                // Assuming insertAllMenu is available in MenuDao. If not, use insertMenu in a loop.
                menus.forEach { menuDao.insertMenu(it) } // Changed to single insert if insertAllMenu is not there

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
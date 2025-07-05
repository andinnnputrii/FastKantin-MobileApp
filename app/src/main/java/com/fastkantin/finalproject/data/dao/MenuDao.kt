package com.fastkantin.finalproject.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fastkantin.finalproject.data.entity.Menu

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu WHERE tenant_id = :tenantId ORDER BY name ASC")
    fun getMenuByTenant(tenantId: Int): LiveData<List<Menu>>

    @Query("SELECT * FROM menu WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchMenu(searchQuery: String): LiveData<List<Menu>>

    @Query("SELECT * FROM menu WHERE category = :category ORDER BY name ASC")
    fun getMenuByCategory(category: String): LiveData<List<Menu>>

    @Query("SELECT * FROM menu WHERE menu_id = :menuId")
    suspend fun getMenuById(menuId: Int): Menu?

    @Query("SELECT DISTINCT category FROM menu ORDER BY category ASC")
    fun getAllCategories(): LiveData<List<String>>

    @Insert
    suspend fun insertMenu(menu: Menu)

    @Insert
    suspend fun insertAllMenu(menus: List<Menu>)

    @Update
    suspend fun updateMenu(menu: Menu)

    @Delete
    suspend fun deleteMenu(menu: Menu)
}

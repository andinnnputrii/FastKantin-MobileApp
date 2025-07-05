package com.fastkantin.finalproject.data.repository

import androidx.lifecycle.LiveData
import com.fastkantin.finalproject.data.dao.MenuDao
import com.fastkantin.finalproject.data.entity.Menu

class MenuRepository(private val menuDao: MenuDao) {

    fun getMenuByTenant(tenantId: Int): LiveData<List<Menu>> {
        return menuDao.getMenuByTenant(tenantId)
    }

    fun searchMenu(searchQuery: String): LiveData<List<Menu>> {
        return menuDao.searchMenu(searchQuery)
    }

    fun getMenuByCategory(category: String): LiveData<List<Menu>> {
        return menuDao.getMenuByCategory(category)
    }

    suspend fun getMenuById(menuId: Int): Menu? {
        return menuDao.getMenuById(menuId)
    }

    fun getAllCategories(): LiveData<List<String>> {
        return menuDao.getAllCategories()
    }
}

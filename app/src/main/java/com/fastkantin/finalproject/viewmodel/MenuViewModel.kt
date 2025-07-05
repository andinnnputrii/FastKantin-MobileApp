package com.fastkantin.finalproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastkantin.finalproject.data.database.FastKantinDatabase
import com.fastkantin.finalproject.data.entity.Menu
import com.fastkantin.finalproject.data.repository.MenuRepository
import kotlinx.coroutines.launch

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val menuRepository: MenuRepository

    private val _menusByTenant = MutableLiveData<List<Menu>>()
    val menusByTenant: LiveData<List<Menu>> = _menusByTenant

    private val _searchResults = MutableLiveData<List<Menu>>()
    val searchResults: LiveData<List<Menu>> = _searchResults

    private val _allMenus = MutableLiveData<List<Menu>>()
    val allMenus: LiveData<List<Menu>> = _allMenus

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val database = FastKantinDatabase.getDatabase(application)
        menuRepository = MenuRepository(database.menuDao())
        loadAllMenus()
        loadCategories()
    }

    fun getMenuByTenant(tenantId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                menuRepository.getMenuByTenant(tenantId).observeForever { menus ->
                    _menusByTenant.value = menus
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error loading menus"
                _isLoading.value = false
            }
        }
    }

    fun searchMenu(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (query.isEmpty()) {
                    _searchResults.value = _allMenus.value ?: emptyList()
                } else {
                    menuRepository.searchMenu(query).observeForever { menus ->
                        _searchResults.value = menus
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error searching menus"
                _isLoading.value = false
            }
        }
    }

    fun getMenuByCategory(category: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (category == "Semua") {
                    _searchResults.value = _allMenus.value ?: emptyList()
                } else {
                    menuRepository.getMenuByCategory(category).observeForever { menus ->
                        _searchResults.value = menus
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error filtering menus"
                _isLoading.value = false
            }
        }
    }

    private fun loadAllMenus() {
        viewModelScope.launch {
            try {
                // Get all menus from all tenants
                val allMenusList = mutableListOf<Menu>()
                for (tenantId in 1..5) { // Assuming we have 5 tenants
                    menuRepository.getMenuByTenant(tenantId).observeForever { menus ->
                        allMenusList.addAll(menus)
                        _allMenus.value = allMenusList.distinctBy { it.menu_id }
                        _searchResults.value = _allMenus.value
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error loading all menus"
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                menuRepository.getAllCategories().observeForever { categories ->
                    val categoriesWithAll = mutableListOf("Semua")
                    categoriesWithAll.addAll(categories)
                    _categories.value = categoriesWithAll
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error loading categories"
            }
        }
    }
}

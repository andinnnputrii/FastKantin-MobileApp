package com.fastkantin.finalproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastkantin.finalproject.data.database.FastKantinDatabase
import com.fastkantin.finalproject.data.entity.Tenant
import com.fastkantin.finalproject.data.repository.TenantRepository
import kotlinx.coroutines.launch

class TenantViewModel(application: Application) : AndroidViewModel(application) {

    private val tenantRepository: TenantRepository
    val allTenants: LiveData<List<Tenant>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val database = FastKantinDatabase.getDatabase(application)
        tenantRepository = TenantRepository(database.tenantDao())
        allTenants = tenantRepository.getAllTenants()

        loadTenants() // agar saat init langsung load data
    }

    private fun loadTenants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Jika getAllTenants() sudah LiveData realtime, bagian ini cukup
                // Namun kalau butuh fetching manual dari API, bisa taruh disini
                // Contoh dummy (karena kita pakai Room sudah observe otomatis)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

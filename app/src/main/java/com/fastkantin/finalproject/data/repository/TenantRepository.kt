package com.fastkantin.finalproject.data.repository

import androidx.lifecycle.LiveData
import com.fastkantin.finalproject.data.dao.TenantDao
import com.fastkantin.finalproject.data.entity.Tenant

class TenantRepository(private val tenantDao: TenantDao) {

    fun getAllTenants(): LiveData<List<Tenant>> {
        return tenantDao.getAllTenants()
    }

    suspend fun getTenantById(tenantId: Int): Tenant? {
        return tenantDao.getTenantById(tenantId)
    }
}

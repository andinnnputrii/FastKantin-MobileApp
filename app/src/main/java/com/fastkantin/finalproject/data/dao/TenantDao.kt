package com.fastkantin.finalproject.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fastkantin.finalproject.data.entity.Tenant

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants ORDER BY tenant_name ASC")
    fun getAllTenants(): LiveData<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE tenant_id = :tenantId")
    suspend fun getTenantById(tenantId: Int): Tenant?

    @Insert
    suspend fun insertTenant(tenant: Tenant)

    @Insert
    suspend fun insertAllTenants(tenants: List<Tenant>)

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)
}

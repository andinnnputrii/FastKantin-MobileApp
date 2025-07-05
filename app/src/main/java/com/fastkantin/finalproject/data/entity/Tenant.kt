package com.fastkantin.finalproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "tenants")
@Parcelize
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val tenant_id: Int = 0,
    val tenant_name: String,
    val description: String,
    val location: String,
    val image_path: String
) : Parcelable

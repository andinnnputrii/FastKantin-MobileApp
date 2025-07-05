package com.fastkantin.finalproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "menu",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["tenant_id"],
            childColumns = ["tenant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class Menu(
    @PrimaryKey(autoGenerate = true)
    val menu_id: Int = 0,
    val tenant_id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val image_path: String
) : Parcelable

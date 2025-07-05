package com.fastkantin.finalproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Parcelize
data class Order(
    @PrimaryKey(autoGenerate = true)
    val order_id: Int = 0,
    val user_id: Int,
    val total_price: Double,
    val order_date: String,
    val pickup_time: String,
    val payment_method: String, // QRIS, Cash, Transfer
    val payment_status: String, // Paid, Pending, Cancelled
    val status: String // Pending, Completed, Cancelled
) : Parcelable

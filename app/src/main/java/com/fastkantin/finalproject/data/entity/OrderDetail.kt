package com.fastkantin.finalproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_details",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["order_id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Menu::class,
            parentColumns = ["menu_id"],
            childColumns = ["menu_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OrderDetail(
    @PrimaryKey(autoGenerate = true)
    val detail_id: Int = 0,
    val order_id: Int,
    val menu_id: Int,
    val quantity: Int,
    val price: Double
)

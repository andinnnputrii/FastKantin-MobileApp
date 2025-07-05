package com.fastkantin.finalproject.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
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
data class Cart(
    @PrimaryKey(autoGenerate = true)
    val cart_id: Int = 0,
    val user_id: Int,
    val menu_id: Int,
    val quantity: Int,
    val note: String = ""
)

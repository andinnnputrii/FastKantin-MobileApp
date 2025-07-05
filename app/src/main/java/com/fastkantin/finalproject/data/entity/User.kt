package com.fastkantin.finalproject.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "users")
@Parcelize
data class User(
    @PrimaryKey(autoGenerate = true)
    val user_id: Int = 0,
    val username: String,
    val email: String,
    val password: String,
    val full_name: String,
    val phone: String
) : Parcelable

package com.fastkantin.finalproject.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fastkantin.finalproject.data.entity.Cart

@Dao
interface CartDao {
    @Query("""
        SELECT cart.cart_id, cart.user_id, cart.menu_id, cart.quantity, cart.note,
               menu.name as menu_name, menu.price as menu_price, menu.image_path as menu_image 
        FROM cart 
        INNER JOIN menu ON cart.menu_id = menu.menu_id 
        WHERE cart.user_id = :userId
    """)
    fun getCartByUser(userId: Int): LiveData<List<CartWithMenu>>

    @Query("SELECT * FROM cart WHERE user_id = :userId AND menu_id = :menuId LIMIT 1")
    suspend fun getCartItem(userId: Int, menuId: Int): Cart?

    @Insert
    suspend fun insertCart(cart: Cart): Long

    @Update
    suspend fun updateCart(cart: Cart)

    @Delete
    suspend fun deleteCart(cart: Cart)

    @Query("DELETE FROM cart WHERE user_id = :userId")
    suspend fun clearCart(userId: Int)

    @Query("SELECT COUNT(*) FROM cart WHERE user_id = :userId")
    fun getCartItemCount(userId: Int): LiveData<Int>

    @Query("SELECT SUM(cart.quantity * menu.price) FROM cart INNER JOIN menu ON cart.menu_id = menu.menu_id WHERE cart.user_id = :userId")
    fun getCartTotal(userId: Int): LiveData<Double?>
}

data class CartWithMenu(
    val cart_id: Int,
    val user_id: Int,
    val menu_id: Int,
    val quantity: Int,
    val note: String,
    val menu_name: String,
    val menu_price: Double,
    val menu_image: String
)

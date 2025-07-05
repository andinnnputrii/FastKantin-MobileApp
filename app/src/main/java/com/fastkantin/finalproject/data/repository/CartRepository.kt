package com.fastkantin.finalproject.data.repository

import androidx.lifecycle.LiveData
import com.fastkantin.finalproject.data.dao.CartDao
import com.fastkantin.finalproject.data.dao.CartWithMenu
import com.fastkantin.finalproject.data.entity.Cart

class CartRepository(private val cartDao: CartDao) {

    fun getCartByUser(userId: Int): LiveData<List<CartWithMenu>> {
        return cartDao.getCartByUser(userId)
    }

    suspend fun addToCart(cart: Cart): Long {
        val existingCart = cartDao.getCartItem(cart.user_id, cart.menu_id)
        return if (existingCart != null) {
            val updatedCart = existingCart.copy(
                quantity = existingCart.quantity + cart.quantity,
                note = if (cart.note.isNotEmpty()) cart.note else existingCart.note
            )
            cartDao.updateCart(updatedCart)
            existingCart.cart_id.toLong()
        } else {
            cartDao.insertCart(cart)
        }
    }

    suspend fun updateCartItem(cart: Cart) {
        cartDao.updateCart(cart)
    }

    suspend fun removeFromCart(cart: Cart) {
        cartDao.deleteCart(cart)
    }

    suspend fun clearCart(userId: Int) {
        cartDao.clearCart(userId)
    }

    fun getCartItemCount(userId: Int): LiveData<Int> {
        return cartDao.getCartItemCount(userId)
    }

    fun getCartTotal(userId: Int): LiveData<Double?> {
        return cartDao.getCartTotal(userId)
    }

    suspend fun getCartItem(userId: Int, menuId: Int): Cart? {
        return cartDao.getCartItem(userId, menuId)
    }
}

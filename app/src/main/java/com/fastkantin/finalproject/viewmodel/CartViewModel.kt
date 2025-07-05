package com.fastkantin.finalproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastkantin.finalproject.data.database.FastKantinDatabase
import com.fastkantin.finalproject.data.dao.CartWithMenu
import com.fastkantin.finalproject.data.entity.Cart
import com.fastkantin.finalproject.data.repository.CartRepository
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val cartRepository: CartRepository

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _operationResult = MutableLiveData<Boolean>()
    val operationResult: LiveData<Boolean> = _operationResult

    init {
        val database = FastKantinDatabase.getDatabase(application)
        cartRepository = CartRepository(database.cartDao())
    }

    fun getCartByUser(userId: Int): LiveData<List<CartWithMenu>> {
        return cartRepository.getCartByUser(userId)
    }

    fun getCartItemCount(userId: Int): LiveData<Int> {
        return cartRepository.getCartItemCount(userId)
    }

    fun getCartTotal(userId: Int): LiveData<Double?> {
        return cartRepository.getCartTotal(userId)
    }

    fun addToCart(cart: Cart) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                cartRepository.addToCart(cart)
                _operationResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error adding to cart"
                _operationResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCartItem(cart: Cart) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                cartRepository.updateCartItem(cart)
                _operationResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error updating cart"
                _operationResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromCart(cart: Cart) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(cart)
                _operationResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error removing from cart"
                _operationResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCart(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                cartRepository.clearCart(userId)
                _operationResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error clearing cart"
                _operationResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}

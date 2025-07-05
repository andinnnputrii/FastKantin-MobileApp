package com.fastkantin.finalproject.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fastkantin.finalproject.data.database.FastKantinDatabase
import com.fastkantin.finalproject.data.dao.OrderDetailWithMenu
import com.fastkantin.finalproject.data.entity.Order
import com.fastkantin.finalproject.data.entity.OrderDetail
import com.fastkantin.finalproject.data.repository.OrderRepository
import com.fastkantin.finalproject.data.repository.CartRepository
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "OrderViewModel"
    }

    private val orderRepository: OrderRepository
    private val cartRepository: CartRepository

    private val _orderCreationResult = MutableLiveData<Boolean>()
    val orderCreationResult: LiveData<Boolean> = _orderCreationResult

    private val _ordersByUser = MutableLiveData<List<Order>>()
    val ordersByUser: LiveData<List<Order>> = _ordersByUser

    private val _orderDetails = MutableLiveData<List<OrderDetailWithMenu>>()
    val orderDetails: LiveData<List<OrderDetailWithMenu>> = _orderDetails

    private val _currentOrder = MutableLiveData<Order?>()
    val currentOrder: LiveData<Order?> = _currentOrder

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        val database = FastKantinDatabase.getDatabase(application)
        orderRepository = OrderRepository(database.orderDao(), database.orderDetailDao())
        cartRepository = CartRepository(database.cartDao())
    }

    fun createOrderFromCart(order: Order, userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating order from cart for user: $userId")

                // Get cart items
                val cartItems = mutableListOf<com.fastkantin.finalproject.data.dao.CartWithMenu>()
                cartRepository.getCartByUser(userId).observeForever { items ->
                    if (!items.isNullOrEmpty()) {
                        cartItems.clear()
                        cartItems.addAll(items)

                        viewModelScope.launch {
                            try {
                                // Create order
                                val orderId = orderRepository.createOrder(order, emptyList())
                                Log.d(TAG, "Order created with ID: $orderId")

                                // Create order details from cart items
                                val orderDetails = cartItems.map { cartItem ->
                                    OrderDetail(
                                        order_id = orderId.toInt(),
                                        menu_id = cartItem.menu_id,
                                        quantity = cartItem.quantity,
                                        price = cartItem.menu_price
                                    )
                                }

                                // Insert order details
                                orderRepository.insertOrderDetails(orderDetails)
                                Log.d(TAG, "Order details created: ${orderDetails.size} items")

                                // Clear cart after successful order
                                cartRepository.clearCart(userId)
                                Log.d(TAG, "Cart cleared for user: $userId")

                                _orderCreationResult.value = true

                            } catch (e: Exception) {
                                Log.e(TAG, "Error creating order details", e)
                                _errorMessage.value = e.message ?: "Error creating order"
                                _orderCreationResult.value = false
                            } finally {
                                _isLoading.value = false
                            }
                        }
                    } else {
                        Log.e(TAG, "Cart is empty, cannot create order")
                        _errorMessage.value = "Keranjang kosong"
                        _orderCreationResult.value = false
                        _isLoading.value = false
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error creating order from cart", e)
                _errorMessage.value = e.message ?: "Error creating order"
                _orderCreationResult.value = false
                _isLoading.value = false
            }
        }
    }

    fun getOrdersByUser(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Getting orders for user: $userId")

                orderRepository.getOrdersByUser(userId).observeForever { orders ->
                    try {
                        Log.d(TAG, "Orders received: ${orders?.size ?: 0}")
                        _ordersByUser.value = orders ?: emptyList()
                        _isLoading.value = false
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing orders", e)
                        _errorMessage.value = e.message ?: "Error loading orders"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting orders by user", e)
                _errorMessage.value = e.message ?: "Error loading orders"
                _isLoading.value = false
            }
        }
    }

    fun getOrderDetailsByOrderId(orderId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Getting order details for order: $orderId")

                orderRepository.getOrderDetailsByOrderId(orderId).observeForever { orderDetails ->
                    try {
                        Log.d(TAG, "Order details received: ${orderDetails?.size ?: 0}")
                        _orderDetails.value = orderDetails ?: emptyList()
                        _isLoading.value = false
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing order details", e)
                        _errorMessage.value = e.message ?: "Error loading order details"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting order details", e)
                _errorMessage.value = e.message ?: "Error loading order details"
                _isLoading.value = false
            }
        }
    }

    fun getOrderById(orderId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Getting order by ID: $orderId")

                val order = orderRepository.getOrderById(orderId)
                _currentOrder.value = order
                _isLoading.value = false

                if (order == null) {
                    _errorMessage.value = "Pesanan tidak ditemukan"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting order by ID", e)
                _errorMessage.value = e.message ?: "Error loading order"
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(order: Order) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                orderRepository.updateOrder(order)
                Log.d(TAG, "Order status updated: ${order.order_id}")
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error updating order status", e)
                _errorMessage.value = e.message ?: "Error updating order"
                _isLoading.value = false
            }
        }
    }
}

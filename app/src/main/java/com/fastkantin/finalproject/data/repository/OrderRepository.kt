package com.fastkantin.finalproject.data.repository

import androidx.lifecycle.LiveData
import com.fastkantin.finalproject.data.dao.OrderDao
import com.fastkantin.finalproject.data.dao.OrderDetailDao
import com.fastkantin.finalproject.data.dao.OrderDetailWithMenu
import com.fastkantin.finalproject.data.entity.Order
import com.fastkantin.finalproject.data.entity.OrderDetail

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderDetailDao: OrderDetailDao
) {

    fun getOrdersByUser(userId: Int): LiveData<List<Order>> {
        return orderDao.getOrdersByUser(userId)
    }

    suspend fun getOrderById(orderId: Int): Order? {
        return orderDao.getOrderById(orderId)
    }

    suspend fun createOrder(order: Order, orderDetails: List<OrderDetail>): Long {
        val orderId = orderDao.insertOrder(order)
        if (orderDetails.isNotEmpty()) {
            val updatedOrderDetails = orderDetails.map { it.copy(order_id = orderId.toInt()) }
            orderDetailDao.insertAllOrderDetails(updatedOrderDetails)
        }
        return orderId
    }

    suspend fun insertOrderDetails(orderDetails: List<OrderDetail>) {
        orderDetailDao.insertAllOrderDetails(orderDetails)
    }

    fun getOrderDetailsByOrderId(orderId: Int): LiveData<List<OrderDetailWithMenu>> {
        return orderDetailDao.getOrderDetailsByOrderId(orderId)
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }
}

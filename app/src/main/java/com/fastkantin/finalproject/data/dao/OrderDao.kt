package com.fastkantin.finalproject.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fastkantin.finalproject.data.entity.Order
import com.fastkantin.finalproject.data.entity.OrderDetail

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY order_date DESC")
    fun getOrdersByUser(userId: Int): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE order_id = :orderId")
    suspend fun getOrderById(orderId: Int): Order?

    @Insert
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)
}

@Dao
interface OrderDetailDao {
    @Query("""
        SELECT od.*, m.name as menu_name, m.image_path as menu_image 
        FROM order_details od 
        INNER JOIN menu m ON od.menu_id = m.menu_id 
        WHERE od.order_id = :orderId
    """)
    fun getOrderDetailsByOrderId(orderId: Int): LiveData<List<OrderDetailWithMenu>>

    @Insert
    suspend fun insertOrderDetail(orderDetail: OrderDetail)

    @Insert
    suspend fun insertAllOrderDetails(orderDetails: List<OrderDetail>)
}

data class OrderDetailWithMenu(
    val detail_id: Int,
    val order_id: Int,
    val menu_id: Int,
    val quantity: Int,
    val price: Double,
    val menu_name: String,
    val menu_image: String
)

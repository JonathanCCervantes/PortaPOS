package com.jonathan.portapos.data.dao

import androidx.room.*
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert
    suspend fun insertOrder(order: Order): Long
    // Returns the new order's ID so we can link items to it

    @Insert
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: Int): List<OrderItem>

    @Query("SELECT receiptNumber FROM orders ORDER BY id DESC LIMIT 1")
    suspend fun getLastReceiptNumber(): String?

    @Query("SELECT * FROM orders WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getOrdersByDate(startOfDay: Long, endOfDay: Long): Flow<List<Order>>

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    @Query("DELETE FROM order_items")
    suspend fun deleteAllOrderItems()
}

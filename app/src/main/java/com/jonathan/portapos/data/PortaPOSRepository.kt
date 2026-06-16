package com.jonathan.portapos.data

import com.jonathan.portapos.data.dao.OrderDao
import com.jonathan.portapos.data.dao.ProductDao
import com.jonathan.portapos.data.dao.SettingsDao
import com.jonathan.portapos.data.model.AppSettings
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import com.jonathan.portapos.data.model.Product
import kotlinx.coroutines.flow.Flow

class PortaPOSRepository(
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
    private val settingsDao: SettingsDao
) {
    // ── Products ──────────────────────────────────────────────
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCategories: Flow<List<String>> = productDao.getAllCategories()

    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // ── Orders ────────────────────────────────────────────────
    suspend fun saveOrder(order: Order, items: List<OrderItem>): Long {
        val orderId = orderDao.insertOrder(order)
        val linkedItems = items.map { it.copy(orderId = orderId.toInt()) }
        orderDao.insertOrderItems(linkedItems)
        return orderId
    }

    suspend fun getOrderById(id: Int) = orderDao.getOrderById(id)
    suspend fun getItemsForOrder(orderId: Int) = orderDao.getItemsForOrder(orderId)
    suspend fun getLastReceiptNumber() = orderDao.getLastReceiptNumber()

    fun getOrdersByDate(start: Long, end: Long) = orderDao.getOrdersByDate(start, end)

    suspend fun resetAllSales() {
        orderDao.deleteAllOrderItems()
        orderDao.deleteAllOrders()
    }

    // ── Settings ──────────────────────────────────────────────
    val settings: Flow<AppSettings?> = settingsDao.getSettings()
    suspend fun saveSettings(settings: AppSettings) = settingsDao.saveSettings(settings)
}

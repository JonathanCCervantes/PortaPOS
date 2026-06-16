package com.jonathan.portapos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jonathan.portapos.data.PortaPOSRepository
import com.jonathan.portapos.data.model.AppSettings
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import com.jonathan.portapos.data.model.Product
import java.util.Calendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Cart Item (lives in memory, not database) ─────────────────
data class CartItem(
    val product: Product,
    val quantity: Int
)

class MainViewModel(private val repository: PortaPOSRepository) : ViewModel() {

    // ── Live data from database ────────────────────────────────
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyOrders: StateFlow<List<Order>> = flow {
        val start = getStartOfDay()
        val end = getEndOfDay()
        emitAll(repository.getOrdersByDate(start, end))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTotal: StateFlow<Double> = dailyOrders.map { orders ->
        orders.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ── Cart state (only lives in memory) ─────────────────────
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    val cartTotal: StateFlow<Double> = cart.map { items ->
        items.sumOf { it.product.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = cart.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Cart actions ───────────────────────────────────────────
    fun addToCart(product: Product) {
        val current = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == product.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(CartItem(product, 1))
        }
        _cart.value = current
    }

    fun removeFromCart(product: Product) {
        val current = _cart.value.toMutableList()
        val existing = current.indexOfFirst { it.product.id == product.id }
        if (existing >= 0) {
            if (current[existing].quantity > 1) {
                current[existing] = current[existing].copy(quantity = current[existing].quantity - 1)
            } else {
                current.removeAt(existing)
            }
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // ── Product management ────────────────────────────────────
    fun saveProduct(product: Product) = viewModelScope.launch {
        if (product.id == 0) repository.insertProduct(product)
        else repository.updateProduct(product)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }

    // ── Save completed order ───────────────────────────────────
    suspend fun completeSale(paymentMethod: String, amountPaid: Double): Long {
        val total = cartTotal.value
        val orderItems = cart.value.map { cartItem ->
            OrderItem(
                orderId = 0, // will be set in repository
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                productPrice = cartItem.product.price,
                quantity = cartItem.quantity
            )
        }

        val lastReceipt = repository.getLastReceiptNumber()
        val nextReceipt = generateNextReceiptNumber(lastReceipt)

        val order = Order(
            totalAmount = total,
            paymentMethod = paymentMethod,
            amountPaid = amountPaid,
            change = if (paymentMethod == "CASH") amountPaid - total else 0.0,
            receiptNumber = nextReceipt
        )
        val orderId = repository.saveOrder(order, orderItems)
        clearCart()
        return orderId
    }

    private fun generateNextReceiptNumber(lastReceipt: String?): String {
        if (lastReceipt == null || !lastReceipt.contains("-")) {
            return "A-01"
        }

        try {
            val parts = lastReceipt.split("-")
            var prefix = parts[0]
            var number = parts[1].toInt()

            number++
            if (number > 99) {
                number = 1
                prefix = nextPrefix(prefix)
            }

            return "%s-%02d".format(prefix, number)
        } catch (e: Exception) {
            return "A-01"
        }
    }

    private fun nextPrefix(prefix: String): String {
        val charArray = prefix.toCharArray()
        var i = charArray.size - 1
        while (i >= 0) {
            if (charArray[i] < 'Z') {
                charArray[i]++
                return String(charArray)
            }
            charArray[i] = 'A'
            i--
        }
        return "A" + String(charArray)
    }

    // ── Settings ───────────────────────────────────────────────
    fun saveSettings(settings: AppSettings) = viewModelScope.launch {
        repository.saveSettings(settings)
    }

    fun resetSales() = viewModelScope.launch {
        repository.resetAllSales()
    }

    suspend fun getItemsForOrder(orderId: Int) = repository.getItemsForOrder(orderId)

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}

// Factory so ViewModel can receive the repository
class MainViewModelFactory(private val repository: PortaPOSRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

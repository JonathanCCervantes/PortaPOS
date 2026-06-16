package com.jonathan.portapos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: Int,               // which order this item belongs to
    val productId: Int,
    val productName: String,        // snapshot of name at time of sale
    val productPrice: Double,       // snapshot of price at time of sale
    val quantity: Int
)

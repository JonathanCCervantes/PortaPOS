package com.jonathan.portapos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val totalAmount: Double,
    val paymentMethod: String,      // "CASH" or "EWALLET"
    val amountPaid: Double = 0.0,
    val change: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val receiptNumber: String = ""  // Alphanumeric tracking (e.g., A-01)
)

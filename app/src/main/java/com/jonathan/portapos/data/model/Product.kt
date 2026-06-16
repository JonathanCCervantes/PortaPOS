package com.jonathan.portapos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String,
    val photoUri: String? = null,   // file path to the photo on the device
    val isAvailable: Boolean = true
)
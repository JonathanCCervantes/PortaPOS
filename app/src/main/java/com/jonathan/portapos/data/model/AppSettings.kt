package com.jonathan.portapos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,                    // only one row of settings ever exists
    val businessName: String = "My Food Store",
    val businessLogoUri: String? = null,
    val businessAddress: String = "",
    val businessTIN: String = "",
    val vatRegistered: Boolean = false,
    val qrCodeImageUri: String? = null, // path to the e-wallet QR image
    val receiptFooter: String = "Thank you for your purchase!",
    val printerMacAddress: String? = null
)

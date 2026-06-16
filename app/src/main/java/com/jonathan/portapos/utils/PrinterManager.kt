package com.jonathan.portapos.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.jonathan.portapos.data.model.AppSettings
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object PrinterManager {

    @SuppressLint("MissingPermission")
    suspend fun printReceipt(
        context: Context,
        order: Order,
        items: List<OrderItem>,
        settings: AppSettings?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = BluetoothPrintersConnections.selectFirstPaired() ?: return@withContext false
            val printer = EscPosPrinter(connection, 203, 48f, 32)
            
            val date = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault()).format(Date(order.timestamp))
            
            val logoHex = settings?.businessLogoUri?.let { path ->
                try {
                    val bitmap = BitmapFactory.decodeFile(path)
                    if (bitmap != null) {
                        "<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap) + "</img>\n"
                    } else ""
                } catch (e: Exception) { "" }
            } ?: ""

            val receiptHeader = """
                [C]$logoHex
                [C]<b>${settings?.businessName ?: "PortaPOS"}</b>
                [C]${settings?.businessAddress ?: ""}
                [C]TIN: ${settings?.businessTIN ?: ""}
                [C]--------------------------------
                [L]Order: ${order.receiptNumber}
                [L]Date: $date
                [C]--------------------------------
            """.trimIndent()

            val itemsText = items.joinToString("\n") { item ->
                "[L]${item.productName}\n[L]  ${item.quantity} x ₱%.2f [R]₱%.2f".format(item.productPrice, item.productPrice * item.quantity)
            }

            val vatDetails = if (settings?.vatRegistered == true) {
                val vatRate = 0.12
                val vatAmount = order.totalAmount * (vatRate / (1 + vatRate))
                val vatableSales = order.totalAmount - vatAmount
                """
                [L]Vatable Sales [R]₱%.2f
                [L]VAT Amount (12%%) [R]₱%.2f
                """.format(vatableSales, vatAmount).trimIndent()
            } else ""

            val receiptFooter = """
                [C]--------------------------------
                $vatDetails
                [L]<b>TOTAL [R]₱%.2f</b>
                [L]Payment: ${order.paymentMethod}
                ${if (order.paymentMethod == "CASH") "[L]Cash Given [R]₱%.2f\n[L]Change [R]₱%.2f".format(order.amountPaid, order.change) else ""}
                [C]================================
                [C]${settings?.receiptFooter ?: "Thank you!"}
            """.trimIndent()

            printer.printFormattedText(receiptHeader + "\n" + itemsText + "\n" + receiptFooter)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printOrderSlip(
        context: Context,
        order: Order,
        items: List<OrderItem>,
        settings: AppSettings?
    ): Boolean = withContext(Dispatchers.IO) {
        // Use the same formatting as printReceipt for consistency
        printReceipt(context, order, items, settings)
    }

    @SuppressLint("MissingPermission")
    suspend fun printDailyReport(
        context: Context,
        orders: List<Order>,
        total: Double,
        settings: AppSettings?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = BluetoothPrintersConnections.selectFirstPaired() ?: return@withContext false
            val printer = EscPosPrinter(connection, 203, 48f, 32)
            
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            
            val header = """
                [C]<b>DAILY SALES REPORT</b>
                [C]${settings?.businessName ?: "PortaPOS"}
                [C]Date: $date
                [C]--------------------------------
            """.trimIndent()

            val stats = """
                [L]Total Orders: [R]${orders.size}
                [L]<b>TOTAL SALES: [R]₱%.2f</b>
                [C]--------------------------------
            """.format(total).trimIndent()

            val footer = """
                [C]*** End of Report ***
            """.trimIndent()

            printer.printFormattedText(header + "\n" + stats + "\n" + footer)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

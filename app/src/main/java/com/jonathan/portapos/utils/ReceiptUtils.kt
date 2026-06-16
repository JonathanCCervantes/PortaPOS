package com.jonathan.portapos.utils

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.jonathan.portapos.data.model.AppSettings
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReceiptUtils {

    // ── Build the receipt as a plain text string ───────────────
    fun buildReceiptText(order: Order, items: List<OrderItem>, settings: AppSettings?): String {
        val date = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
            .format(Date(order.timestamp))
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine(settings?.businessName ?: "PortaPOS")
        sb.appendLine("================================")
        sb.appendLine("Receipt: ${order.receiptNumber}")
        sb.appendLine("Date: $date")
        sb.appendLine("--------------------------------")
        items.forEach { item ->
            sb.appendLine("${item.productName}")
            sb.appendLine("  ${item.quantity} x ₱%.2f = ₱%.2f".format(item.productPrice, item.productPrice * item.quantity))
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("TOTAL:  ₱%.2f".format(order.totalAmount))
        if (order.paymentMethod == "CASH") {
            sb.appendLine("CASH:   ₱%.2f".format(order.amountPaid))
            sb.appendLine("CHANGE: ₱%.2f".format(order.change))
        } else {
            sb.appendLine("PAID VIA: E-Wallet")
        }
        sb.appendLine("================================")
        sb.appendLine(settings?.receiptFooter ?: "Thank you!")
        return sb.toString()
    }

    // ── Save the receipt as a PDF and return its Uri ───────────
    fun savePdf(
        context: Context,
        order: Order,
        items: List<OrderItem>,
        settings: AppSettings?
    ): Uri? {
        return try {
            val receiptDir = File(context.getExternalFilesDir(null), "receipts")
            receiptDir.mkdirs()
            val file = File(receiptDir, "receipt_${order.receiptNumber}.pdf")

            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(576, 800, 1).create() // 58mm width approx
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 18f
            }
            val bodyPaint = Paint().apply { textSize = 14f }
            val text = buildReceiptText(order, items, settings)
            var y = 40f
            text.lines().forEach { line ->
                val paint = if (line.startsWith("=") || line.startsWith("-")) bodyPaint else bodyPaint
                canvas.drawText(line, 20f, y, paint)
                y += 20f
            }

            pdfDoc.finishPage(page)
            FileOutputStream(file).use { pdfDoc.writeTo(it) }
            pdfDoc.close()

            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ── Share receipt text via any app (WhatsApp, SMS, Email) ──
    fun shareReceiptText(context: Context, receiptText: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, receiptText)
            putExtra(Intent.EXTRA_SUBJECT, "Receipt")
        }
        context.startActivity(Intent.createChooser(intent, "Send Receipt via..."))
    }

    // ── Open PDF in another app for printing ──────────────────
    fun sharePdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF with..."))
    }
}

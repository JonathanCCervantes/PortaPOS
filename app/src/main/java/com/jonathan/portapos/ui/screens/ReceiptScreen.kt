package com.jonathan.portapos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonathan.portapos.data.PortaPOSRepository
import com.jonathan.portapos.data.model.Order
import com.jonathan.portapos.data.model.OrderItem
import com.jonathan.portapos.utils.PrinterManager
import com.jonathan.portapos.utils.ReceiptUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    orderId: Long,
    repository: PortaPOSRepository,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by repository.settings.collectAsState(initial = null)

    var order by remember { mutableStateOf<Order?>(null) }
    var items by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var isPrinting by remember { mutableStateOf(false) }

    // Load the just-completed order from the database
    LaunchedEffect(orderId) {
        order = repository.getOrderById(orderId.toInt())
        items = repository.getItemsForOrder(orderId.toInt())
    }

    val receiptText = if (order != null) {
        ReceiptUtils.buildReceiptText(order!!, items, settings)
    } else ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt ${order?.receiptNumber ?: ""}") },
                actions = {
                    IconButton(onClick = onDone) { Icon(Icons.Default.Home, "Home") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Receipt preview
            Card(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text(
                    text = receiptText.ifEmpty { "Loading receipt..." },
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())
                )
            }

            if (isPrinting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Connecting to printer...", style = MaterialTheme.typography.labelSmall)
            }

            Text("Actions:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Primary Printing Actions
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        order?.let { ord ->
                            scope.launch {
                                isPrinting = true
                                PrinterManager.printReceipt(context, ord, items, settings)
                                isPrinting = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1.2f),
                    enabled = !isPrinting
                ) {
                    Icon(Icons.Default.Print, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Print Receipt")
                }

                FilledTonalButton(
                    onClick = {
                        order?.let { ord ->
                            scope.launch {
                                isPrinting = true
                                PrinterManager.printOrderSlip(context, ord, items, settings)
                                isPrinting = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isPrinting
                ) {
                    Icon(Icons.Default.Restaurant, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Order Slip")
                }
            }

            // Secondary Actions
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        order?.let { ord ->
                            scope.launch {
                                val uri = ReceiptUtils.savePdf(context, ord, items, settings)
                                if (uri != null) ReceiptUtils.sharePdf(context, uri)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(Modifier.width(4.dp))
                    Text("PDF")
                }
                OutlinedButton(
                    onClick = { ReceiptUtils.shareReceiptText(context, receiptText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }
            }

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, null)
                Spacer(Modifier.width(8.dp))
                Text("Start New Sale")
            }
        }
    }
}

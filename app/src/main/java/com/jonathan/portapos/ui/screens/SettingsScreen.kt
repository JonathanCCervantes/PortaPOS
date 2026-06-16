package com.jonathan.portapos.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonathan.portapos.data.model.AppSettings
import com.jonathan.portapos.ui.viewmodel.MainViewModel
import com.jonathan.portapos.utils.ImageUtils
import com.jonathan.portapos.utils.PrinterManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onRequirePin: (String) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val dailyOrders by viewModel.dailyOrders.collectAsState()
    val dailyTotal by viewModel.dailyTotal.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? android.app.Activity

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Daily Sales?") },
            text = { Text("This will permanently delete all order history. This action requires PIN authentication.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onRequirePin("reset")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Authenticate & Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }

    var businessName by remember(settings) { mutableStateOf(settings?.businessName ?: "") }
    var businessAddress by remember(settings) { mutableStateOf(settings?.businessAddress ?: "") }
    var businessTIN by remember(settings) { mutableStateOf(settings?.businessTIN ?: "") }
    var vatRegistered by remember(settings) { mutableStateOf(settings?.vatRegistered ?: false) }
    var receiptFooter by remember(settings) { mutableStateOf(settings?.receiptFooter ?: "") }
    var businessLogoUri by remember(settings) { mutableStateOf(settings?.businessLogoUri) }
    var qrUri by remember(settings) { mutableStateOf(settings?.qrCodeImageUri) }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val localPath = ImageUtils.saveImageToInternalStorage(context, it, "logo")
            businessLogoUri = localPath
        }
    }

    val qrPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val localPath = ImageUtils.saveImageToInternalStorage(context, it, "qr")
            qrUri = localPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Daily Sales Summary ───────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Daily Sales Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Orders Today", style = MaterialTheme.typography.labelMedium)
                                Text("${dailyOrders.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Sales", style = MaterialTheme.typography.labelMedium)
                                Text("₱%.2f".format(dailyTotal), style = MaterialTheme.typography.headlineSmall, 
                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        PrinterManager.printDailyReport(context, dailyOrders, dailyTotal, settings)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Print, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Print Report")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val csvData = buildSalesCsv(dailyOrders, dailyTotal)
                                        saveCsvFile(context, csvData)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Download CSV")
                            }
                        }

                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Reset Sales History")
                        }

                        // App Display Lock
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Button(
                            onClick = {
                                activity?.startLockTask()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.ScreenLockPortrait, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Lock App (Kiosk Mode)")
                        }
                    }
                }
            }

            // ── Business Info ──────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Business Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = businessAddress,
                            onValueChange = { businessAddress = it },
                            label = { Text("Business Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = businessTIN,
                            onValueChange = { businessTIN = it },
                            label = { Text("TIN") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = vatRegistered, onCheckedChange = { vatRegistered = it })
                            Text("VAT Registered", style = MaterialTheme.typography.bodyMedium)
                        }
                        OutlinedTextField(
                            value = receiptFooter,
                            onValueChange = { receiptFooter = it },
                            label = { Text("Receipt Footer Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text("Business Logo", style = MaterialTheme.typography.labelMedium)
                        if (businessLogoUri != null) {
                            coil.compose.AsyncImage(
                                model = businessLogoUri,
                                contentDescription = "Logo",
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        OutlinedButton(onClick = { logoPicker.launch("image/*") }) {
                            Icon(Icons.Default.Image, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (businessLogoUri == null) "Upload Logo" else "Change Logo")
                        }

                        Button(
                            onClick = {
                                viewModel.saveSettings(
                                    AppSettings(
                                        businessName = businessName,
                                        businessAddress = businessAddress,
                                        businessTIN = businessTIN,
                                        vatRegistered = vatRegistered,
                                        receiptFooter = receiptFooter,
                                        businessLogoUri = businessLogoUri,
                                        qrCodeImageUri = qrUri
                                    )
                                )
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Save Info") }
                    }
                }
            }

            // ── E-Wallet QR Code ───────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("E-Wallet QR Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Upload your GCash / Maya / bank QR code.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (qrUri != null) {
                            coil.compose.AsyncImage(
                                model = qrUri,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(120.dp)
                            )
                        }
                        OutlinedButton(onClick = { qrPicker.launch("image/*") }) {
                            Icon(Icons.Default.QrCode, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (qrUri == null) "Upload QR Code" else "Change QR Code")
                        }
                    }
                }
            }

            // ── Product List ───────────────────────────────────
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Menu Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = onAddProduct) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }

            items(products) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("₱%.2f  •  ${product.category}".format(product.price),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onEditProduct(product.id) }) {
                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.deleteProduct(product) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

private fun buildSalesCsv(orders: List<com.jonathan.portapos.data.model.Order>, total: Double): String {
    val sb = StringBuilder()
    sb.appendLine("Receipt Number,Date,Payment Method,Amount")
    val df = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    orders.forEach { order ->
        sb.appendLine("${order.receiptNumber},${df.format(java.util.Date(order.timestamp))},${order.paymentMethod},${order.totalAmount}")
    }
    sb.appendLine()
    sb.appendLine("TOTAL SALES,,,\"₱%.2f\"".format(total))
    return sb.toString()
}

private fun saveCsvFile(context: android.content.Context, csvData: String) {
    val date = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())
    val filename = "DailySales_$date.csv"
    
    try {
        val file = java.io.File(context.getExternalFilesDir(null), filename)
        file.writeText(csvData)
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Save/Share Daily Sales CSV"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

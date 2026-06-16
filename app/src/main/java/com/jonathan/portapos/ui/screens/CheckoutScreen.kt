package com.jonathan.portapos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jonathan.portapos.ui.components.Numpad
import com.jonathan.portapos.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOrderComplete: (Long) -> Unit
) {
    val total by viewModel.cartTotal.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedPayment by remember { mutableStateOf("CASH") } // "CASH" or "EWALLET"
    var cashGiven by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }

    val cashGivenDouble = cashGiven.toDoubleOrNull() ?: 0.0
    val change = cashGivenDouble - total

    // ── QR Code full-screen dialog ─────────────────────────────
    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Scan to Pay", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("₱%.2f".format(total), style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    if (settings?.qrCodeImageUri != null) {
                        coil.compose.AsyncImage(
                            model = settings!!.qrCodeImageUri,
                            contentDescription = "Payment QR",
                            modifier = Modifier.size(240.dp)
                        )
                    } else {
                        Surface(modifier = Modifier.size(240.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("QR code not set.\nGo to Settings to add your QR code.",
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showQrDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                showQrDialog = false
                                scope.launch {
                                    val orderId = viewModel.completeSale("EWALLET", total)
                                    onOrderComplete(orderId)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Payment Received ✓")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order total summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Amount Due", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("₱%.2f".format(total), style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Payment method selector
            Text("Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedPayment == "CASH",
                    onClick = { selectedPayment = "CASH" },
                    label = { Text("💵  Cash") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedPayment == "EWALLET",
                    onClick = { selectedPayment = "EWALLET" },
                    label = { Text("📱  E-Wallet QR") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Cash section
            if (selectedPayment == "CASH") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Cash Received", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "₱ ${cashGiven.ifEmpty { "0" }}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (cashGivenDouble > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (change >= 0) MaterialTheme.colorScheme.secondaryContainer
                                                else MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Change", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (change >= 0) "₱%.2f".format(change) else "Not enough ₱%.2f short".format(-change),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (change >= 0) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Numpad(
                        onNumberClick = { num ->
                            if (cashGiven.length < 9) cashGiven += num
                        },
                        onDeleteClick = {
                            if (cashGiven.isNotEmpty()) cashGiven = cashGiven.dropLast(1)
                        },
                        onClearClick = { cashGiven = "" },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        scope.launch {
                            val orderId = viewModel.completeSale("CASH", cashGivenDouble)
                            onOrderComplete(orderId)
                        }
                    },
                    enabled = change >= 0,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Payments, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Cash Payment", style = MaterialTheme.typography.titleMedium)
                }
            }

            // E-Wallet section
            if (selectedPayment == "EWALLET") {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { showQrDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Show QR Code for Customer", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

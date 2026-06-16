package com.jonathan.portapos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonathan.portapos.ui.viewmodel.MainViewModel

@Composable
fun CartSummaryPanel(
    viewModel: MainViewModel,
    onCheckout: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.cartTotal.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Current Order", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (cart.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Tap items to add them here", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart) { cartItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cartItem.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("₱%.2f each".format(cartItem.product.price), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.removeFromCart(cartItem.product) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, "Remove", modifier = Modifier.size(16.dp))
                            }
                            Text("${cartItem.quantity}", style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = { viewModel.addToCart(cartItem.product) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, "Add", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("₱%.2f".format(total), style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = cart.isNotEmpty()
        ) {
            Text("Checkout", style = MaterialTheme.typography.titleMedium)
        }
    }
}

package com.jonathan.portapos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonathan.portapos.ui.components.CartSummaryPanel
import com.jonathan.portapos.ui.components.ProductCard
import com.jonathan.portapos.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    isTablet: Boolean,
    viewModel: MainViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onUnlock: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    // Check if app is pinned (kiosk mode)
    val isLocked = remember(activity) {
        val am = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        am.lockTaskModeState != android.app.ActivityManager.LOCK_TASK_MODE_NONE
    }

    var selectedCategory by remember { mutableStateOf("All") }

    val displayedProducts = if (selectedCategory == "All") products
                            else products.filter { it.category == selectedCategory }

    if (isTablet) {
        // ── TABLET LAYOUT: side-by-side ─────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            // Left side: menu
            Column(modifier = Modifier.weight(1.6f).fillMaxHeight()) {
                MenuTopBar(
                    cartCount = cartCount, 
                    onCart = onNavigateToCart, 
                    onSettings = onNavigateToSettings, 
                    showCartBadge = false,
                    isLocked = isLocked,
                    onUnlock = onUnlock
                )
                CategoryFilterRow(
                    categories = listOf("All") + categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
                ProductGrid(
                    products = displayedProducts,
                    onAddToCart = { viewModel.addToCart(it) },
                    columns = 3
                )
            }
            // Right side: cart panel
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                CartSummaryPanel(
                    viewModel = viewModel,
                    onCheckout = onNavigateToCart // on tablet, cart IS the right panel
                )
            }
        }
    } else {
        // ── PHONE LAYOUT: full screen menu + floating cart button ──
        Scaffold(
            topBar = {
                MenuTopBar(
                    cartCount = cartCount, 
                    onCart = onNavigateToCart, 
                    onSettings = onNavigateToSettings, 
                    showCartBadge = true,
                    isLocked = isLocked,
                    onUnlock = onUnlock
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                CategoryFilterRow(
                    categories = listOf("All") + categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
                ProductGrid(
                    products = displayedProducts,
                    onAddToCart = { viewModel.addToCart(it) },
                    columns = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuTopBar(
    cartCount: Int,
    onCart: () -> Unit,
    onSettings: () -> Unit,
    showCartBadge: Boolean,
    isLocked: Boolean,
    onUnlock: () -> Unit
) {
    TopAppBar(
        title = { Text("PortaPOS", fontWeight = FontWeight.Bold) },
        actions = {
            if (isLocked) {
                IconButton(onClick = onUnlock) {
                    Icon(Icons.Default.LockOpen, "Unlock App", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                if (showCartBadge) {
                    BadgedBox(badge = {
                        if (cartCount > 0) Badge { Text(cartCount.toString()) }
                    }) {
                        IconButton(onClick = onCart) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    }
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }
        }
    )
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<com.jonathan.portapos.data.model.Product>,
    onAddToCart: (com.jonathan.portapos.data.model.Product) -> Unit,
    columns: Int
) {
    if (products.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🍽️", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(8.dp))
                Text("No items yet — add some in Settings!", style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(product = product, onAddToCart = onAddToCart)
            }
        }
    }
}

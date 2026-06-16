package com.jonathan.portapos.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jonathan.portapos.data.model.Product
import com.jonathan.portapos.ui.viewmodel.MainViewModel
import com.jonathan.portapos.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val existingProduct = products.find { it.id == productId }
    val isEditing = productId != 0
    val context = LocalContext.current

    var name by remember(existingProduct) { mutableStateOf(existingProduct?.name ?: "") }
    var price by remember(existingProduct) { mutableStateOf(existingProduct?.price?.toString() ?: "") }
    var category by remember(existingProduct) { mutableStateOf(existingProduct?.category ?: "") }
    var photoUri by remember(existingProduct) { mutableStateOf(existingProduct?.photoUri) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val localPath = ImageUtils.saveImageToInternalStorage(context, it, "prod")
            photoUri = localPath
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Item" else "New Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp)
                .fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo picker
            Card(
                onClick = { photoPicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (photoUri != null) {
                        coil.compose.AsyncImage(
                            model = photoUri,
                            contentDescription = "Product Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tap to add photo", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Item Name *") },
                isError = nameError,
                supportingText = { if (nameError) Text("Name is required") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it; priceError = false },
                label = { Text("Price (₱) *") },
                isError = priceError,
                supportingText = { if (priceError) Text("Enter a valid price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (e.g. Meals, Drinks)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    nameError = name.isBlank()
                    priceError = price.toDoubleOrNull() == null
                    if (!nameError && !priceError) {
                        viewModel.saveProduct(
                            Product(
                                id = if (isEditing) productId else 0,
                                name = name.trim(),
                                price = price.toDouble(),
                                category = category.trim().ifEmpty { "General" },
                                photoUri = photoUri
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Save Changes" else "Add to Menu", style = MaterialTheme.typography.titleMedium)
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = { existingProduct?.let { viewModel.deleteProduct(it) }; onBack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete This Item")
                }
            }
        }
    }
}

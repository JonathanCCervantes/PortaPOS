package com.jonathan.portapos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jonathan.portapos.ui.components.Numpad

@Composable
fun PinEntryScreen(
    purpose: String = "login",
    onCorrectPin: () -> Unit
) {
    val correctPin = "095615"
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val title = when (purpose) {
        "reset" -> "Authorize Reset"
        "unlock" -> "Unlock App"
        else -> "Enter Security PIN"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (purpose == "login") Icons.Default.Lock else Icons.Default.AdminPanelSettings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (purpose == "reset") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        // Visual feedback for PIN length
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(6) { index ->
                val filled = index < enteredPin.length
                Surface(
                    modifier = Modifier.size(16.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = if (filled) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }
        }

        Spacer(Modifier.height(32.dp))

        if (showError) {
            Text(
                "Incorrect PIN. Try again.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
        }

        Numpad(
            onNumberClick = { num ->
                if (enteredPin.length < 6) {
                    enteredPin += num
                    showError = false
                    if (enteredPin.length == 6) {
                        if (enteredPin == correctPin) {
                            onCorrectPin()
                        } else {
                            showError = true
                            enteredPin = ""
                        }
                    }
                }
            },
            onDeleteClick = {
                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
            },
            onClearClick = { enteredPin = "" },
            modifier = Modifier.widthIn(max = 300.dp)
        )
    }
}

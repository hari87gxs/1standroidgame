package com.athreya.mathworkout.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Your Player Name") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Please enter your name before joining groups.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { 
                        playerName = it
                        showError = false
                    },
                    label = { Text("Player Name") },
                    placeholder = { Text("Enter your name") },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Name must be 2-20 characters") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = playerName.trim()
                    if (trimmed.length in 2..20) {
                        onConfirm(trimmed)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.athreya.mathworkout.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

/**
 * Dialog for user registration - capturing player name
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationDialog(
    onRegister: (String) -> Unit,
    onDismiss: () -> Unit,
    onCheckUsername: (String) -> Unit,
    isCheckingUsername: Boolean = false,
    usernameAvailable: Boolean? = null,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var showUsernameCheck by remember { mutableStateOf(false) }
    
    // Auto-check username availability when user stops typing
    LaunchedEffect(username) {
        if (username.length >= 3) {
            delay(500) // Debounce
            if (username.isNotBlank()) {
                showUsernameCheck = true
                onCheckUsername(username)
            }
        } else {
            showUsernameCheck = false
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Join Global Leaderboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Choose a unique player name to compete with players worldwide!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Username input
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        if (it.length <= 20) { // Limit username length
                            username = it.trim()
                        }
                    },
                    label = { Text("Player Name") },
                    placeholder = { Text("Enter your name...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (username.length >= 3 && usernameAvailable == true) {
                                onRegister(username)
                            }
                        }
                    ),
                    supportingText = {
                        when {
                            username.length < 3 && username.isNotEmpty() -> {
                                Text(
                                    text = "Name must be at least 3 characters",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            showUsernameCheck && isCheckingUsername -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Checking availability...")
                                }
                            }
                            showUsernameCheck && usernameAvailable == true -> {
                                Text(
                                    text = "✓ Name available!",
                                    color = Color.Green
                                )
                            }
                            showUsernameCheck && usernameAvailable == false -> {
                                Text(
                                    text = "✗ Name already taken",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {
                                Text("3-20 characters, letters and numbers only")
                            }
                        }
                    },
                    isError = (username.length < 3 && username.isNotEmpty()) || 
                              (showUsernameCheck && usernameAvailable == false),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Later")
                    }
                    
                    Button(
                        onClick = { onRegister(username) },
                        enabled = username.length >= 3 && 
                                 usernameAvailable == true && 
                                 !isCheckingUsername,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Register")
                    }
                }
                
                // Info text
                Text(
                    text = "You can change your name later in settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.viewmodel.SettingsViewModel

/**
 * SettingsScreen Composable - Allows users to configure game settings.
 * 
 * This screen demonstrates several important Compose concepts:
 * - State observation with collectAsState()
 * - Radio button groups
 * - Material Design components
 * 
 * @param onBackClick Callback function called when back button is pressed
 * @param viewModel ViewModel that manages settings state and business logic
 * @param modifier Optional modifier for customizing appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // Observe the settings state from the ViewModel
    // collectAsState() converts a Flow to Compose State
    // This automatically recomposes the UI when settings change
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Difficulty Selection Section
            SettingsSection(title = "Difficulty") {
                // selectableGroup() for accessibility - screen readers understand this is a group
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create radio buttons for each difficulty option
                    viewModel.getDifficultyOptions().forEach { difficulty ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (difficulty == uiState.difficulty),
                                    onClick = { viewModel.updateDifficulty(difficulty) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (difficulty == uiState.difficulty),
                                onClick = null // onClick is handled by the Row's selectable modifier
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = difficulty.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getDifficultyDescription(difficulty),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Question Count Selection Section
            SettingsSection(title = "Number of Questions") {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create radio buttons for each question count option
                    viewModel.getQuestionCountOptions().forEach { count ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (count == uiState.questionCount),
                                    onClick = { viewModel.updateQuestionCount(count) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (count == uiState.questionCount),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "$count questions",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable composable for settings sections.
 * This provides consistent styling for different setting groups.
 * 
 * @param title The section title
 * @param content The content to display in this section
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

/**
 * Helper function to get description text for difficulty levels.
 * 
 * @param difficulty The difficulty level
 * @return Description string explaining what this difficulty includes
 */
private fun getDifficultyDescription(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.EASY -> "Numbers 1-10, simple operations"
        Difficulty.MEDIUM -> "Numbers 1-100, moderate complexity"
        Difficulty.COMPLEX -> "Numbers 1-1000, advanced problems"
    }
}

/**
 * Preview for the Settings screen.
 */
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        // Note: In a real preview, we'd need to provide a mock ViewModel
        // For now, this shows the structure
    }
}
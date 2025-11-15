package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
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
    onThemesClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    achievementManager: com.athreya.mathworkout.data.AchievementManager? = null,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Themes Button Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Button(
                    onClick = onThemesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🎨 Customize Theme",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Achievements Button Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Button(
                    onClick = onAchievementsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🏆 View Achievements",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Reset Achievements Button (for testing)
            if (achievementManager != null) {
                var showResetDialog by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "🔄 Reset All Achievements (Testing)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset All Achievements?") },
                        text = { Text("This will reset all your achievements and statistics. This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    achievementManager.resetAllAchievements()
                                    showResetDialog = false
                                }
                            ) {
                                Text("Reset")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
            
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
            
            // Scoring System Information Section
            SettingsSection(title = "Scoring System") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Complete Scoring Formula:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "1. Base Points = Correct × Difficulty",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "2. Points with Speed = Base × Time Multiplier",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "3. Points after Penalties = Speed Points - (Errors × 5)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "4. Final Score = Penalty Points × Streak Multiplier",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = "Breakdown:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "• Difficulty Points:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "  - Easy: 10 pts/question",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "  - Medium: 20 pts/question",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "  - Hard: 30 pts/question",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    
                    Text(
                        text = "• Time Multiplier (based on total game time):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "  - ≤1s per question: 3× multiplier",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "    (e.g., ≤10s for 10 questions, ≤20s for 20 questions)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "  - ≤1.2s per question: 2× multiplier",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "    (e.g., ≤12s for 10 questions, ≤24s for 20 questions)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "  - >1.2s per question: 1× multiplier",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    
                    Text(
                        text = "• Penalties: -5 points per wrong answer",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "Daily Challenge Streak Bonuses:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "Complete the daily challenge to unlock streak bonuses for ALL games!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "• 3+ day streak: 1.5× multiplier",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 7+ day streak: 2.0× multiplier",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 14+ day streak: 2.5× multiplier",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 30+ day streak: 3.0× multiplier",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "💡 Tip: Build your daily streak to get bonus multipliers on every game you play!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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
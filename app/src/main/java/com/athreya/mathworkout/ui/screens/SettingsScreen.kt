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
            
            // How to Play - Game Modes
            SettingsSection(title = "📚 How to Play") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Practice Mode
                    GameModeGuide(
                        icon = "🎯",
                        title = "Practice Mode",
                        description = "Play solo to improve your skills and climb the rankings!",
                        steps = listOf(
                            "Choose a game mode (Addition, Subtraction, etc.)",
                            "Select difficulty (Easy/Medium/Hard)",
                            "Answer questions as fast and accurately as possible",
                            "Score = Correct answers × Speed × Difficulty",
                            "Your best scores are saved in High Scores"
                        )
                    )
                    
                    Divider()
                    
                    // Daily Challenge
                    GameModeGuide(
                        icon = "📅",
                        title = "Daily Challenge",
                        description = "Complete the daily puzzle to build streaks and unlock bonuses!",
                        steps = listOf(
                            "New challenge available every day at midnight",
                            "Complete it to maintain your streak",
                            "Streak bonuses: 3 days (1.5×), 7 days (2×), 14 days (2.5×), 30 days (3×)",
                            "Streak multiplier applies to ALL games you play",
                            "Miss a day and your streak resets to 0"
                        )
                    )
                    
                    Divider()
                    
                    // Groups
                    GameModeGuide(
                        icon = "👥",
                        title = "Groups",
                        description = "Join or create groups to compete with friends!",
                        steps = listOf(
                            "Create a group and share the code with friends",
                            "Or join an existing group using their code",
                            "Play any game mode and your scores automatically count",
                            "Group leaderboard shows rankings based on average score",
                            "Compete to reach the top of your group!"
                        )
                    )
                    
                    Divider()
                    
                    // Challenges
                    GameModeGuide(
                        icon = "⚔️",
                        title = "Player Challenges",
                        description = "Challenge other players to head-to-head battles!",
                        steps = listOf(
                            "Send a challenge to any player in your groups",
                            "Choose game mode, difficulty, and question count",
                            "They play first, then you try to beat their score",
                            "Whoever scores higher wins the challenge",
                            "View challenge history in the Challenges tab"
                        )
                    )
                    
                    Divider()
                    
                    // Global Leaderboard
                    GameModeGuide(
                        icon = "🌍",
                        title = "Global Leaderboard",
                        description = "Compete with players worldwide!",
                        steps = listOf(
                            "Register with a unique username",
                            "Play any game to submit scores globally",
                            "Filter by game mode and difficulty",
                            "See your global rank and top players",
                            "Your badges are displayed next to your name"
                        )
                    )
                    
                    Divider()
                    
                    // Interactive Games
                    GameModeGuide(
                        icon = "🎮",
                        title = "Interactive Games",
                        description = "Learn through fun, engaging activities!",
                        steps = listOf(
                            "Sudoku: Fill the grid with 1-9 (each row, column, box)",
                            "Daily Riddle: Solve a new math riddle every day",
                            "Math Tricks Library: Learn calculation shortcuts",
                            "Practice tricks with guided examples",
                            "All games count toward achievements!"
                        )
                    )
                }
            }
            
            // Achievements & Badges System
            SettingsSection(title = "🏆 Achievements & Badges") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Unlock badges by completing achievements!",
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🏅 Badge Categories:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Speed: Complete games quickly",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• Accuracy: Get perfect scores",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• Collection: Complete all game modes",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• Challenge: Win player battles",
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• Dedication: Build daily streaks",
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    Text(
                        text = "🌟 Badge Rarity Levels:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    RarityBadge("🥉 Bronze", "Common achievements")
                    RarityBadge("🥈 Silver", "Challenging achievements")
                    RarityBadge("🥇 Gold", "Difficult achievements")
                    RarityBadge("💎 Platinum", "Very rare achievements")
                    RarityBadge("💠 Diamond", "Ultimate achievements")
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Text(
                        text = "📍 Where Your Badges Appear:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "• Home screen (below your name)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Global leaderboard (next to your entry)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Group leaderboard (your entry only)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Challenge screens (when challenging others)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "💡 Tip: Check the Achievements screen to see your progress and unlock more badges!",
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
 * Composable for displaying a game mode guide
 */
@Composable
private fun GameModeGuide(
    icon: String,
    title: String,
    description: String,
    steps: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = description,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(
            modifier = Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            steps.forEach { step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = step,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Composable for displaying badge rarity information
 */
@Composable
private fun RarityBadge(
    rarity: String,
    description: String
) {
    Row(
        modifier = Modifier.padding(start = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rarity,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "- $description",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
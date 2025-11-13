package com.athreya.mathworkout.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.viewmodel.HighScoreViewModel

/**
 * ResultsScreen Composable - Shows game results and allows saving high scores.
 * 
 * This screen demonstrates:
 * - Displaying calculated results
 * - Conditional UI based on achievements
 * - Integration with database operations
 * 
 * @param gameMode The game mode that was played
 * @param difficulty The difficulty level that was played
 * @param wrongAttempts Number of wrong answers during the game
 * @param totalTime Total time including penalties (in milliseconds)
 * @param onViewHighScores Callback to navigate to high scores
 * @param onHomeClick Callback to navigate to home screen
 * @param viewModel ViewModel for managing high score operations
 * @param modifier Optional modifier for customizing appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    gameMode: GameMode,
    difficulty: String,
    wrongAttempts: Int,
    totalTime: Long,
    onViewHighScores: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: HighScoreViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // Calculate various time metrics
    val timePenalty = wrongAttempts * 5000L // 5 seconds per wrong answer
    val actualGameTime = totalTime - timePenalty
    
    // Check if this is a new record
    var isNewRecord by remember { mutableStateOf(false) }
    
    // Save the high score when the screen loads
    LaunchedEffect(gameMode, difficulty, totalTime, wrongAttempts) {
        // Check if it's a new record
        isNewRecord = viewModel.isNewRecord(gameMode, difficulty, totalTime)
        
        // Save the score to database
        viewModel.saveHighScore(gameMode, difficulty, totalTime, wrongAttempts)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Congratulations message
            Text(
                text = if (isNewRecord) "🎉 New Record! 🎉" else "Great Job!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isNewRecord) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Results card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Game mode and difficulty
                    Text(
                        text = getGameModeTitle(gameMode),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Difficulty: ${difficulty.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Divider()
                    
                    // Time metrics
                    ResultRow(
                        label = "Game Time:",
                        value = viewModel.formatTime(actualGameTime)
                    )
                    
                    if (wrongAttempts > 0) {
                        ResultRow(
                            label = "Wrong Attempts:",
                            value = wrongAttempts.toString()
                        )
                        
                        ResultRow(
                            label = "Time Penalty:",
                            value = viewModel.formatTime(timePenalty),
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Divider()
                    
                    // Final score
                    ResultRow(
                        label = "Final Score:",
                        value = viewModel.formatTime(totalTime),
                        labelStyle = MaterialTheme.typography.titleMedium,
                        valueStyle = MaterialTheme.typography.titleMedium,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // View High Scores button
                OutlinedButton(
                    onClick = onViewHighScores,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("High Scores")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Home button
                Button(
                    onClick = onHomeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Home")
                }
            }
        }
    }
}

/**
 * Reusable composable for displaying result rows.
 * 
 * @param label The label text (e.g., "Game Time:")
 * @param value The value text (e.g., "1m 23s")
 * @param labelStyle Typography style for the label
 * @param valueStyle Typography style for the value
 * @param valueColor Color for the value text
 */
@Composable
private fun ResultRow(
    label: String,
    value: String,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle
        )
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Helper function to get display title for each game mode.
 */
private fun getGameModeTitle(gameMode: GameMode): String {
    return when (gameMode) {
        GameMode.ADDITION_SUBTRACTION -> "Addition & Subtraction"
        GameMode.MULTIPLICATION_DIVISION -> "Multiplication & Division"
        GameMode.TEST_ME -> "Test Me"
        GameMode.BRAIN_TEASER -> "Brain Teaser"
        GameMode.SUDOKU -> "Sudoku"
    }
}

/**
 * Preview for the Results screen.
 */
@Preview(showBackground = true)
@Composable
private fun ResultsScreenPreview() {
    MaterialTheme {
        ResultsScreen(
            gameMode = GameMode.ADDITION_SUBTRACTION,
            difficulty = "EASY",
            wrongAttempts = 2,
            totalTime = 65000L,
            onViewHighScores = { },
            onHomeClick = { }
        )
    }
}
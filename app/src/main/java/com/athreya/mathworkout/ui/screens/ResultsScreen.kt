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
import com.athreya.mathworkout.ui.components.AnimatedCounter
import com.athreya.mathworkout.ui.components.ConfettiAnimation
import com.athreya.mathworkout.ui.components.ProgressMessageCard
import com.athreya.mathworkout.ui.components.generateProgressMessages
import kotlinx.coroutines.launch

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
 * @param isDailyChallenge Whether this was a daily challenge game
 * @param onViewHighScores Callback to navigate to high scores
 * @param onHomeClick Callback to navigate to home screen
 * @param viewModel ViewModel for managing high score operations
 * @param dailyChallengeViewModel ViewModel for managing daily challenges
 * @param modifier Optional modifier for customizing appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    gameMode: GameMode,
    difficulty: String,
    wrongAttempts: Int,
    totalTime: Long,
    questionsAnswered: Int,
    isDailyChallenge: Boolean = false,
    onViewHighScores: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: HighScoreViewModel = viewModel(),
    dailyChallengeViewModel: com.athreya.mathworkout.viewmodel.DailyChallengeViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Calculate various time metrics
    val timePenalty = wrongAttempts * 5000L // 5 seconds per wrong answer
    val actualGameTime = totalTime - timePenalty
    
    // Calculate points and scores
    val correctAnswers = questionsAnswered - wrongAttempts
    
    // Calculate actual base points (before any multipliers)
    val difficultyPoints = when (difficulty) {
        "Easy" -> 10
        "Medium" -> 20
        "Hard" -> 30
        else -> 10
    }
    val actualBasePoints = correctAnswers * difficultyPoints
    
    // Get time multiplier
    val timeMultiplier = remember(questionsAnswered, totalTime) {
        com.athreya.mathworkout.data.HighScore.getTimeMultiplier(questionsAnswered, totalTime)
    }
    
    // Calculate points after speed multiplier
    val pointsAfterSpeed = (actualBasePoints * timeMultiplier).toInt()
    
    // Apply penalties
    val wrongPenalty = wrongAttempts * 5
    val pointsAfterPenalties = maxOf(0, pointsAfterSpeed - wrongPenalty)
    
    // This will be used for saving to database (includes time multiplier and penalties)
    val basePoints = remember(questionsAnswered, correctAnswers, wrongAttempts, totalTime, difficulty) {
        com.athreya.mathworkout.data.HighScore.calculatePoints(
            questionsAnswered, correctAnswers, wrongAttempts, totalTime, difficulty
        )
    }
    
    var bonusMultiplier by remember { mutableStateOf(1.0f) }
    var finalScore by remember { mutableStateOf(basePoints) }
    var isNewRecord by remember { mutableStateOf(false) }
    
    // Statistics for progress messages
    var averageScore by remember { mutableStateOf(0) }
    var bestScore by remember { mutableStateOf(0) }
    var averageTime by remember { mutableStateOf(0f) }
    var bestTime by remember { mutableStateOf(Float.MAX_VALUE) }
    var progressMessages by remember { mutableStateOf<List<com.athreya.mathworkout.ui.components.ProgressMessage>>(emptyList()) }
    var currentMessageIndex by remember { mutableStateOf(0) }
    var streakDays by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    // Save the high score and generate progress messages when the screen loads
    LaunchedEffect(gameMode, difficulty, totalTime, wrongAttempts, isDailyChallenge) {
        try {
            // Fetch statistics first
            averageScore = viewModel.getAverageScore(gameMode, difficulty)
            bestScore = viewModel.getBestScoreValue(gameMode, difficulty)
            averageTime = viewModel.getAverageTime(gameMode, difficulty)
            bestTime = viewModel.getBestTime(gameMode, difficulty)
            
            // Always get streak multiplier (applies to all games if daily challenge completed)
            if (dailyChallengeViewModel != null) {
                bonusMultiplier = dailyChallengeViewModel.getStreakMultiplier()
                streakDays = dailyChallengeViewModel.getCurrentStreak()
            }
            
            finalScore = com.athreya.mathworkout.data.HighScore.applyBonus(basePoints, bonusMultiplier)
            
            // Check if it's a new record (based on score, not time)
            isNewRecord = viewModel.isNewRecord(gameMode, difficulty, finalScore)
            
            // Generate progress messages (now with correct statistics)
            progressMessages = generateProgressMessages(
                score = finalScore,
                averageScore = averageScore,
                bestScore = bestScore,
                timeInSeconds = totalTime / 1000f,
                averageTime = averageTime,
                bestTime = bestTime,
                wrongAttempts = wrongAttempts,
                timeMultiplier = timeMultiplier,
                streakDays = streakDays,
                isNewRecord = isNewRecord
            )
            
            // Save the score to database with points calculation
            viewModel.saveHighScoreWithPoints(
                gameMode = gameMode,
                difficulty = difficulty,
                timeTaken = totalTime,
                wrongAttempts = wrongAttempts,
                questionsAnswered = questionsAnswered,
                isDailyChallenge = isDailyChallenge,
                bonusMultiplier = bonusMultiplier
            )
            
            // If it's a daily challenge, mark it as complete
            if (isDailyChallenge && dailyChallengeViewModel != null) {
                dailyChallengeViewModel.completeDailyChallenge(
                    timeTaken = totalTime,
                    wrongAttempts = wrongAttempts,
                    questionsAnswered = questionsAnswered
                )
            }
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Confetti animation for new records
            if (isNewRecord) {
                ConfettiAnimation(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 100
                )
            }
            
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Progress messages at the top
                if (progressMessages.isNotEmpty() && currentMessageIndex < progressMessages.size) {
                    ProgressMessageCard(
                        message = progressMessages[currentMessageIndex],
                        onDismiss = {
                            if (currentMessageIndex < progressMessages.size - 1) {
                                currentMessageIndex++
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
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
                    
                    if (isDailyChallenge) {
                        Text(
                            text = "⭐ Daily Challenge ⭐",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Divider()
                    
                    // Score breakdown
                    ResultRow(
                        label = "Base Points:",
                        value = "$actualBasePoints (${correctAnswers}×${difficultyPoints})",
                        animated = true,
                        animatedValue = actualBasePoints
                    )
                    
                    ResultRow(
                        label = "Time Multiplier:",
                        value = "${timeMultiplier}x",
                        valueColor = if (timeMultiplier > 1.0f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (wrongAttempts > 0) {
                        ResultRow(
                            label = "Penalties:",
                            value = "-${wrongPenalty}",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    ResultRow(
                        label = "Points after Speed:",
                        value = pointsAfterSpeed.toString(),
                        valueColor = MaterialTheme.colorScheme.primary,
                        animated = true,
                        animatedValue = pointsAfterSpeed
                    )
                    
                    if (bonusMultiplier > 1.0f) {
                        ResultRow(
                            label = "Streak Bonus:",
                            value = "${bonusMultiplier}x",
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Time metrics
                    ResultRow(
                        label = "Time:",
                        value = viewModel.formatTime(totalTime)
                    )
                    
                    Divider()
                    
                    // Final score with animation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Final Score:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        AnimatedCounter(
                            count = finalScore,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            duration = 2000 // 2 second animation
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
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
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    animated: Boolean = false,
    animatedValue: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor
        )
        
        if (animated && animatedValue > 0) {
            AnimatedCounter(
                count = animatedValue,
                style = valueStyle.copy(fontWeight = FontWeight.Medium),
                color = valueColor,
                duration = 1500,
                suffix = if (value.contains("(")) " ${value.substringAfter(" ")}" else ""
            )
        } else {
            Text(
                text = value,
                style = valueStyle,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
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
            questionsAnswered = 10,
            onViewHighScores = { },
            onHomeClick = { }
        )
    }
}
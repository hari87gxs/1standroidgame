package com.athreya.mathworkout.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athreya.mathworkout.data.GameDifficulty
import com.athreya.mathworkout.data.GameType
import com.athreya.mathworkout.data.MathGame
import com.athreya.mathworkout.data.MathGames
import com.athreya.mathworkout.data.UserPreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    gameType: GameType,
    difficulty: GameDifficulty,
    onBackClick: () -> Unit,
    userPreferencesManager: UserPreferencesManager
) {
    val totalQuestions = 10
    val games = remember {
        List(totalQuestions) {
            when (gameType) {
                GameType.NUMBER_PUZZLE -> MathGames.generateNumberPuzzle(difficulty)
                GameType.PATTERN_MATCHING -> MathGames.generatePatternMatching(difficulty)
                GameType.STORY_PROBLEM -> MathGames.generateStoryProblem(difficulty)
                else -> MathGames.generateNumberPuzzle(difficulty)
            }
        }
    }
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var showHint by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    
    val currentGame = games[currentQuestionIndex]
    val gameTitle = when (gameType) {
        GameType.NUMBER_PUZZLE -> "Number Puzzles 🧩"
        GameType.PATTERN_MATCHING -> "Pattern Matching 🔢"
        GameType.STORY_PROBLEM -> "Story Problems 📖"
        else -> "Math Games"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gameTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isCompleted && !showFeedback) {
                        IconButton(
                            onClick = { showHint = !showHint }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = if (showHint) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (isCompleted) {
            // Completion screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (score == totalQuestions) "🎉" else if (score >= totalQuestions * 0.7) "🌟" else "👍",
                    fontSize = 100.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (score == totalQuestions) "Perfect Score!" else if (score >= totalQuestions * 0.7) "Great Job!" else "Good Effort!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Score",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "$score / $totalQuestions",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("Exit", fontSize = 16.sp)
                    }
                    
                    Button(
                        onClick = {
                            currentQuestionIndex = 0
                            selectedAnswer = null
                            showFeedback = false
                            score = 0
                            showHint = false
                            isCompleted = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("Play Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Game play screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Score: $score",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            
                            Text(
                                text = "${difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                // Question card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentGame.question,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 36.sp
                            )
                        }
                    }
                }
                
                // Hint
                if (showHint && currentGame.hint.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = currentGame.hint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Answer choices
                item {
                    Text(
                        text = "Choose your answer:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(currentGame.choices) { choice ->
                    val isSelected = selectedAnswer == choice
                    val isCorrect = showFeedback && choice == currentGame.correctAnswer
                    val isWrong = showFeedback && isSelected && choice != currentGame.correctAnswer
                    
                    Card(
                        onClick = {
                            if (!showFeedback) {
                                selectedAnswer = choice
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCorrect -> MaterialTheme.colorScheme.primaryContainer
                                isWrong -> MaterialTheme.colorScheme.errorContainer
                                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isSelected && !showFeedback) {
                            CardDefaults.outlinedCardBorder()
                        } else null,
                        enabled = !showFeedback
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = choice,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            
                            if (showFeedback) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else if (isWrong) Icons.Default.Cancel else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = when {
                                        isCorrect -> MaterialTheme.colorScheme.primary
                                        isWrong -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Feedback
                if (showFeedback) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedAnswer == currentGame.correctAnswer) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (selectedAnswer == currentGame.correctAnswer) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (selectedAnswer == currentGame.correctAnswer) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                    Text(
                                        text = if (selectedAnswer == currentGame.correctAnswer) "Correct! 🎉" else "Not quite! 🤔",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                if (currentGame.explanation.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentGame.explanation,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!showFeedback) {
                            Button(
                                onClick = {
                                    showFeedback = true
                                    if (selectedAnswer == currentGame.correctAnswer) {
                                        score++
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = selectedAnswer != null
                            ) {
                                Text("Check Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentQuestionIndex < totalQuestions - 1) {
                                        currentQuestionIndex++
                                        selectedAnswer = null
                                        showFeedback = false
                                        showHint = false
                                    } else {
                                        // Award XP
                                        val xpEarned = 10 + (score * 2)
                                        userPreferencesManager.addXP(xpEarned)
                                        isCompleted = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    if (currentQuestionIndex < totalQuestions - 1) "Next Question" else "Finish",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

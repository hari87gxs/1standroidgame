package com.athreya.mathworkout.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.viewmodel.GameViewModel
import kotlinx.coroutines.delay

/**
 * GameScreen Composable - The main game interface where users solve math problems.
 * 
 * This screen demonstrates:
 * - Complex state management with ViewModels
 * - Text input handling
 * - Keyboard interactions
 * - Progress indicators
 * 
 * @param gameMode The type of math problems to generate
 * @param onGameComplete Callback called when the game is completed
 * @param onBackClick Callback called when back button is pressed
 * @param viewModel ViewModel that manages game state and logic
 * @param modifier Optional modifier for customizing appearance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameMode: GameMode,
    onGameComplete: (GameMode, String, Int, Long, Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: GameViewModel = viewModel(),
    isDailyChallenge: Boolean = false,
    challengeId: String? = null,
    modifier: Modifier = Modifier
) {
    // Observe the game state
    val uiState by viewModel.uiState.collectAsState()
    
    // Focus management for the input field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Shake animation state for wrong answers
    var triggerShake by remember { mutableStateOf(false) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (triggerShake) 0f else 1f,
        animationSpec = if (triggerShake) {
            repeatable(
                iterations = 3,
                animation = tween(durationMillis = 50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(durationMillis = 0)
        },
        finishedListener = { triggerShake = false },
        label = "shake"
    )
    
    // Pop animation for correct answers
    var triggerPop by remember { mutableStateOf(false) }
    val popScale by animateFloatAsState(
        targetValue = if (triggerPop) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { 
            if (it == 1.2f) {
                triggerPop = false
            }
        },
        label = "pop"
    )
    
    // Watch for wrong attempts to trigger shake
    LaunchedEffect(uiState.wrongAttempts) {
        if (uiState.wrongAttempts > 0) {
            triggerShake = true
        }
    }
    
    // Initialize the game when this composable is first created
    LaunchedEffect(gameMode, isDailyChallenge) {
        viewModel.initializeGame(gameMode, isDailyChallenge)
    }
    
    // Navigate to results when game is complete
    LaunchedEffect(uiState.gameCompleted, uiState.gameSessionId) {
        // Only navigate to results when:
        // 1. Game is explicitly completed 
        // 2. We have a valid game session ID (not empty)
        // 3. The session ID is from this game instance
        if (uiState.gameCompleted && uiState.gameSessionId.isNotEmpty()) {
            // Game completed, navigate to results
            onGameComplete(
                gameMode,
                uiState.difficulty.name,
                uiState.wrongAttempts,
                viewModel.getFinalScore(),
                uiState.totalQuestions
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(getGameModeTitle(gameMode))
                },
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
        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Progress indicator showing current question number
                LinearProgressIndicator(
                    progress = uiState.questionNumber.toFloat() / uiState.totalQuestions.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // Question counter
                Text(
                    text = "Question ${uiState.questionNumber} of ${uiState.totalQuestions}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Current question
                uiState.currentQuestion?.let { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                            .graphicsLayer {
                                translationX = if (triggerShake) (shakeOffset * 20f - 10f) else 0f
                            }
                            .scale(popScale),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = question.question,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
                
                // Answer input field with auto-submission
                uiState.currentQuestion?.let { question ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = uiState.userAnswer,
                            onValueChange = viewModel::updateUserAnswer,
                            label = { Text("Your Answer") },
                            placeholder = { Text("Enter ${question.expectedDigits} digit${if (question.expectedDigits > 1) "s" else ""}") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (uiState.userAnswer.isNotBlank()) {
                                        viewModel.submitAnswer()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .focusRequester(focusRequester),
                            singleLine = true,
                            supportingText = {
                                Text(
                                    text = "Answer will submit automatically",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        
                        // Optional manual submit for edge cases
                        if (uiState.userAnswer.isNotBlank() && uiState.userAnswer.length != uiState.currentQuestion?.expectedDigits) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = { 
                                    viewModel.submitAnswer()
                                }
                            ) {
                                Text(
                                    "Submit Answer",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                // Wrong attempts counter
                if (uiState.wrongAttempts > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Wrong attempts: ${uiState.wrongAttempts}",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // Request focus on the input field when the question changes
    LaunchedEffect(uiState.currentQuestion) {
        if (!uiState.isLoading && uiState.currentQuestion != null) {
            focusRequester.requestFocus()
        }
    }
}

/**
 * Helper function to get display title for each game mode.
 * 
 * @param gameMode The game mode
 * @return Human-readable title for the game mode
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
 * Preview for the Game screen.
 */
@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    MaterialTheme {
        // Note: In a real preview, we'd need to provide a mock ViewModel
        // For now, this shows the structure
    }
}
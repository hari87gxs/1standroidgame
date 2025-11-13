package com.athreya.mathworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.GameSettings
import com.athreya.mathworkout.data.SettingsManager
import com.athreya.mathworkout.game.MathQuestion
import com.athreya.mathworkout.game.QuestionGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Data class representing the current state of the game.
 * This includes all the information needed to display the game UI.
 */
data class GameUiState(
    val currentQuestion: MathQuestion? = null,
    val questionNumber: Int = 0,
    val totalQuestions: Int = 0,
    val userAnswer: String = "",
    val wrongAttempts: Int = 0,
    val gameStartTime: Long = 0L,
    val isGameActive: Boolean = false,
    val isLoading: Boolean = true,
    val gameCompleted: Boolean = false, // Flag to track if game finished properly
    val gameSessionId: String = "", // Unique ID for this game session
    val gameMode: GameMode = GameMode.ADDITION_SUBTRACTION,
    val difficulty: Difficulty = Difficulty.EASY
)

/**
 * ViewModel for the Game screen.
 * 
 * This ViewModel handles all the game logic including:
 * - Loading game settings
 * - Generating questions
 * - Tracking time and score
 * - Managing game state
 */
class GameViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    private val questionGenerator = QuestionGenerator()
    
    // Private mutable state
    private val _uiState = MutableStateFlow(GameUiState())
    
    // Public read-only state
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    /**
     * Initialize the game with the specified game mode.
     * This should be called when navigating to the GameScreen.
     * 
     * @param gameMode The type of math problems to generate
     */
    fun initializeGame(gameMode: GameMode) {
        viewModelScope.launch {
            try {
                // Reset game state completely for a fresh start
                resetGame()
                
                // Get current settings from DataStore
                val settings = settingsManager.gameSettings.first()
                
                // Update UI state with settings and start the game
                _uiState.value = _uiState.value.copy(
                    gameMode = gameMode,
                    difficulty = settings.difficulty,
                    totalQuestions = settings.questionCount,
                    questionNumber = 1,
                    wrongAttempts = 0, // Explicitly reset wrong attempts
                    gameStartTime = System.currentTimeMillis(),
                    gameSessionId = System.currentTimeMillis().toString(), // Unique session ID
                    isGameActive = true,
                    isLoading = false
                )
                
                // Generate the first question
                generateNextQuestion()
                
            } catch (e: Exception) {
                // Handle error (in a real app, you might want to show an error state)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    /**
     * Generate the next question in the sequence.
     */
    private fun generateNextQuestion() {
        val currentState = _uiState.value
        val question = questionGenerator.generateQuestion(
            currentState.gameMode,
            currentState.difficulty
        )
        
        _uiState.value = currentState.copy(
            currentQuestion = question,
            userAnswer = ""
        )
    }
    
    /**
     * Update the user's current answer input.
     * Automatically submits when the expected number of digits is reached.
     * 
     * @param answer The user's input as a string
     */
    fun updateUserAnswer(answer: String) {
        // Only allow numeric input
        val numericAnswer = answer.filter { it.isDigit() }
        
        _uiState.value = _uiState.value.copy(userAnswer = numericAnswer)
        
        // Auto-submit when expected number of digits is reached
        val currentQuestion = _uiState.value.currentQuestion
        if (currentQuestion != null && numericAnswer.length == currentQuestion.expectedDigits && numericAnswer.isNotBlank()) {
            // Small delay to ensure the UI updates before submission
            viewModelScope.launch {
                kotlinx.coroutines.delay(100) // 100ms delay
                submitAnswer()
            }
        }
    }
    
    /**
     * Submit the user's answer and handle the result.
     * This function checks if the answer is correct and either
     * moves to the next question or increments wrong attempts.
     */
    fun submitAnswer() {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion ?: return
        
        // Parse the user's answer
        val userAnswerInt = currentState.userAnswer.toIntOrNull()
        
        if (userAnswerInt == currentQuestion.correctAnswer) {
            // Correct answer - move to next question or end game
            if (currentState.questionNumber >= currentState.totalQuestions) {
                // Game complete
                endGame()
            } else {
                // Next question
                _uiState.value = currentState.copy(
                    questionNumber = currentState.questionNumber + 1
                )
                generateNextQuestion()
            }
        } else {
            // Wrong answer - increment wrong attempts and clear input
            _uiState.value = currentState.copy(
                wrongAttempts = currentState.wrongAttempts + 1,
                userAnswer = ""
            )
        }
    }
    
    /**
     * End the current game and transition to results.
     * This calculates the final time and prepares data for the ResultsScreen.
     */
    private fun endGame() {
        _uiState.value = _uiState.value.copy(
            isGameActive = false,
            gameCompleted = true
        )
    }
    
    /**
     * Get the total time taken for the game (in milliseconds).
     */
    fun getTotalGameTime(): Long {
        val currentState = _uiState.value
        return if (currentState.gameStartTime > 0) {
            System.currentTimeMillis() - currentState.gameStartTime
        } else {
            0L
        }
    }
    
    /**
     * Calculate the time penalty based on wrong attempts.
     * Each wrong answer adds 5 seconds to the final time.
     */
    fun getTimePenalty(): Long {
        return _uiState.value.wrongAttempts * 5000L // 5 seconds per wrong attempt
    }
    
    /**
     * Get the final score (total time + penalty).
     */
    fun getFinalScore(): Long {
        return getTotalGameTime() + getTimePenalty()
    }
    
    /**
     * Reset the game state for a new game.
     * Completely clears all previous game data.
     */
    fun resetGame() {
        _uiState.value = GameUiState(
            currentQuestion = null,
            questionNumber = 0,
            totalQuestions = 0,
            userAnswer = "",
            wrongAttempts = 0,
            gameStartTime = 0L,
            isGameActive = false,
            isLoading = true,
            gameCompleted = false,
            gameSessionId = "",
            gameMode = GameMode.ADDITION_SUBTRACTION,
            difficulty = Difficulty.EASY
        )
    }
    
    /**
     * Force clear all game state - use when navigating away from game/results
     */
    fun clearGameState() {
        resetGame()
    }
}
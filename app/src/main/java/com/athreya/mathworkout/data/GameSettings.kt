package com.athreya.mathworkout.data

/**
 * Enum representing different difficulty levels in the game.
 * This determines the range of numbers used in math problems.
 */
enum class Difficulty {
    EASY,    // Numbers 1-10
    MEDIUM,  // Numbers 1-100
    COMPLEX  // Numbers 1-1000, more complex operations
}

/**
 * Enum representing different game modes available.
 * Each mode focuses on specific mathematical operations.
 */
enum class GameMode {
    ADDITION_SUBTRACTION,     // Addition and subtraction problems
    MULTIPLICATION_DIVISION,  // Multiplication and division problems
    TEST_ME,                 // Mixed operations
    BRAIN_TEASER,           // Complex multi-step problems
    SUDOKU                  // Sudoku puzzle solving
}

/**
 * Data class representing user settings for the game.
 * These settings persist between app sessions using DataStore.
 * 
 * @param difficulty The selected difficulty level
 * @param questionCount Number of questions per game session
 */
data class GameSettings(
    val difficulty: Difficulty = Difficulty.EASY,
    val questionCount: Int = 10
)
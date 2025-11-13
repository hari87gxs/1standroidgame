package com.athreya.mathworkout.navigation

import com.athreya.mathworkout.data.GameMode

/**
 * Navigation routes for the app.
 * 
 * This sealed class defines all the possible destinations in our app.
 * Using sealed classes for navigation routes provides type safety
 * and makes it easier to handle navigation arguments.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object HighScores : Screen("high_scores")
    object Results : Screen("results/{gameMode}/{difficulty}/{wrongAttempts}/{totalTime}") {
        fun createRoute(gameMode: GameMode, difficulty: String, wrongAttempts: Int, totalTime: Long): String {
            return "results/${gameMode.name}/$difficulty/$wrongAttempts/$totalTime"
        }
    }
    object Game : Screen("game/{gameMode}") {
        fun createRoute(gameMode: GameMode): String {
            return "game/${gameMode.name}"
        }
    }
    object Sudoku : Screen("sudoku")
}
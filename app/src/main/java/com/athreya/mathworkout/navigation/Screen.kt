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
    object ThemeSelector : Screen("theme_selector")
    object Achievements : Screen("achievements")
    object HighScores : Screen("high_scores")
    object GlobalLeaderboard : Screen("global_leaderboard")
    object DailyChallenge : Screen("daily_challenge")
    
    // Social features
    object Groups : Screen("groups")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String): String {
            return "group_detail/$groupId"
        }
    }
    object Challenges : Screen("challenges")
    object ChallengeDetail : Screen("challenge_detail/{challengeId}") {
        fun createRoute(challengeId: String): String {
            return "challenge_detail/$challengeId"
        }
    }
    
    object Results : Screen("results/{gameMode}/{difficulty}/{wrongAttempts}/{totalTime}/{questionsAnswered}/{isDailyChallenge}") {
        fun createRoute(gameMode: GameMode, difficulty: String, wrongAttempts: Int, totalTime: Long, questionsAnswered: Int, isDailyChallenge: Boolean = false): String {
            return "results/${gameMode.name}/$difficulty/$wrongAttempts/$totalTime/$questionsAnswered/$isDailyChallenge"
        }
    }
    object Game : Screen("game/{gameMode}/{isDailyChallenge}?challengeId={challengeId}") {
        fun createRoute(gameMode: GameMode, isDailyChallenge: Boolean = false, challengeId: String? = null): String {
            return if (challengeId != null) {
                "game/${gameMode.name}/$isDailyChallenge?challengeId=$challengeId"
            } else {
                "game/${gameMode.name}/$isDailyChallenge"
            }
        }
    }
    object Sudoku : Screen("sudoku")
    object GlobalScore : Screen("global_score")
}
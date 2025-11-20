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
    object Mathematicians : Screen("mathematicians")
    object Badges : Screen("badges")
    object MathTricks : Screen("math_tricks")
    object TrickDetail : Screen("trick_detail/{trickId}") {
        fun createRoute(trickId: String): String {
            return "trick_detail/$trickId"
        }
    }
    object TrickPractice : Screen("trick_practice/{trickId}") {
        fun createRoute(trickId: String): String {
            return "trick_practice/$trickId"
        }
    }
    object InteractiveGames : Screen("interactive_games")
    object DailyRiddle : Screen("daily_riddle")
    object GamePlay : Screen("game_play/{gameType}/{difficulty}") {
        fun createRoute(gameType: String, difficulty: String): String {
            return "game_play/$gameType/$difficulty"
        }
    }
    
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
    
    object Results : Screen("results/{gameMode}/{difficulty}/{wrongAttempts}/{totalTime}/{questionsAnswered}/{isDailyChallenge}?challengeId={challengeId}") {
        fun createRoute(gameMode: GameMode, difficulty: String, wrongAttempts: Int, totalTime: Long, questionsAnswered: Int, isDailyChallenge: Boolean = false, challengeId: String? = null): String {
            return if (challengeId != null) {
                "results/${gameMode.name}/$difficulty/$wrongAttempts/$totalTime/$questionsAnswered/$isDailyChallenge?challengeId=$challengeId"
            } else {
                "results/${gameMode.name}/$difficulty/$wrongAttempts/$totalTime/$questionsAnswered/$isDailyChallenge"
            }
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
    object Sudoku : Screen("sudoku/{isDailyChallenge}") {
        fun createRoute(isDailyChallenge: Boolean = false): String {
            return "sudoku/$isDailyChallenge"
        }
    }
    object GlobalScore : Screen("global_score")
}
package com.athreya.mathworkout.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages achievement progress, unlocking, and notifications.
 */
class AchievementManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "achievement_prefs",
        Context.MODE_PRIVATE
    )
    
    private val _unlockedAchievements = MutableStateFlow<Set<String>>(loadUnlockedAchievements())
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()
    
    private val _newlyUnlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val newlyUnlockedAchievements: StateFlow<List<Achievement>> = _newlyUnlockedAchievements.asStateFlow()
    
    // Statistics tracking
    private var totalGamesPlayed: Int
        get() = prefs.getInt("total_games", 0)
        set(value) = prefs.edit().putInt("total_games", value).apply()
    
    private var perfectGamesCount: Int
        get() = prefs.getInt("perfect_games", 0)
        set(value) = prefs.edit().putInt("perfect_games", value).apply()
    
    private var speedMultiplier3xCount: Int
        get() = prefs.getInt("speed_3x_count", 0)
        set(value) = prefs.edit().putInt("speed_3x_count", value).apply()
    
    private var highScoreSingleGame: Int
        get() = prefs.getInt("high_score_single", 0)
        set(value) = prefs.edit().putInt("high_score_single", value).apply()
    
    private var challengeWins: Int
        get() = prefs.getInt("challenge_wins", 0)
        set(value) = prefs.edit().putInt("challenge_wins", value).apply()
    
    private var totalPointsAccumulated: Int
        get() = prefs.getInt("total_points", 0)
        set(value) = prefs.edit().putInt("total_points", value).apply()
    
    /**
     * Get all achievements with their current progress and unlock status.
     */
    fun getAllAchievementsWithProgress(): List<Achievement> {
        val unlocked = _unlockedAchievements.value
        return AchievementDefinitions.getAllAchievements().map { achievement ->
            val progress = getAchievementProgress(achievement)
            achievement.copy(
                unlocked = unlocked.contains(achievement.id),
                progress = progress
            )
        }
    }
    
    /**
     * Calculate current progress for an achievement.
     */
    private fun getAchievementProgress(achievement: Achievement): Int {
        return when (achievement.category) {
            AchievementCategory.GAMES_PLAYED -> totalGamesPlayed
            AchievementCategory.PERFECT_SCORES -> perfectGamesCount
            AchievementCategory.SPEED_DEMON -> speedMultiplier3xCount
            AchievementCategory.STREAK_MASTER -> getCurrentStreak()
            AchievementCategory.DIFFICULTY_MASTER -> getCompletedDifficultyCount()
            AchievementCategory.MULTIPLAYER -> challengeWins
            else -> 0
        }
    }
    
    /**
     * Track a completed game and check for achievement unlocks.
     */
    fun trackGameCompletion(
        score: Int,
        wrongAttempts: Int,
        timeMultiplier: Float,
        difficulty: Difficulty,
        isDailyChallenge: Boolean = false
    ) {
        totalGamesPlayed++
        totalPointsAccumulated += score
        
        // Track perfect game
        if (wrongAttempts == 0) {
            perfectGamesCount++
        }
        
        // Track speed multiplier
        if (timeMultiplier >= 3.0f) {
            speedMultiplier3xCount++
        }
        
        // Track high score
        if (score > highScoreSingleGame) {
            highScoreSingleGame = score
        }
        
        // Track difficulty completion
        markDifficultyCompleted(difficulty)
        
        // Track daily challenge
        if (isDailyChallenge) {
            recordDailyChallengeCompletion()
        }
        
        // Check for unlocks
        checkAndUnlockAchievements()
    }
    
    /**
     * Track a challenge win.
     */
    fun trackChallengeWin() {
        challengeWins++
        checkAndUnlockAchievements()
    }
    
    /**
     * Check all achievements and unlock any that meet their requirements.
     */
    private fun checkAndUnlockAchievements() {
        val currentlyUnlocked = _unlockedAchievements.value.toMutableSet()
        val newUnlocks = mutableListOf<Achievement>()
        
        AchievementDefinitions.getAllAchievements().forEach { achievement ->
            if (!currentlyUnlocked.contains(achievement.id)) {
                val progress = getAchievementProgress(achievement)
                if (progress >= achievement.requirement) {
                    currentlyUnlocked.add(achievement.id)
                    newUnlocks.add(achievement.copy(unlocked = true, progress = progress))
                    
                    // Unlock theme based on achievement
                    when (achievement.id) {
                        "games_50" -> unlockTheme("dc") // Complete 50 games unlocks DC theme
                        "streak_7" -> unlockTheme("ocean") // 7-day streak unlocks Ocean theme
                        "speed_30s" -> unlockTheme("neon") // Speed achievement unlocks Neon theme
                    }
                }
            }
        }
        
        // Check special conditions for theme unlocks
        // Marvel: Score 300+ in a single game
        if (highScoreSingleGame >= 300) {
            unlockTheme("marvel")
        }
        
        // Sunset: Reach 5000+ total points (Level 10 equivalent)
        if (totalPointsAccumulated >= 5000) {
            unlockTheme("sunset")
        }
        
        if (newUnlocks.isNotEmpty()) {
            _unlockedAchievements.value = currentlyUnlocked
            _newlyUnlockedAchievements.value = newUnlocks
            saveUnlockedAchievements(currentlyUnlocked)
        }
    }
    
    /**
     * Get current rank based on total points.
     */
    fun getCurrentRank(): Rank {
        return Ranks.getRankForPoints(totalPointsAccumulated)
    }
    
    /**
     * Get total points earned across all games.
     */
    fun getTotalPoints(): Int = totalPointsAccumulated
    
    /**
     * Get progress to next rank (0.0 to 1.0).
     */
    fun getRankProgress(): Float {
        return Ranks.getProgressToNextRank(totalPointsAccumulated)
    }
    
    /**
     * Clear notification of newly unlocked achievements.
     */
    fun clearNewlyUnlockedAchievements() {
        _newlyUnlockedAchievements.value = emptyList()
    }
    
    // Helper methods for streak tracking
    private fun getCurrentStreak(): Int {
        return prefs.getInt("current_streak", 0)
    }
    
    private fun recordDailyChallengeCompletion() {
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastCompletion = prefs.getLong("last_daily_completion", 0)
        val yesterday = today - 1
        
        val currentStreak = if (lastCompletion == yesterday) {
            getCurrentStreak() + 1
        } else if (lastCompletion == today) {
            getCurrentStreak() // Already completed today
        } else {
            1 // Streak broken, start fresh
        }
        
        prefs.edit()
            .putInt("current_streak", currentStreak)
            .putLong("last_daily_completion", today)
            .apply()
    }
    
    // Helper methods for difficulty tracking
    private fun markDifficultyCompleted(difficulty: Difficulty) {
        val key = "difficulty_${difficulty.name.lowercase()}_completed"
        prefs.edit().putBoolean(key, true).apply()
    }
    
    private fun getCompletedDifficultyCount(): Int {
        var count = 0
        Difficulty.values().forEach { difficulty ->
            val key = "difficulty_${difficulty.name.lowercase()}_completed"
            if (prefs.getBoolean(key, false)) count++
        }
        return count
    }
    
    // Theme unlocking
    private fun unlockTheme(themeId: String) {
        val themeManager = ThemePreferencesManager(context)
        themeManager.unlockTheme(themeId)
    }
    
    // Persistence
    private fun loadUnlockedAchievements(): Set<String> {
        return prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
    }
    
    private fun saveUnlockedAchievements(unlocked: Set<String>) {
        prefs.edit().putStringSet("unlocked_achievements", unlocked).apply()
    }
    
    /**
     * Get statistics for display.
     */
    fun getStats(): AchievementStats {
        return AchievementStats(
            totalGames = totalGamesPlayed,
            perfectGames = perfectGamesCount,
            speedGames = speedMultiplier3xCount,
            currentStreak = getCurrentStreak(),
            totalPoints = totalPointsAccumulated,
            highScore = highScoreSingleGame,
            challengeWins = challengeWins
        )
    }
    
    /**
     * Reset all achievements and statistics (for testing purposes).
     */
    fun resetAllAchievements() {
        prefs.edit().clear().apply()
        _unlockedAchievements.value = emptySet()
        _newlyUnlockedAchievements.value = emptyList()
    }
}

/**
 * Container for achievement statistics.
 */
data class AchievementStats(
    val totalGames: Int,
    val perfectGames: Int,
    val speedGames: Int,
    val currentStreak: Int,
    val totalPoints: Int,
    val highScore: Int,
    val challengeWins: Int
)

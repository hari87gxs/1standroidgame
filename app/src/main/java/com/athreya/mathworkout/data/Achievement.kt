package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing an achievement/badge.
 * Achievements unlock when players reach specific milestones.
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String, // Unique achievement ID
    val title: String,
    val description: String,
    val iconName: String, // Icon resource name
    val category: AchievementCategory,
    val requirement: Int, // Number required to unlock
    val unlocked: Boolean = false,
    val progress: Int = 0,
    val unlockedTimestamp: Long? = null,
    val xpReward: Int = 100 // XP points awarded
)

enum class AchievementCategory {
    GAMES_PLAYED,
    PERFECT_SCORES,
    SPEED_DEMON,
    STREAK_MASTER,
    GAME_MODE_MASTER,
    DIFFICULTY_MASTER,
    DAILY_CHALLENGE,
    MULTIPLAYER
}

object AchievementDefinitions {
    /**
     * All available achievements in the game
     */
    fun getAllAchievements(): List<Achievement> = listOf(
        // Games Played Achievements
        Achievement(
            id = "first_game",
            title = "First Steps",
            description = "Complete your first game",
            iconName = "ic_first_game",
            category = AchievementCategory.GAMES_PLAYED,
            requirement = 1,
            xpReward = 50
        ),
        Achievement(
            id = "games_10",
            title = "Getting Started",
            description = "Complete 10 games",
            iconName = "ic_games_10",
            category = AchievementCategory.GAMES_PLAYED,
            requirement = 10,
            xpReward = 100
        ),
        Achievement(
            id = "games_50",
            title = "Dedicated Player",
            description = "Complete 50 games",
            iconName = "ic_games_50",
            category = AchievementCategory.GAMES_PLAYED,
            requirement = 50,
            xpReward = 250
        ),
        Achievement(
            id = "games_100",
            title = "Centurion",
            description = "Complete 100 games",
            iconName = "ic_games_100",
            category = AchievementCategory.GAMES_PLAYED,
            requirement = 100,
            xpReward = 500
        ),
        Achievement(
            id = "games_500",
            title = "Math Master",
            description = "Complete 500 games",
            iconName = "ic_games_500",
            category = AchievementCategory.GAMES_PLAYED,
            requirement = 500,
            xpReward = 1000
        ),
        
        // Perfect Score Achievements
        Achievement(
            id = "perfect_first",
            title = "Perfection",
            description = "Get your first perfect score (no mistakes)",
            iconName = "ic_perfect",
            category = AchievementCategory.PERFECT_SCORES,
            requirement = 1,
            xpReward = 100
        ),
        Achievement(
            id = "perfect_10",
            title = "Flawless",
            description = "Get 10 perfect scores",
            iconName = "ic_perfect_10",
            category = AchievementCategory.PERFECT_SCORES,
            requirement = 10,
            xpReward = 300
        ),
        Achievement(
            id = "perfect_50",
            title = "Unstoppable",
            description = "Get 50 perfect scores",
            iconName = "ic_perfect_50",
            category = AchievementCategory.PERFECT_SCORES,
            requirement = 50,
            xpReward = 750
        ),
        
        // Speed Achievements
        Achievement(
            id = "speed_60s",
            title = "Speed Runner",
            description = "Complete a game in under 60 seconds",
            iconName = "ic_speed_60",
            category = AchievementCategory.SPEED_DEMON,
            requirement = 1,
            xpReward = 150
        ),
        Achievement(
            id = "speed_30s",
            title = "Lightning Fast",
            description = "Complete a game in under 30 seconds",
            iconName = "ic_speed_30",
            category = AchievementCategory.SPEED_DEMON,
            requirement = 1,
            xpReward = 300
        ),
        
        // Streak Achievements
        Achievement(
            id = "streak_3",
            title = "On Fire",
            description = "Maintain a 3-day streak",
            iconName = "ic_streak_3",
            category = AchievementCategory.STREAK_MASTER,
            requirement = 3,
            xpReward = 150
        ),
        Achievement(
            id = "streak_7",
            title = "Week Warrior",
            description = "Maintain a 7-day streak",
            iconName = "ic_streak_7",
            category = AchievementCategory.STREAK_MASTER,
            requirement = 7,
            xpReward = 350
        ),
        Achievement(
            id = "streak_30",
            title = "Month Champion",
            description = "Maintain a 30-day streak",
            iconName = "ic_streak_30",
            category = AchievementCategory.STREAK_MASTER,
            requirement = 30,
            xpReward = 1000
        ),
        
        // Game Mode Achievements
        Achievement(
            id = "master_addition",
            title = "Addition Expert",
            description = "Complete 25 Addition games",
            iconName = "ic_addition_master",
            category = AchievementCategory.GAME_MODE_MASTER,
            requirement = 25,
            xpReward = 200
        ),
        Achievement(
            id = "master_multiplication",
            title = "Multiplication Pro",
            description = "Complete 25 Multiplication games",
            iconName = "ic_multiplication_master",
            category = AchievementCategory.GAME_MODE_MASTER,
            requirement = 25,
            xpReward = 200
        ),
        Achievement(
            id = "master_sudoku",
            title = "Sudoku Solver",
            description = "Complete 25 Sudoku games",
            iconName = "ic_sudoku_master",
            category = AchievementCategory.GAME_MODE_MASTER,
            requirement = 25,
            xpReward = 300
        ),
        
        // Difficulty Achievements
        Achievement(
            id = "hard_mode",
            title = "Challenge Accepted",
            description = "Complete a game on Hard difficulty",
            iconName = "ic_hard_mode",
            category = AchievementCategory.DIFFICULTY_MASTER,
            requirement = 1,
            xpReward = 100
        ),
        Achievement(
            id = "hard_mode_10",
            title = "Hardcore Player",
            description = "Complete 10 games on Hard difficulty",
            iconName = "ic_hard_10",
            category = AchievementCategory.DIFFICULTY_MASTER,
            requirement = 10,
            xpReward = 400
        ),
        
        // Daily Challenge Achievements
        Achievement(
            id = "daily_first",
            title = "Daily Dedication",
            description = "Complete your first daily challenge",
            iconName = "ic_daily_first",
            category = AchievementCategory.DAILY_CHALLENGE,
            requirement = 1,
            xpReward = 150
        ),
        Achievement(
            id = "daily_7",
            title = "Challenge Seeker",
            description = "Complete 7 daily challenges",
            iconName = "ic_daily_7",
            category = AchievementCategory.DAILY_CHALLENGE,
            requirement = 7,
            xpReward = 500
        ),
        Achievement(
            id = "daily_30",
            title = "Challenge Champion",
            description = "Complete 30 daily challenges",
            iconName = "ic_daily_30",
            category = AchievementCategory.DAILY_CHALLENGE,
            requirement = 30,
            xpReward = 1500
        ),
        
        // Multiplayer Achievements
        Achievement(
            id = "multiplayer_first",
            title = "Social Player",
            description = "Complete your first multiplayer game",
            iconName = "ic_multiplayer_first",
            category = AchievementCategory.MULTIPLAYER,
            requirement = 1,
            xpReward = 100
        ),
        Achievement(
            id = "multiplayer_win",
            title = "Competitive Spirit",
            description = "Win your first multiplayer game",
            iconName = "ic_multiplayer_win",
            category = AchievementCategory.MULTIPLAYER,
            requirement = 1,
            xpReward = 200
        ),
        Achievement(
            id = "multiplayer_10",
            title = "Multiplayer Master",
            description = "Win 10 multiplayer games",
            iconName = "ic_multiplayer_10",
            category = AchievementCategory.MULTIPLAYER,
            requirement = 10,
            xpReward = 600
        )
    )
}

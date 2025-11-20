package com.athreya.mathworkout.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Badge categories
 */
enum class BadgeCategory {
    SPEED,
    ACCURACY,
    COLLECTION,
    CHALLENGE,
    DEDICATION
}

/**
 * Badge rarity levels
 */
enum class BadgeRarity(val color: Long, val displayName: String) {
    BRONZE(0xFFCD7F32, "Bronze"),
    SILVER(0xFFC0C0C0, "Silver"),
    GOLD(0xFFFFD700, "Gold"),
    PLATINUM(0xFFE5E4E2, "Platinum"),
    DIAMOND(0xFFB9F2FF, "Diamond")
}

/**
 * Achievement Badge
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val emoji: String,
    val requirement: Int,
    val isUnlocked: Boolean = false,
    val progress: Int = 0
) {
    val progressPercent: Float
        get() = if (requirement > 0) (progress.toFloat() / requirement).coerceIn(0f, 1f) else 0f
}

/**
 * Badge definitions
 */
object Badges {
    
    fun getAllBadges(): List<Badge> {
        return listOf(
            // Speed Badges
            Badge(
                id = "speed_demon",
                name = "Speed Demon",
                description = "Complete 10 games in under 1 minute each",
                category = BadgeCategory.SPEED,
                rarity = BadgeRarity.GOLD,
                emoji = "⚡",
                requirement = 10
            ),
            Badge(
                id = "lightning_fast",
                name = "Lightning Fast",
                description = "Complete 50 games in under 1 minute each",
                category = BadgeCategory.SPEED,
                rarity = BadgeRarity.PLATINUM,
                emoji = "⚡",
                requirement = 50
            ),
            Badge(
                id = "flash",
                name = "The Flash",
                description = "Complete 100 games in under 45 seconds each",
                category = BadgeCategory.SPEED,
                rarity = BadgeRarity.DIAMOND,
                emoji = "💨",
                requirement = 100
            ),
            
            // Accuracy Badges
            Badge(
                id = "perfect_score",
                name = "Perfect Score",
                description = "Complete 10 games with 100% accuracy",
                category = BadgeCategory.ACCURACY,
                rarity = BadgeRarity.GOLD,
                emoji = "🎯",
                requirement = 10
            ),
            Badge(
                id = "flawless",
                name = "Flawless",
                description = "Complete 50 games with 100% accuracy",
                category = BadgeCategory.ACCURACY,
                rarity = BadgeRarity.PLATINUM,
                emoji = "💎",
                requirement = 50
            ),
            Badge(
                id = "perfectionist",
                name = "Perfectionist",
                description = "Complete 100 games with 100% accuracy",
                category = BadgeCategory.ACCURACY,
                rarity = BadgeRarity.DIAMOND,
                emoji = "👑",
                requirement = 100
            ),
            
            // Collection Badges
            Badge(
                id = "mathematician_collector",
                name = "Mathematician Collector",
                description = "Unlock 10 famous mathematicians",
                category = BadgeCategory.COLLECTION,
                rarity = BadgeRarity.SILVER,
                emoji = "📚",
                requirement = 10
            ),
            Badge(
                id = "math_historian",
                name = "Math Historian",
                description = "Unlock 20 famous mathematicians",
                category = BadgeCategory.COLLECTION,
                rarity = BadgeRarity.GOLD,
                emoji = "🎓",
                requirement = 20
            ),
            Badge(
                id = "complete_collection",
                name = "Complete Collection",
                description = "Unlock all 24 mathematicians",
                category = BadgeCategory.COLLECTION,
                rarity = BadgeRarity.DIAMOND,
                emoji = "🏆",
                requirement = 24
            ),
            
            // Challenge Badges
            Badge(
                id = "challenger",
                name = "Challenger",
                description = "Win 10 challenge matches",
                category = BadgeCategory.CHALLENGE,
                rarity = BadgeRarity.BRONZE,
                emoji = "⚔️",
                requirement = 10
            ),
            Badge(
                id = "challenge_master",
                name = "Challenge Master",
                description = "Win 50 challenge matches",
                category = BadgeCategory.CHALLENGE,
                rarity = BadgeRarity.GOLD,
                emoji = "🥇",
                requirement = 50
            ),
            Badge(
                id = "undefeated",
                name = "Undefeated Champion",
                description = "Win 100 challenge matches",
                category = BadgeCategory.CHALLENGE,
                rarity = BadgeRarity.DIAMOND,
                emoji = "👑",
                requirement = 100
            ),
            
            // Dedication Badges
            Badge(
                id = "week_warrior",
                name = "Week Warrior",
                description = "Maintain a 7-day login streak",
                category = BadgeCategory.DEDICATION,
                rarity = BadgeRarity.BRONZE,
                emoji = "🔥",
                requirement = 7
            ),
            Badge(
                id = "monthly_master",
                name = "Monthly Master",
                description = "Maintain a 30-day login streak",
                category = BadgeCategory.DEDICATION,
                rarity = BadgeRarity.SILVER,
                emoji = "🌟",
                requirement = 30
            ),
            Badge(
                id = "daily_warrior",
                name = "Daily Warrior",
                description = "Maintain a 100-day login streak",
                category = BadgeCategory.DEDICATION,
                rarity = BadgeRarity.GOLD,
                emoji = "💪",
                requirement = 100
            ),
            Badge(
                id = "eternal_student",
                name = "Eternal Student",
                description = "Maintain a 365-day login streak",
                category = BadgeCategory.DEDICATION,
                rarity = BadgeRarity.DIAMOND,
                emoji = "🎖️",
                requirement = 365
            )
        )
    }
    
    fun getBadgeById(id: String): Badge? {
        return getAllBadges().find { it.id == id }
    }
}

/**
 * Manages badge progress and unlocking
 */
class BadgeManager(private val userPreferences: UserPreferencesManager) {
    
    companion object {
        private const val PREF_BADGE_PREFIX = "badge_"
        private const val PREF_PROGRESS_PREFIX = "badge_progress_"
        
        // Progress trackers
        private const val PREF_SPEED_GAMES_COUNT = "speed_games_under_60s"
        private const val PREF_SPEED_GAMES_45S_COUNT = "speed_games_under_45s"
        private const val PREF_PERFECT_GAMES_COUNT = "perfect_accuracy_games"
        private const val PREF_CHALLENGE_WINS_COUNT = "challenge_wins"
    }
    
    /**
     * Get all badges with current progress
     */
    fun getAllBadges(): List<Badge> {
        return Badges.getAllBadges().map { badge ->
            badge.copy(
                isUnlocked = isBadgeUnlocked(badge.id),
                progress = getBadgeProgress(badge.id, badge.category)
            )
        }
    }
    
    /**
     * Get unlocked badges only
     */
    fun getUnlockedBadges(): List<Badge> {
        return getAllBadges().filter { it.isUnlocked }
    }
    
    /**
     * Check if badge is unlocked
     */
    fun isBadgeUnlocked(badgeId: String): Boolean {
        return userPreferences.getBoolean(PREF_BADGE_PREFIX + badgeId, false)
    }
    
    /**
     * Unlock a badge
     */
    private fun unlockBadge(badgeId: String) {
        userPreferences.putBoolean(PREF_BADGE_PREFIX + badgeId, true)
    }
    
    /**
     * Get badge progress
     */
    private fun getBadgeProgress(badgeId: String, category: BadgeCategory): Int {
        return when (category) {
            BadgeCategory.SPEED -> when (badgeId) {
                "flash" -> userPreferences.getInt(PREF_SPEED_GAMES_45S_COUNT, 0)
                else -> userPreferences.getInt(PREF_SPEED_GAMES_COUNT, 0)
            }
            BadgeCategory.ACCURACY -> userPreferences.getInt(PREF_PERFECT_GAMES_COUNT, 0)
            BadgeCategory.CHALLENGE -> userPreferences.getInt(PREF_CHALLENGE_WINS_COUNT, 0)
            BadgeCategory.DEDICATION -> {
                // Use current login streak
                userPreferences.getInt("login_streak", 0)
            }
            BadgeCategory.COLLECTION -> {
                // Count unlocked mathematicians (will need to query database)
                userPreferences.getInt(PREF_PROGRESS_PREFIX + badgeId, 0)
            }
        }
    }
    
    /**
     * Track game completion and check for badge unlocks
     */
    fun trackGameCompletion(
        timeTaken: Long,
        wrongAttempts: Int,
        questionsAnswered: Int
    ): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()
        
        // Track speed
        val timeInSeconds = timeTaken / 1000
        val avgTimePerQuestion = if (questionsAnswered > 0) timeInSeconds / questionsAnswered else 999
        
        if (avgTimePerQuestion <= 60) {
            val count = userPreferences.getInt(PREF_SPEED_GAMES_COUNT, 0) + 1
            userPreferences.putInt(PREF_SPEED_GAMES_COUNT, count)
            
            // Check speed badges
            checkAndUnlockBadge("speed_demon", count)?.let { newlyUnlocked.add(it) }
            checkAndUnlockBadge("lightning_fast", count)?.let { newlyUnlocked.add(it) }
        }
        
        if (avgTimePerQuestion <= 45) {
            val count = userPreferences.getInt(PREF_SPEED_GAMES_45S_COUNT, 0) + 1
            userPreferences.putInt(PREF_SPEED_GAMES_45S_COUNT, count)
            checkAndUnlockBadge("flash", count)?.let { newlyUnlocked.add(it) }
        }
        
        // Track perfect accuracy
        if (wrongAttempts == 0 && questionsAnswered > 0) {
            val count = userPreferences.getInt(PREF_PERFECT_GAMES_COUNT, 0) + 1
            userPreferences.putInt(PREF_PERFECT_GAMES_COUNT, count)
            
            checkAndUnlockBadge("perfect_score", count)?.let { newlyUnlocked.add(it) }
            checkAndUnlockBadge("flawless", count)?.let { newlyUnlocked.add(it) }
            checkAndUnlockBadge("perfectionist", count)?.let { newlyUnlocked.add(it) }
        }
        
        return newlyUnlocked
    }
    
    /**
     * Track challenge win
     */
    fun trackChallengeWin(): List<Badge> {
        val count = userPreferences.getInt(PREF_CHALLENGE_WINS_COUNT, 0) + 1
        userPreferences.putInt(PREF_CHALLENGE_WINS_COUNT, count)
        
        val newlyUnlocked = mutableListOf<Badge>()
        checkAndUnlockBadge("challenger", count)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("challenge_master", count)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("undefeated", count)?.let { newlyUnlocked.add(it) }
        
        return newlyUnlocked
    }
    
    /**
     * Track login streak for dedication badges
     */
    fun trackLoginStreak(streak: Int): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()
        
        checkAndUnlockBadge("week_warrior", streak)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("monthly_master", streak)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("daily_warrior", streak)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("eternal_student", streak)?.let { newlyUnlocked.add(it) }
        
        return newlyUnlocked
    }
    
    /**
     * Update mathematician collection progress
     */
    fun updateCollectionProgress(unlockedCount: Int): List<Badge> {
        val newlyUnlocked = mutableListOf<Badge>()
        
        checkAndUnlockBadge("mathematician_collector", unlockedCount)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("math_historian", unlockedCount)?.let { newlyUnlocked.add(it) }
        checkAndUnlockBadge("complete_collection", unlockedCount)?.let { newlyUnlocked.add(it) }
        
        return newlyUnlocked
    }
    
    /**
     * Check if badge should be unlocked and return it if newly unlocked
     */
    private fun checkAndUnlockBadge(badgeId: String, progress: Int): Badge? {
        if (isBadgeUnlocked(badgeId)) return null
        
        val badge = Badges.getBadgeById(badgeId) ?: return null
        
        if (progress >= badge.requirement) {
            unlockBadge(badgeId)
            return badge.copy(isUnlocked = true, progress = progress)
        }
        
        return null
    }
}

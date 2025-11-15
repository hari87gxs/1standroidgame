package com.athreya.mathworkout.data

import androidx.compose.ui.graphics.Color

/**
 * Represents player ranks based on total points earned.
 */
data class Rank(
    val id: String,
    val name: String,
    val icon: String, // Emoji badge
    val minPoints: Int,
    val maxPoints: Int?,
    val color: Color,
    val description: String
)

/**
 * All available ranks in the game.
 */
object Ranks {
    private val ranks = listOf(
        Rank(
            id = "beginner",
            name = "Beginner",
            icon = "🌱",
            minPoints = 0,
            maxPoints = 999,
            color = Color(0xFF4CAF50),
            description = "Just starting your math journey"
        ),
        Rank(
            id = "amateur",
            name = "Amateur",
            icon = "📚",
            minPoints = 1000,
            maxPoints = 4999,
            color = Color(0xFF2196F3),
            description = "Building your skills"
        ),
        Rank(
            id = "expert",
            name = "Expert",
            icon = "🎓",
            minPoints = 5000,
            maxPoints = 14999,
            color = Color(0xFF9C27B0),
            description = "Mastering the basics"
        ),
        Rank(
            id = "master",
            name = "Master",
            icon = "⚡",
            minPoints = 15000,
            maxPoints = 49999,
            color = Color(0xFFFF9800),
            description = "A force to be reckoned with"
        ),
        Rank(
            id = "grandmaster",
            name = "Grandmaster",
            icon = "👑",
            minPoints = 50000,
            maxPoints = null,
            color = Color(0xFFFFD700),
            description = "The pinnacle of math mastery"
        )
    )
    
    /**
     * Get rank based on total points.
     */
    fun getRankForPoints(totalPoints: Int): Rank {
        return ranks.lastOrNull { totalPoints >= it.minPoints } ?: ranks.first()
    }
    
    /**
     * Get next rank after current rank.
     */
    fun getNextRank(currentRank: Rank): Rank? {
        val currentIndex = ranks.indexOf(currentRank)
        return if (currentIndex >= 0 && currentIndex < ranks.size - 1) {
            ranks[currentIndex + 1]
        } else {
            null
        }
    }
    
    /**
     * Calculate progress percentage to next rank.
     */
    fun getProgressToNextRank(totalPoints: Int): Float {
        val currentRank = getRankForPoints(totalPoints)
        val nextRank = getNextRank(currentRank) ?: return 1f
        
        val pointsInCurrentRank = totalPoints - currentRank.minPoints
        val pointsNeededForNextRank = nextRank.minPoints - currentRank.minPoints
        
        return (pointsInCurrentRank.toFloat() / pointsNeededForNextRank.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Get points needed to reach next rank.
     */
    fun getPointsToNextRank(totalPoints: Int): Int {
        val currentRank = getRankForPoints(totalPoints)
        val nextRank = getNextRank(currentRank) ?: return 0
        
        return (nextRank.minPoints - totalPoints).coerceAtLeast(0)
    }
    
    /**
     * Get all ranks.
     */
    fun getAllRanks(): List<Rank> = ranks
}

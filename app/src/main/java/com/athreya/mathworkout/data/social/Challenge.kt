package com.athreya.mathworkout.data.social

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode

/**
 * Challenge entity for friend challenges
 * Represents a challenge from one player to another
 */
@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey
    val challengeId: String, // Unique challenge ID (Firebase document ID)
    
    val groupId: String, // Group where challenge was created
    
    // Challenge participants
    val challengerId: String, // Device ID of challenger
    val challengerName: String,
    val challengedId: String?, // Null for open group challenges
    val challengedName: String?,
    
    // Game configuration
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val questionCount: Int = 10,
    
    // Challenge scores
    val challengerScore: Int = 0,
    val challengerTime: Long = 0L,
    val challengerCompleted: Boolean = false,
    val challengerCompletedAt: Long? = null,
    
    val challengedScore: Int = 0,
    val challengedTime: Long = 0L,
    val challengedCompleted: Boolean = false,
    val challengedCompletedAt: Long? = null,
    
    // Challenge status
    val status: ChallengeStatus = ChallengeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
    
    // Results
    val winnerId: String? = null,
    val winnerName: String? = null,
    
    // Sync status
    val synced: Boolean = false,
    val lastSyncedAt: Long = 0L
) {
    /**
     * Check if challenge is expired
     */
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }
    
    /**
     * Check if challenge is complete
     */
    fun isComplete(): Boolean {
        return status == ChallengeStatus.COMPLETED || 
               status == ChallengeStatus.EXPIRED
    }
    
    /**
     * Determine winner based on scores
     * Note: Higher score is better (score = points from calculatePoints)
     */
    fun determineWinner(): Pair<String?, String?> {
        if (!challengerCompleted || !challengedCompleted) {
            return Pair(null, null)
        }
        
        // In this game, score = points, so HIGHER is BETTER
        return when {
            challengerScore!! > challengedScore!! -> Pair(challengerId, challengerName) // Challenger won (more points)
            challengedScore!! > challengerScore!! -> Pair(challengedId, challengedName) // Challenged won (more points)
            else -> Pair(null, "TIE") // Exact tie
        }
    }
}

/**
 * Challenge status enum
 */
enum class ChallengeStatus {
    PENDING,      // Challenge sent, waiting for response
    ACCEPTED,     // Both players accepted
    IN_PROGRESS,  // At least one player started
    COMPLETED,    // Both players finished
    DECLINED,     // Challenged player declined
    EXPIRED,      // Challenge time expired
    CANCELLED     // Challenger cancelled
}

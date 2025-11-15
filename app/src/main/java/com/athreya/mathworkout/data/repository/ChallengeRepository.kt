package com.athreya.mathworkout.data.repository

import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.social.Challenge
import com.athreya.mathworkout.data.social.ChallengeDao
import com.athreya.mathworkout.data.social.ChallengeFirebaseService
import com.athreya.mathworkout.data.social.ChallengeStatus
import com.athreya.mathworkout.data.social.GroupMemberDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for managing challenges between players
 * Handles challenge creation, acceptance, completion, and results
 * Syncs with Firebase when available
 */
class ChallengeRepository(
    private val challengeDao: ChallengeDao,
    private val groupMemberDao: GroupMemberDao,
    private val userPreferences: UserPreferencesManager,
    private val firebaseService: ChallengeFirebaseService? = null
) {
    
    /**
     * Create a challenge to a specific member
     * @param groupId ID of the group
     * @param challengedId Device ID of the challenged player
     * @param challengedName Name of the challenged player
     * @param gameMode Game mode for the challenge
     * @param difficulty Difficulty level
     * @param questionCount Number of questions (default 10)
     * @return Result with the created Challenge or error
     */
    suspend fun createChallenge(
        groupId: String,
        challengedId: String,
        challengedName: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        questionCount: Int = 10
    ): Result<Challenge> {
        return try {
            val challengerId = userPreferences.getDeviceId()
            val challengerName = userPreferences.getPlayerName() ?: "Player"
            
            android.util.Log.d("ChallengeRepository", "Creating challenge: challenger=$challengerId, challenged=$challengedId, group=$groupId")
            
            // Verify both are members of the group
            val challenger = groupMemberDao.getMember(groupId, challengerId)
            if (challenger == null) {
                android.util.Log.e("ChallengeRepository", "Challenger not found in group")
                return Result.failure(Exception("You are not a member of this group"))
            }
            
            val challenged = groupMemberDao.getMember(groupId, challengedId)
            if (challenged == null) {
                android.util.Log.e("ChallengeRepository", "Challenged player not found in group")
                return Result.failure(Exception("Challenged player is not in this group"))
            }
            
            // Cannot challenge yourself
            if (challengerId == challengedId) {
                android.util.Log.e("ChallengeRepository", "Cannot challenge yourself")
                return Result.failure(Exception("You cannot challenge yourself"))
            }
            
            // Create challenge
            val challengeId = UUID.randomUUID().toString()
            val challenge = Challenge(
                challengeId = challengeId,
                groupId = groupId,
                challengerId = challengerId,
                challengerName = challengerName,
                challengedId = challengedId,
                challengedName = challengedName,
                gameMode = gameMode,
                difficulty = difficulty,
                questionCount = questionCount,
                status = ChallengeStatus.PENDING
            )
            
            android.util.Log.d("ChallengeRepository", "Inserting challenge into database: $challengeId")
            challengeDao.insertChallenge(challenge)
            
            // Sync to Firebase in background
            android.util.Log.d("ChallengeRepository", "Uploading challenge to Firebase")
            firebaseService?.uploadChallenge(challenge)
            
            android.util.Log.d("ChallengeRepository", "Challenge created successfully: $challengeId")
            Result.success(challenge)
        } catch (e: Exception) {
            android.util.Log.e("ChallengeRepository", "Error creating challenge", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create an open challenge for the entire group
     * @param groupId ID of the group
     * @param gameMode Game mode for the challenge
     * @param difficulty Difficulty level
     * @param questionCount Number of questions
     * @return Result with the created Challenge or error
     */
    suspend fun createOpenChallenge(
        groupId: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        questionCount: Int = 10
    ): Result<Challenge> {
        return try {
            val challengerId = userPreferences.getDeviceId()
            val challengerName = userPreferences.getPlayerName() ?: "Player"
            
            // Verify is member
            groupMemberDao.getMember(groupId, challengerId)
                ?: return Result.failure(Exception("You are not a member of this group"))
            
            // Create open challenge (challengedId is null)
            val challengeId = UUID.randomUUID().toString()
            val challenge = Challenge(
                challengeId = challengeId,
                groupId = groupId,
                challengerId = challengerId,
                challengerName = challengerName,
                challengedId = null,
                challengedName = "Open Challenge",
                gameMode = gameMode,
                difficulty = difficulty,
                questionCount = questionCount,
                status = ChallengeStatus.ACCEPTED // Open challenges are auto-accepted
            )
            
            challengeDao.insertChallenge(challenge)
            
            Result.success(challenge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Accept a challenge
     * @param challengeId ID of the challenge to accept
     * @return Result with success or error
     */
    suspend fun acceptChallenge(challengeId: String): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            
            val challenge = challengeDao.getChallengeById(challengeId)
                ?: return Result.failure(Exception("Challenge not found"))
            
            // Verify this is for the current user
            if (challenge.challengedId != memberId) {
                return Result.failure(Exception("This challenge is not for you"))
            }
            
            // Check status
            if (challenge.status != ChallengeStatus.PENDING) {
                return Result.failure(Exception("Challenge is not pending"))
            }
            
            // Check expiration
            if (challenge.isExpired()) {
                challengeDao.updateChallengeStatus(challengeId, ChallengeStatus.EXPIRED)
                return Result.failure(Exception("Challenge has expired"))
            }
            
            // Accept challenge in local DB
            challengeDao.updateChallengeStatus(challengeId, ChallengeStatus.ACCEPTED)
            
            // Sync to Firebase
            val updatedChallenge = challengeDao.getChallengeById(challengeId)
            if (updatedChallenge != null) {
                firebaseService?.uploadChallenge(updatedChallenge)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Decline a challenge
     * @param challengeId ID of the challenge to decline
     * @return Result with success or error
     */
    suspend fun declineChallenge(challengeId: String): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            
            val challenge = challengeDao.getChallengeById(challengeId)
                ?: return Result.failure(Exception("Challenge not found"))
            
            // Verify this is for the current user
            if (challenge.challengedId != memberId) {
                return Result.failure(Exception("This challenge is not for you"))
            }
            
            // Check status
            if (challenge.status != ChallengeStatus.PENDING) {
                return Result.failure(Exception("Challenge is not pending"))
            }
            
            // Decline challenge in local DB
            challengeDao.updateChallengeStatus(challengeId, ChallengeStatus.DECLINED)
            
            // Sync to Firebase
            val updatedChallenge = challengeDao.getChallengeById(challengeId)
            if (updatedChallenge != null) {
                firebaseService?.uploadChallenge(updatedChallenge)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submit challenge result after completing the game
     * @param challengeId ID of the challenge
     * @param score Final score achieved
     * @param timeTaken Time taken in milliseconds
     * @return Result with success or error
     */
    suspend fun submitChallengeResult(
        challengeId: String,
        score: Int,
        timeTaken: Long
    ): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            val timestamp = System.currentTimeMillis()
            
            val challenge = challengeDao.getChallengeById(challengeId)
                ?: return Result.failure(Exception("Challenge not found"))
            
            // Check if expired
            if (challenge.isExpired()) {
                challengeDao.updateChallengeStatus(challengeId, ChallengeStatus.EXPIRED)
                return Result.failure(Exception("Challenge has expired"))
            }
            
            // Update based on who completed it
            when (memberId) {
                challenge.challengerId -> {
                    if (challenge.challengerCompleted) {
                        return Result.failure(Exception("You have already completed this challenge"))
                    }
                    challengeDao.updateChallengerResult(challengeId, score, timeTaken, timestamp)
                }
                challenge.challengedId -> {
                    if (challenge.challengedCompleted) {
                        return Result.failure(Exception("You have already completed this challenge"))
                    }
                    challengeDao.updateChallengedResult(challengeId, score, timeTaken, timestamp)
                }
                else -> {
                    return Result.failure(Exception("You are not a participant in this challenge"))
                }
            }
            
            // Update status to in progress
            if (challenge.status == ChallengeStatus.ACCEPTED) {
                challengeDao.updateChallengeStatus(challengeId, ChallengeStatus.IN_PROGRESS)
            }
            
            // Check if both completed
            val updatedChallenge = challengeDao.getChallengeById(challengeId)!!
            if (updatedChallenge.challengerCompleted && updatedChallenge.challengedCompleted) {
                // Both completed, determine winner
                finalizeChallengeResult(challengeId)
            }
            
            // Sync to Firebase
            val finalChallenge = challengeDao.getChallengeById(challengeId)
            if (finalChallenge != null) {
                firebaseService?.uploadChallenge(finalChallenge)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Finalize challenge and determine winner
     * Called when both players have completed the challenge
     */
    private suspend fun finalizeChallengeResult(challengeId: String) {
        try {
            val challenge = challengeDao.getChallengeById(challengeId)
                ?: return
            
            if (!challenge.challengerCompleted || !challenge.challengedCompleted) {
                return // Not ready to finalize
            }
            
            // Determine winner
            val (winnerId, winnerName) = challenge.determineWinner()
            
            // Set winner locally
            challengeDao.setChallengeWinner(challengeId, winnerId, winnerName)
            
            // Update challenge stats for both players
            if (winnerId != null && winnerId != "TIE") {
                val loserId = if (winnerId == challenge.challengerId) {
                    challenge.challengedId
                } else {
                    challenge.challengerId
                }
                
                // Update winner stats locally
                groupMemberDao.incrementChallengesWon(challenge.groupId, winnerId)
                
                // Update loser stats locally
                if (loserId != null) {
                    groupMemberDao.incrementChallengesLost(challenge.groupId, loserId)
                }
                
                // Note: Member stats will be synced to Firebase when the group is next synced
                // or when other group operations occur (updateMemberStatsAfterGame, etc.)
            }
            
            // Sync finalized challenge to Firebase
            val finalizedChallenge = challengeDao.getChallengeById(challengeId)
            if (finalizedChallenge != null) {
                firebaseService?.uploadChallenge(finalizedChallenge)
            }
        } catch (e: Exception) {
            android.util.Log.e("ChallengeRepository", "Error finalizing challenge", e)
        }
    }
    
    /**
     * Cancel a challenge (challenger only, before acceptance)
     * @param challengeId ID of the challenge to cancel
     * @return Result with success or error
     */
    suspend fun cancelChallenge(challengeId: String): Result<Unit> {
        return try {
            val memberId = userPreferences.getDeviceId()
            
            val challenge = challengeDao.getChallengeById(challengeId)
                ?: return Result.failure(Exception("Challenge not found"))
            
            // Only challenger can cancel
            if (challenge.challengerId != memberId) {
                return Result.failure(Exception("Only the challenger can cancel"))
            }
            
            // Can only cancel pending challenges
            if (challenge.status != ChallengeStatus.PENDING) {
                return Result.failure(Exception("Can only cancel pending challenges"))
            }
            
            // Update status locally
            challengeDao.updateChallengeStatus(challengeId, ChallengeStatus.CANCELLED)
            
            // Sync to Firebase
            val updatedChallenge = challengeDao.getChallengeById(challengeId)
            if (updatedChallenge != null) {
                firebaseService?.uploadChallenge(updatedChallenge)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all challenges for the current user
     */
    fun getMyChallenges(): Flow<List<Challenge>> {
        val memberId = userPreferences.getDeviceId()
        return challengeDao.getMemberChallenges(memberId)
    }
    
    /**
     * Get pending challenges for the current user (challenges awaiting response)
     */
    fun getPendingChallenges(): Flow<List<Challenge>> {
        val memberId = userPreferences.getDeviceId()
        return challengeDao.getPendingChallenges(memberId)
    }
    
    /**
     * Get active challenges (accepted or in progress)
     */
    fun getActiveChallenges(): Flow<List<Challenge>> {
        val memberId = userPreferences.getDeviceId()
        return challengeDao.getActiveChallenges(memberId)
    }
    
    /**
     * Get completed challenges (completed, declined, expired, cancelled)
     */
    fun getCompletedChallenges(): Flow<List<Challenge>> {
        val memberId = userPreferences.getDeviceId()
        return challengeDao.getCompletedChallenges(memberId)
    }
    
    /**
     * Get all challenges in a group
     */
    fun getGroupChallenges(groupId: String): Flow<List<Challenge>> {
        return challengeDao.getGroupChallenges(groupId)
    }
    
    /**
     * Get recent completed challenges for a group
     */
    fun getRecentCompletedChallenges(groupId: String, limit: Int = 20): Flow<List<Challenge>> {
        return challengeDao.getRecentCompletedChallenges(groupId, limit)
    }
    
    /**
     * Get a specific challenge by ID
     */
    fun getChallenge(challengeId: String): Flow<Challenge?> {
        return challengeDao.getChallengeByIdFlow(challengeId)
    }
    
    /**
     * Get count of pending challenges (for notification badge)
     */
    fun getPendingChallengeCount(): Flow<Int> {
        val memberId = userPreferences.getDeviceId()
        return challengeDao.getPendingChallengeCount(memberId)
    }
    
    /**
     * Expire old challenges (should be called periodically)
     */
    suspend fun expireOldChallenges() {
        try {
            val currentTime = System.currentTimeMillis()
            challengeDao.expireOldChallenges(currentTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get challenges by status
     */
    fun getChallengesByStatus(status: ChallengeStatus): Flow<List<Challenge>> {
        val memberId = userPreferences.getDeviceId()
        return challengeDao.getMemberChallengesByStatus(memberId, status)
    }
    
    /**
     * Sync challenges from Firebase for current user
     */
    suspend fun syncChallengesFromFirebase(): Result<Unit> {
        return try {
            if (firebaseService == null) {
                android.util.Log.w("ChallengeRepository", "Firebase service is null")
                return Result.failure(Exception("Firebase service not available"))
            }
            
            val memberId = userPreferences.getDeviceId()
            android.util.Log.d("ChallengeRepository", "Syncing challenges for user: $memberId")
            
            // Download challenges where user is either challenger or challenged
            val result = firebaseService.downloadMemberChallenges(memberId)
            
            if (result.isSuccess) {
                val challenges = result.getOrNull() ?: emptyList()
                android.util.Log.d("ChallengeRepository", "Downloaded ${challenges.size} challenges from Firebase")
                
                // Insert all challenges into local database
                challenges.forEach { challenge ->
                    challengeDao.insertChallenge(challenge)
                    android.util.Log.d("ChallengeRepository", "Inserted challenge: ${challenge.challengeId} (${challenge.challengerName} vs ${challenge.challengedName})")
                }
                
                Result.success(Unit)
            } else {
                android.util.Log.e("ChallengeRepository", "Failed to download challenges: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ChallengeRepository", "Error syncing challenges", e)
            Result.failure(e)
        }
    }
}

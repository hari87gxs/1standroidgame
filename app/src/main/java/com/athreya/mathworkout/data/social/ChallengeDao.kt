package com.athreya.mathworkout.data.social

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Challenge operations
 */
@Dao
interface ChallengeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)
    
    @Update
    suspend fun updateChallenge(challenge: Challenge)
    
    @Delete
    suspend fun deleteChallenge(challenge: Challenge)
    
    @Query("SELECT * FROM challenges WHERE challengeId = :challengeId")
    suspend fun getChallengeById(challengeId: String): Challenge?
    
    @Query("SELECT * FROM challenges WHERE challengeId = :challengeId")
    fun getChallengeByIdFlow(challengeId: String): Flow<Challenge?>
    
    @Query("SELECT * FROM challenges WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getGroupChallenges(groupId: String): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE (challengerId = :memberId OR challengedId = :memberId) ORDER BY createdAt DESC")
    fun getMemberChallenges(memberId: String): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE (challengerId = :memberId OR challengedId = :memberId) AND status = :status ORDER BY createdAt DESC")
    fun getMemberChallengesByStatus(memberId: String, status: ChallengeStatus): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE (challengerId = :memberId OR challengedId = :memberId) AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingChallenges(memberId: String): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE (challengerId = :memberId OR challengedId = :memberId) AND status IN ('ACCEPTED', 'IN_PROGRESS') ORDER BY createdAt DESC")
    fun getActiveChallenges(memberId: String): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE (challengerId = :memberId OR challengedId = :memberId) AND status IN ('COMPLETED', 'DECLINED', 'EXPIRED', 'CANCELLED') ORDER BY createdAt DESC")
    fun getCompletedChallenges(memberId: String): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE groupId = :groupId AND status = 'COMPLETED' ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentCompletedChallenges(groupId: String, limit: Int): Flow<List<Challenge>>
    
    @Query("UPDATE challenges SET status = :status WHERE challengeId = :challengeId")
    suspend fun updateChallengeStatus(challengeId: String, status: ChallengeStatus)
    
    @Query("UPDATE challenges SET challengerScore = :score, challengerTime = :time, challengerCompleted = 1, challengerCompletedAt = :timestamp WHERE challengeId = :challengeId")
    suspend fun updateChallengerResult(challengeId: String, score: Int, time: Long, timestamp: Long)
    
    @Query("UPDATE challenges SET challengedScore = :score, challengedTime = :time, challengedCompleted = 1, challengedCompletedAt = :timestamp WHERE challengeId = :challengeId")
    suspend fun updateChallengedResult(challengeId: String, score: Int, time: Long, timestamp: Long)
    
    @Query("UPDATE challenges SET winnerId = :winnerId, winnerName = :winnerName, status = 'COMPLETED' WHERE challengeId = :challengeId")
    suspend fun setChallengeWinner(challengeId: String, winnerId: String?, winnerName: String?)
    
    @Query("UPDATE challenges SET status = 'EXPIRED' WHERE expiresAt < :currentTime AND status NOT IN ('COMPLETED', 'EXPIRED', 'CANCELLED', 'DECLINED')")
    suspend fun expireOldChallenges(currentTime: Long)
    
    @Query("UPDATE challenges SET synced = :synced, lastSyncedAt = :timestamp WHERE challengeId = :challengeId")
    suspend fun updateSyncStatus(challengeId: String, synced: Boolean, timestamp: Long)
    
    @Query("SELECT * FROM challenges WHERE synced = 0")
    suspend fun getUnsyncedChallenges(): List<Challenge>
    
    @Query("DELETE FROM challenges WHERE challengeId = :challengeId")
    suspend fun deleteChallengeById(challengeId: String)
    
    @Query("DELETE FROM challenges WHERE groupId = :groupId")
    suspend fun deleteGroupChallenges(groupId: String)
    
    @Query("SELECT COUNT(*) FROM challenges WHERE challengedId = :memberId AND status = 'PENDING'")
    fun getPendingChallengeCount(memberId: String): Flow<Int>
}

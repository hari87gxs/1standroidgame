package com.athreya.mathworkout.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Daily Challenge operations
 */
@Dao
interface DailyChallengeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: DailyChallenge): Long
    
    @Update
    suspend fun update(challenge: DailyChallenge)
    
    @Query("SELECT * FROM daily_challenges WHERE date = :date LIMIT 1")
    suspend fun getChallengeForDate(date: String): DailyChallenge?
    
    @Query("SELECT * FROM daily_challenges WHERE date = :date LIMIT 1")
    fun getChallengeForDateFlow(date: String): Flow<DailyChallenge?>
    
    @Query("SELECT * FROM daily_challenges WHERE completed = 1 ORDER BY completedTimestamp DESC")
    fun getCompletedChallenges(): Flow<List<DailyChallenge>>
    
    @Query("SELECT COUNT(*) FROM daily_challenges WHERE completed = 1")
    suspend fun getCompletedChallengesCount(): Int
    
    @Query("SELECT * FROM daily_challenges ORDER BY date DESC LIMIT 30")
    fun getRecentChallenges(): Flow<List<DailyChallenge>>
    
    @Query("DELETE FROM daily_challenges WHERE date < :beforeDate")
    suspend fun deleteOldChallenges(beforeDate: String)
}

/**
 * DAO for Achievement operations
 */
@Dao
interface AchievementDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>)
    
    @Update
    suspend fun update(achievement: Achievement)
    
    @Query("SELECT * FROM achievements ORDER BY category, requirement")
    fun getAllAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE unlocked = 1 ORDER BY unlockedTimestamp DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE unlocked = 0 ORDER BY category, requirement")
    fun getLockedAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievementById(achievementId: String): Achievement?
    
    @Query("SELECT * FROM achievements WHERE category = :category")
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>>
    
    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    suspend fun getUnlockedCount(): Int
    
    @Query("SELECT SUM(xpReward) FROM achievements WHERE unlocked = 1")
    suspend fun getTotalXpEarned(): Int?
    
    @Query("UPDATE achievements SET progress = :progress WHERE id = :achievementId")
    suspend fun updateProgress(achievementId: String, progress: Int)
    
    @Query("UPDATE achievements SET unlocked = 1, unlockedTimestamp = :timestamp WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: String, timestamp: Long)
}

/**
 * DAO for Daily Streak operations
 */
@Dao
interface DailyStreakDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(streak: DailyStreak)
    
    @Update
    suspend fun update(streak: DailyStreak)
    
    @Query("SELECT * FROM daily_streak WHERE id = 1 LIMIT 1")
    suspend fun getStreak(): DailyStreak?
    
    @Query("SELECT * FROM daily_streak WHERE id = 1 LIMIT 1")
    fun getStreakFlow(): Flow<DailyStreak?>
    
    @Query("DELETE FROM daily_streak")
    suspend fun clearStreak()
}

/**
 * DAO for Timed Challenge operations
 */
@Dao
interface TimedChallengeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: TimedChallenge): Long
    
    @Query("SELECT * FROM timed_challenges WHERE completed = 1 ORDER BY timestamp DESC")
    fun getCompletedChallenges(): Flow<List<TimedChallenge>>
    
    @Query("SELECT * FROM timed_challenges WHERE gameMode = :gameMode AND difficulty = :difficulty AND completed = 1 ORDER BY actualTime ASC LIMIT 10")
    fun getBestTimesForMode(gameMode: String, difficulty: String): Flow<List<TimedChallenge>>
    
    @Query("SELECT * FROM timed_challenges WHERE completed = 1 ORDER BY actualTime ASC LIMIT 1")
    suspend fun getFastestChallenge(): TimedChallenge?
    
    @Query("SELECT COUNT(*) FROM timed_challenges WHERE completed = 1")
    suspend fun getCompletedCount(): Int
    
    @Query("SELECT AVG(actualTime) FROM timed_challenges WHERE gameMode = :gameMode AND completed = 1")
    suspend fun getAverageTimeForMode(gameMode: String): Long?
}

/**
 * DAO for Multiplayer Game operations
 */
@Dao
interface MultiplayerGameDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: MultiplayerGame)
    
    @Update
    suspend fun update(game: MultiplayerGame)
    
    @Query("SELECT * FROM multiplayer_games WHERE gameId = :gameId")
    suspend fun getGameById(gameId: String): MultiplayerGame?
    
    @Query("SELECT * FROM multiplayer_games WHERE gameId = :gameId")
    fun getGameByIdFlow(gameId: String): Flow<MultiplayerGame?>
    
    @Query("SELECT * FROM multiplayer_games WHERE status = 'WAITING' ORDER BY createdAt DESC")
    fun getWaitingGames(): Flow<List<MultiplayerGame>>
    
    @Query("SELECT * FROM multiplayer_games WHERE status = 'IN_PROGRESS' AND (hostPlayerId = :playerId OR opponentPlayerId = :playerId)")
    fun getActiveGamesForPlayer(playerId: String): Flow<List<MultiplayerGame>>
    
    @Query("SELECT * FROM multiplayer_games WHERE status = 'COMPLETED' AND (hostPlayerId = :playerId OR opponentPlayerId = :playerId) ORDER BY completedAt DESC LIMIT 20")
    fun getCompletedGamesForPlayer(playerId: String): Flow<List<MultiplayerGame>>
    
    @Query("SELECT COUNT(*) FROM multiplayer_games WHERE status = 'COMPLETED' AND winnerId = :playerId")
    suspend fun getWinCount(playerId: String): Int
    
    @Query("SELECT COUNT(*) FROM multiplayer_games WHERE status = 'COMPLETED' AND (hostPlayerId = :playerId OR opponentPlayerId = :playerId)")
    suspend fun getTotalGamesPlayed(playerId: String): Int
    
    @Query("DELETE FROM multiplayer_games WHERE status = 'COMPLETED' AND completedAt < :beforeTimestamp")
    suspend fun deleteOldGames(beforeTimestamp: Long)
}

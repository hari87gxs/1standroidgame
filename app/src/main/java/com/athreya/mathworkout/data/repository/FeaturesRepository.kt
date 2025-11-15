package com.athreya.mathworkout.data.repository

import com.athreya.mathworkout.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository for managing all new features: daily challenges, achievements, streaks, timed challenges, and multiplayer.
 */
class FeaturesRepository(
    private val dailyChallengeDao: DailyChallengeDao,
    private val achievementDao: AchievementDao,
    private val dailyStreakDao: DailyStreakDao,
    private val timedChallengeDao: TimedChallengeDao,
    private val multiplayerGameDao: MultiplayerGameDao,
    private val highScoreDao: HighScoreDao
) {
    
    // ==================== DAILY CHALLENGES ====================
    
    /**
     * Get or create today's daily challenge
     */
    suspend fun getTodaysChallenge(): DailyChallenge {
        val today = DailyChallenge.getTodayDate()
        return dailyChallengeDao.getChallengeForDate(today) ?: run {
            val challenge = DailyChallenge.generateToday()
            dailyChallengeDao.insert(challenge)
            challenge
        }
    }
    
    /**
     * Observe today's challenge
     */
    fun getTodaysChallengeFlow(): Flow<DailyChallenge?> {
        val today = DailyChallenge.getTodayDate()
        return dailyChallengeDao.getChallengeForDateFlow(today)
    }
    
    /**
     * Check if today's challenge has been completed
     */
    suspend fun isTodaysChallengeCompleted(): Boolean {
        val challenge = getTodaysChallenge()
        return challenge.completed
    }
    
    /**
     * Complete today's challenge
     * @param timeTaken Time taken in milliseconds
     * @param wrongAttempts Number of mistakes
     * @param questionsAnswered Total questions answered
     */
    suspend fun completeDailyChallenge(
        timeTaken: Long,
        wrongAttempts: Int,
        questionsAnswered: Int = 10
    ) {
        val challenge = getTodaysChallenge()
        if (!challenge.completed) {
            val updated = challenge.copy(
                completed = true,
                timeTaken = timeTaken,
                wrongAttempts = wrongAttempts,
                completedTimestamp = System.currentTimeMillis()
            )
            dailyChallengeDao.update(updated)
            
            // Update streak
            updateStreakOnGamePlayed()
            
            // Update achievements
            checkDailyChallengeAchievements()
        }
    }
    
    /**
     * Get all completed challenges
     */
    fun getCompletedChallenges(): Flow<List<DailyChallenge>> {
        return dailyChallengeDao.getCompletedChallenges()
    }
    
    /**
     * Get recent challenges
     */
    fun getRecentChallenges(): Flow<List<DailyChallenge>> {
        return dailyChallengeDao.getRecentChallenges()
    }
    
    // ==================== ACHIEVEMENTS ====================
    
    /**
     * Initialize achievements if not already done
     */
    suspend fun initializeAchievements() {
        val existing = achievementDao.getAllAchievements().first()
        if (existing.isEmpty()) {
            achievementDao.insertAll(AchievementDefinitions.getAllAchievements())
        }
    }
    
    /**
     * Get all achievements
     */
    fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements()
    }
    
    /**
     * Get unlocked achievements
     */
    fun getUnlockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievements()
    }
    
    /**
     * Get locked achievements
     */
    fun getLockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getLockedAchievements()
    }
    
    /**
     * Get achievements by category
     */
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>> {
        return achievementDao.getAchievementsByCategory(category)
    }
    
    /**
     * Update achievement progress
     */
    suspend fun updateAchievementProgress(achievementId: String, progress: Int) {
        val achievement = achievementDao.getAchievementById(achievementId)
        if (achievement != null && !achievement.unlocked && progress >= achievement.requirement) {
            achievementDao.unlockAchievement(achievementId, System.currentTimeMillis())
        } else {
            achievementDao.updateProgress(achievementId, progress)
        }
    }
    
    /**
     * Check and update achievements based on game completion
     */
    suspend fun checkAchievementsAfterGame(
        gameMode: String,
        difficulty: String,
        wrongAttempts: Int,
        timeTaken: Long,
        isMultiplayer: Boolean = false,
        wonMultiplayer: Boolean = false
    ) {
        // Games played achievements
        val totalGames = highScoreDao.getHighScoresCount()
        updateAchievementProgress("first_game", totalGames)
        updateAchievementProgress("games_10", totalGames)
        updateAchievementProgress("games_50", totalGames)
        updateAchievementProgress("games_100", totalGames)
        updateAchievementProgress("games_500", totalGames)
        
        // Perfect score achievements
        if (wrongAttempts == 0) {
            val perfectScores = highScoreDao.getPerfectScoresCount()
            updateAchievementProgress("perfect_first", perfectScores)
            updateAchievementProgress("perfect_10", perfectScores)
            updateAchievementProgress("perfect_50", perfectScores)
        }
        
        // Speed achievements
        val timeInSeconds = timeTaken / 1000
        if (timeInSeconds < 60) {
            updateAchievementProgress("speed_60s", 1)
        }
        if (timeInSeconds < 30) {
            updateAchievementProgress("speed_30s", 1)
        }
        
        // Difficulty achievements
        if (difficulty == "Hard") {
            val hardGames = highScoreDao.getHighScoresCountByDifficulty("Hard")
            updateAchievementProgress("hard_mode", hardGames)
            updateAchievementProgress("hard_mode_10", hardGames)
        }
        
        // Game mode specific achievements
        val modeGames = highScoreDao.getHighScoresCountByMode(gameMode)
        when (gameMode) {
            "Addition", "Subtraction" -> updateAchievementProgress("master_addition", modeGames)
            "Multiplication", "Division" -> updateAchievementProgress("master_multiplication", modeGames)
            "Sudoku" -> updateAchievementProgress("master_sudoku", modeGames)
        }
        
        // Multiplayer achievements
        if (isMultiplayer) {
            val multiplayerGames = multiplayerGameDao.getTotalGamesPlayed(getDeviceId())
            updateAchievementProgress("multiplayer_first", multiplayerGames)
            
            if (wonMultiplayer) {
                val wins = multiplayerGameDao.getWinCount(getDeviceId())
                updateAchievementProgress("multiplayer_win", wins)
                updateAchievementProgress("multiplayer_10", wins)
            }
        }
    }
    
    /**
     * Check daily challenge achievements
     */
    private suspend fun checkDailyChallengeAchievements() {
        val completedCount = dailyChallengeDao.getCompletedChallengesCount()
        updateAchievementProgress("daily_first", completedCount)
        updateAchievementProgress("daily_7", completedCount)
        updateAchievementProgress("daily_30", completedCount)
    }
    
    /**
     * Get total XP earned
     */
    suspend fun getTotalXpEarned(): Int {
        return achievementDao.getTotalXpEarned() ?: 0
    }
    
    // ==================== DAILY STREAK ====================
    
    /**
     * Get current streak
     */
    suspend fun getCurrentStreak(): DailyStreak {
        return dailyStreakDao.getStreak() ?: DailyStreak().also {
            dailyStreakDao.insert(it)
        }
    }
    
    /**
     * Observe current streak
     */
    fun getCurrentStreakFlow(): Flow<DailyStreak?> {
        return dailyStreakDao.getStreakFlow()
    }
    
    /**
     * Update streak when a game is played
     */
    suspend fun updateStreakOnGamePlayed() {
        val streak = getCurrentStreak()
        val updated = streak.updateWithTodayPlay()
        dailyStreakDao.update(updated)
        
        // Check streak achievements
        updateAchievementProgress("streak_3", updated.currentStreak)
        updateAchievementProgress("streak_7", updated.currentStreak)
        updateAchievementProgress("streak_30", updated.currentStreak)
    }
    
    /**
     * Get current streak multiplier based on whether today's challenge is completed
     * @return Multiplier value: 2.7x if today's challenge completed, then additional multiplier based on streak
     */
    suspend fun getStreakMultiplier(): Float {
        // Check if today's daily challenge has been completed
        val todayCompleted = isTodaysChallengeCompleted()
        
        if (!todayCompleted) {
            return 1.0f // No multiplier if today's challenge not completed
        }
        
        // Base multiplier for completing today's challenge
        var multiplier = 2.7f
        
        // Additional multiplier based on streak length
        val streak = getCurrentStreak()
        val streakBonus = when {
            streak.currentStreak >= 30 -> 1.3f
            streak.currentStreak >= 14 -> 1.2f
            streak.currentStreak >= 7 -> 1.15f
            streak.currentStreak >= 3 -> 1.1f
            else -> 1.0f
        }
        
        return multiplier * streakBonus
    }
    
    // ==================== TIMED CHALLENGES ====================
    
    /**
     * Save timed challenge result
     */
    suspend fun saveTimedChallenge(challenge: TimedChallenge): Long {
        return timedChallengeDao.insert(challenge)
    }
    
    /**
     * Get completed timed challenges
     */
    fun getCompletedTimedChallenges(): Flow<List<TimedChallenge>> {
        return timedChallengeDao.getCompletedChallenges()
    }
    
    /**
     * Get best times for a specific mode
     */
    fun getBestTimesForMode(gameMode: String, difficulty: String): Flow<List<TimedChallenge>> {
        return timedChallengeDao.getBestTimesForMode(gameMode, difficulty)
    }
    
    /**
     * Get fastest challenge overall
     */
    suspend fun getFastestChallenge(): TimedChallenge? {
        return timedChallengeDao.getFastestChallenge()
    }
    
    // ==================== MULTIPLAYER ====================
    
    /**
     * Create a new multiplayer game
     */
    suspend fun createMultiplayerGame(
        hostPlayerId: String,
        hostPlayerName: String,
        gameMode: String,
        difficulty: String
    ): MultiplayerGame {
        val gameId = "MP_${System.currentTimeMillis()}_${(0..9999).random()}"
        val game = MultiplayerGame(
            gameId = gameId,
            hostPlayerId = hostPlayerId,
            hostPlayerName = hostPlayerName,
            gameMode = gameMode,
            difficulty = difficulty,
            status = GameStatus.WAITING
        )
        multiplayerGameDao.insert(game)
        return game
    }
    
    /**
     * Join a multiplayer game
     */
    suspend fun joinMultiplayerGame(
        gameId: String,
        opponentPlayerId: String,
        opponentPlayerName: String
    ): MultiplayerGame? {
        val game = multiplayerGameDao.getGameById(gameId)
        return if (game != null && game.status == GameStatus.WAITING) {
            val updated = game.copy(
                opponentPlayerId = opponentPlayerId,
                opponentPlayerName = opponentPlayerName,
                status = GameStatus.IN_PROGRESS,
                startedAt = System.currentTimeMillis()
            )
            multiplayerGameDao.update(updated)
            updated
        } else {
            null
        }
    }
    
    /**
     * Update game score
     */
    suspend fun updateMultiplayerScore(
        gameId: String,
        playerId: String,
        score: Int,
        completed: Boolean
    ) {
        val game = multiplayerGameDao.getGameById(gameId) ?: return
        
        val updated = if (playerId == game.hostPlayerId) {
            game.copy(hostScore = score, hostCompleted = completed)
        } else {
            game.copy(opponentScore = score, opponentCompleted = completed)
        }
        
        // Check if game is complete
        val finalGame = if (updated.hostCompleted && updated.opponentCompleted) {
            val winnerId = when {
                updated.hostScore > updated.opponentScore -> updated.hostPlayerId
                updated.opponentScore > updated.hostScore -> updated.opponentPlayerId
                else -> null // Draw
            }
            updated.copy(
                status = GameStatus.COMPLETED,
                completedAt = System.currentTimeMillis(),
                winnerId = winnerId
            )
        } else {
            updated
        }
        
        multiplayerGameDao.update(finalGame)
    }
    
    /**
     * Get waiting games
     */
    fun getWaitingGames(): Flow<List<MultiplayerGame>> {
        return multiplayerGameDao.getWaitingGames()
    }
    
    /**
     * Get active games for a player
     */
    fun getActiveGamesForPlayer(playerId: String): Flow<List<MultiplayerGame>> {
        return multiplayerGameDao.getActiveGamesForPlayer(playerId)
    }
    
    /**
     * Get completed games for a player
     */
    fun getCompletedGamesForPlayer(playerId: String): Flow<List<MultiplayerGame>> {
        return multiplayerGameDao.getCompletedGamesForPlayer(playerId)
    }
    
    /**
     * Observe a specific game
     */
    fun observeGame(gameId: String): Flow<MultiplayerGame?> {
        return multiplayerGameDao.getGameByIdFlow(gameId)
    }
    
    /**
     * Get multiplayer win rate
     */
    suspend fun getMultiplayerWinRate(playerId: String): Float {
        val wins = multiplayerGameDao.getWinCount(playerId)
        val total = multiplayerGameDao.getTotalGamesPlayed(playerId)
        return if (total > 0) wins.toFloat() / total.toFloat() else 0f
    }
    
    // ==================== HELPER METHODS ====================
    
    private suspend fun getDeviceId(): String {
        // Get from user preferences or generate
        return "device_${System.currentTimeMillis()}" // Placeholder
    }
}

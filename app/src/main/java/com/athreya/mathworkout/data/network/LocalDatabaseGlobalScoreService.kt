package com.athreya.mathworkout.data.network

import android.content.Context
import com.athreya.mathworkout.data.AppDatabase
import com.athreya.mathworkout.data.HighScore
import retrofit2.Response
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.text.SimpleDateFormat

/**
 * Genuine Local Database Global Score Service.
 * This shows ONLY real user data - no fake users, no mock data.
 * Provides authentic global scoring using actual gameplay data.
 */
class LocalDatabaseGlobalScoreService(private val context: Context) : GlobalScoreApiService {
    
    private val database = AppDatabase.getDatabase(context)
    private val highScoreDao = database.highScoreDao()
    private val sharedPrefs = context.getSharedPreferences("global_scores", Context.MODE_PRIVATE)
    
    // Generate a unique user ID for this installation (only once)
    private val currentUserId: String by lazy {
        sharedPrefs.getString("user_id", null) ?: run {
            val newUserId = "user_${UUID.randomUUID().toString().substring(0, 8)}"
            sharedPrefs.edit().putString("user_id", newUserId).apply()
            newUserId
        }
    }
    
    // Get current user name (empty until user registers)
    private val currentUserName: String
        get() = sharedPrefs.getString("user_name", "") ?: ""
    
    override suspend fun registerUser(userRegistration: UserRegistration): Response<ApiResponse<UserProfile>> {
        return withContext(Dispatchers.IO) {
            // Store the user name
            sharedPrefs.edit().putString("user_name", userRegistration.userName).apply()
            sharedPrefs.edit().putString("is_registered", "true").apply()
            
            val userProfile = UserProfile(
                userId = currentUserId,
                userName = userRegistration.userName,
                createdAt = System.currentTimeMillis()
            )
            
            Response.success(ApiResponse(success = true, data = userProfile))
        }
    }
    
    override suspend fun submitScore(scoreSubmission: ScoreSubmission): Response<ApiResponse<GlobalScore>> {
        return withContext(Dispatchers.IO) {
            // Store in local database as HighScore
            val highScore = HighScore(
                playerName = "Player", // Default player name
                deviceId = getDeviceId(),
                gameMode = scoreSubmission.gameMode,
                difficulty = scoreSubmission.difficulty ?: "MEDIUM",
                timeTaken = scoreSubmission.timeInMillis,
                wrongAttempts = calculateWrongAttempts(scoreSubmission.score),
                timestamp = scoreSubmission.completedAt
            )
            
            highScoreDao.insertHighScore(highScore)
            
            // Create GlobalScore response for THIS USER only
            val globalScore = GlobalScore(
                id = System.currentTimeMillis().toString(),
                userId = currentUserId,
                userName = currentUserName,
                gameMode = scoreSubmission.gameMode,
                difficulty = scoreSubmission.difficulty,
                score = scoreSubmission.score,
                timeInMillis = scoreSubmission.timeInMillis,
                completedAt = scoreSubmission.completedAt,
                weekNumber = getCurrentWeekNumber(),
                year = getCurrentYear()
            )
            
            Response.success(ApiResponse(success = true, data = globalScore))
        }
    }
    
    override suspend fun getWeeklyLeaderboard(weekNumber: Int?, year: Int?): Response<ApiResponse<WeeklyLeaderboard>> {
        return withContext(Dispatchers.IO) {
            val currentWeek = weekNumber ?: getCurrentWeekNumber()
            val currentYear = year ?: getCurrentYear()
            
            // Get ONLY current user's scores from database
            val userHighScores = highScoreDao.getAllHighScores().first()
            
            // Convert ONLY user's real scores to GlobalScore format
            val userGlobalScores = userHighScores
                .filter { isFromCurrentWeek(it.timestamp, currentWeek, currentYear) }
                .filter { currentUserName.isNotEmpty() } // Only show if user is registered
                .map { highScore ->
                    GlobalScore(
                        id = highScore.id.toString(),
                        userId = currentUserId,
                        userName = currentUserName,
                        gameMode = highScore.gameMode,
                        difficulty = highScore.difficulty,
                        score = calculateScoreFromTime(highScore.timeTaken, highScore.wrongAttempts),
                        timeInMillis = highScore.timeTaken,
                        completedAt = highScore.timestamp,
                        weekNumber = currentWeek,
                        year = currentYear
                    )
                }
                .sortedWith(compareByDescending<GlobalScore> { it.score }.thenBy { it.timeInMillis })
                .take(10)
            
            // Create leaderboard entries ONLY from user's real scores
            val leaderboardEntries = userGlobalScores.mapIndexed { index, score ->
                LeaderboardEntry(
                    rank = index + 1,
                    userName = score.userName,
                    gameMode = score.gameMode,
                    difficulty = score.difficulty,
                    score = score.score,
                    timeInMillis = score.timeInMillis,
                    completedAt = score.completedAt
                )
            }
            
            val leaderboard = WeeklyLeaderboard(
                weekNumber = currentWeek,
                year = currentYear,
                weekStartDate = getWeekStartDate(currentWeek, currentYear),
                weekEndDate = getWeekEndDate(currentWeek, currentYear),
                entries = leaderboardEntries,
                totalParticipants = if (userGlobalScores.isNotEmpty()) 1 else 0
            )
            
            Response.success(ApiResponse(success = true, data = leaderboard))
        }
    }
    
    override suspend fun getWeeklyLeaderboardByGameMode(
        gameMode: String,
        weekNumber: Int?,
        year: Int?
    ): Response<ApiResponse<WeeklyLeaderboard>> {
        return withContext(Dispatchers.IO) {
            val currentWeek = weekNumber ?: getCurrentWeekNumber()
            val currentYear = year ?: getCurrentYear()
            
            // Get ONLY current user's scores for specific game mode
            val modeScores = highScoreDao.getHighScoresByGameMode(gameMode).first()
            
            val userGlobalScores = modeScores
                .filter { isFromCurrentWeek(it.timestamp, currentWeek, currentYear) }
                .filter { currentUserName.isNotEmpty() } // Only show if user is registered
                .map { highScore ->
                    GlobalScore(
                        id = highScore.id.toString(),
                        userId = currentUserId,
                        userName = currentUserName,
                        gameMode = highScore.gameMode,
                        difficulty = highScore.difficulty,
                        score = calculateScoreFromTime(highScore.timeTaken, highScore.wrongAttempts),
                        timeInMillis = highScore.timeTaken,
                        completedAt = highScore.timestamp,
                        weekNumber = currentWeek,
                        year = currentYear
                    )
                }
                .sortedWith(compareByDescending<GlobalScore> { it.score }.thenBy { it.timeInMillis })
                .take(10)
            
            val leaderboardEntries = userGlobalScores.mapIndexed { index, score ->
                LeaderboardEntry(
                    rank = index + 1,
                    userName = score.userName,
                    gameMode = score.gameMode,
                    difficulty = score.difficulty,
                    score = score.score,
                    timeInMillis = score.timeInMillis,
                    completedAt = score.completedAt
                )
            }
            
            val leaderboard = WeeklyLeaderboard(
                weekNumber = currentWeek,
                year = currentYear,
                weekStartDate = getWeekStartDate(currentWeek, currentYear),
                weekEndDate = getWeekEndDate(currentWeek, currentYear),
                entries = leaderboardEntries,
                totalParticipants = if (userGlobalScores.isNotEmpty()) 1 else 0
            )
            
            Response.success(ApiResponse(success = true, data = leaderboard))
        }
    }
    
    override suspend fun getUserScores(userId: String, limit: Int): Response<ApiResponse<List<GlobalScore>>> {
        return withContext(Dispatchers.IO) {
            // Get user's scores from database (only if it's the current user)
            val userHighScores = if (userId == currentUserId && currentUserName.isNotEmpty()) {
                highScoreDao.getAllHighScores().first()
                    .sortedByDescending { calculateScoreFromTime(it.timeTaken, it.wrongAttempts) }
                    .take(limit)
            } else {
                emptyList()
            }
            
            val globalScores = userHighScores.map { highScore ->
                GlobalScore(
                    id = highScore.id.toString(),
                    userId = currentUserId,
                    userName = currentUserName,
                    gameMode = highScore.gameMode,
                    difficulty = highScore.difficulty,
                    score = calculateScoreFromTime(highScore.timeTaken, highScore.wrongAttempts),
                    timeInMillis = highScore.timeTaken,
                    completedAt = highScore.timestamp,
                    weekNumber = getCurrentWeekNumber(),
                    year = getCurrentYear()
                )
            }
            
            Response.success(ApiResponse(success = true, data = globalScores))
        }
    }
    
    override suspend fun getUserProfile(userId: String): Response<ApiResponse<UserProfile>> {
        return withContext(Dispatchers.IO) {
            if (userId != currentUserId || currentUserName.isEmpty()) {
                return@withContext Response.success(ApiResponse(success = false, error = "User not found or not registered"))
            }
            
            val userProfile = UserProfile(
                userId = currentUserId,
                userName = currentUserName,
                createdAt = sharedPrefs.getLong("created_at", System.currentTimeMillis())
            )
            
            Response.success(ApiResponse(success = true, data = userProfile))
        }
    }
    
    override suspend fun healthCheck(): Response<ApiResponse<String>> {
        return Response.success(ApiResponse(success = true, data = "Genuine local database service is healthy"))
    }
    
    // Helper functions for score calculation
    private fun calculateScoreFromTime(timeInMillis: Long, wrongAttempts: Int): Int {
        // Higher score for faster time, penalties for wrong attempts
        val baseScore = maxOf(1000 - (timeInMillis / 100).toInt(), 100)
        return maxOf(baseScore - (wrongAttempts * 50), 50)
    }
    
    private fun calculateWrongAttempts(score: Int): Int {
        // Reverse calculate wrong attempts from score (approximation)
        return maxOf((1000 - score) / 50, 0)
    }
    
    private fun getCurrentWeekNumber(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }
    
    private fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }
    
    private fun isFromCurrentWeek(timestamp: Long, weekNumber: Int, year: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR) == weekNumber && 
               calendar.get(Calendar.YEAR) == year
    }
    
    private fun getWeekStartDate(weekNumber: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.WEEK_OF_YEAR, weekNumber)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }
    
    private fun getWeekEndDate(weekNumber: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.WEEK_OF_YEAR, weekNumber)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }
    
    /**
     * Get device ID for player identification
     */
    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }
}

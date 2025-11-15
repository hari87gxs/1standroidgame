package com.athreya.mathworkout.data.network

import com.athreya.mathworkout.data.network.ApiResponse
import com.athreya.mathworkout.data.network.ScoreSubmission
import com.athreya.mathworkout.data.network.UserRegistration
import com.athreya.mathworkout.data.network.WeeklyLeaderboard
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for global score functionality.
 * 
 * This interface defines all the endpoints needed for:
 * - User registration
 * - Score submission
 * - Leaderboard retrieval
 * - User profile management
 */
interface GlobalScoreApiService {
    
    /**
     * Register a new user with the global scoring system.
     */
    @POST("users/register")
    suspend fun registerUser(
        @Body userRegistration: UserRegistration
    ): Response<ApiResponse<UserProfile>>
    
    /**
     * Submit a new score to the global leaderboard.
     */
    @POST("scores/submit")
    suspend fun submitScore(
        @Body scoreSubmission: ScoreSubmission
    ): Response<ApiResponse<GlobalScore>>
    
    /**
     * Get the current week's leaderboard for all game modes.
     */
    @GET("leaderboard/weekly")
    suspend fun getWeeklyLeaderboard(
        @Query("week") weekNumber: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<WeeklyLeaderboard>>
    
    /**
     * Get the weekly leaderboard for a specific game mode.
     */
    @GET("leaderboard/weekly/{gameMode}")
    suspend fun getWeeklyLeaderboardByGameMode(
        @Path("gameMode") gameMode: String,
        @Query("week") weekNumber: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<WeeklyLeaderboard>>
    
    /**
     * Get user's personal best scores.
     */
    @GET("users/{userId}/scores")
    suspend fun getUserScores(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<GlobalScore>>>
    
    /**
     * Get user profile information.
     */
    @GET("users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): Response<ApiResponse<UserProfile>>
    
    /**
     * Health check endpoint to verify API connectivity.
     */
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<String>>
}

/**
 * Mock API service for development/testing.
 * This simulates the backend functionality locally.
 */
class MockGlobalScoreApiService : GlobalScoreApiService {
    
    private val mockUsers = mutableMapOf<String, UserProfile>()
    private val mockScores = mutableListOf<GlobalScore>()
    
    override suspend fun registerUser(userRegistration: UserRegistration): Response<ApiResponse<UserProfile>> {
        val userProfile = UserProfile(
            userId = userRegistration.deviceId,
            userName = userRegistration.userName
        )
        mockUsers[userRegistration.deviceId] = userProfile
        
        return Response.success(ApiResponse(success = true, data = userProfile))
    }
    
    override suspend fun submitScore(scoreSubmission: ScoreSubmission): Response<ApiResponse<GlobalScore>> {
        val globalScore = GlobalScore(
            id = "${mockScores.size + 1}",
            userId = scoreSubmission.userId,
            userName = scoreSubmission.userName,
            gameMode = scoreSubmission.gameMode,
            difficulty = scoreSubmission.difficulty,
            score = scoreSubmission.score,
            timeInMillis = scoreSubmission.timeInMillis,
            completedAt = scoreSubmission.completedAt,
            weekNumber = ScoreUtils.getCurrentWeekNumber(),
            year = ScoreUtils.getCurrentYear()
        )
        mockScores.add(globalScore)
        
        return Response.success(ApiResponse(success = true, data = globalScore))
    }
    
    override suspend fun getWeeklyLeaderboard(
        weekNumber: Int?,
        year: Int?
    ): Response<ApiResponse<WeeklyLeaderboard>> {
        val currentWeek = weekNumber ?: ScoreUtils.getCurrentWeekNumber()
        val currentYear = year ?: ScoreUtils.getCurrentYear()
        
        // Filter scores for the current week and sort by score (descending) then time (ascending)
        val weeklyScores = mockScores
            .filter { it.weekNumber == currentWeek && it.year == currentYear }
            .sortedWith(compareByDescending<GlobalScore> { it.score }.thenBy { it.timeInMillis })
            .take(10)
        
        val leaderboardEntries = weeklyScores.mapIndexed { index, score ->
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
            weekStartDate = "Nov 11, 2024",
            weekEndDate = "Nov 17, 2024",
            entries = leaderboardEntries,
            totalParticipants = mockScores.filter { it.weekNumber == currentWeek && it.year == currentYear }
                .map { it.userId }.distinct().size
        )
        
        return Response.success(ApiResponse(success = true, data = leaderboard))
    }
    
    override suspend fun getWeeklyLeaderboardByGameMode(
        gameMode: String,
        weekNumber: Int?,
        year: Int?
    ): Response<ApiResponse<WeeklyLeaderboard>> {
        val currentWeek = weekNumber ?: ScoreUtils.getCurrentWeekNumber()
        val currentYear = year ?: ScoreUtils.getCurrentYear()
        
        // Filter by game mode as well
        val weeklyScores = mockScores
            .filter { 
                it.weekNumber == currentWeek && 
                it.year == currentYear && 
                it.gameMode.equals(gameMode, ignoreCase = true) 
            }
            .sortedWith(compareByDescending<GlobalScore> { it.score }.thenBy { it.timeInMillis })
            .take(10)
        
        val leaderboardEntries = weeklyScores.mapIndexed { index, score ->
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
            weekStartDate = "Nov 11, 2024",
            weekEndDate = "Nov 17, 2024",
            entries = leaderboardEntries,
            totalParticipants = mockScores.filter { 
                it.weekNumber == currentWeek && 
                it.year == currentYear && 
                it.gameMode.equals(gameMode, ignoreCase = true) 
            }.map { it.userId }.distinct().size
        )
        
        return Response.success(ApiResponse(success = true, data = leaderboard))
    }
    
    override suspend fun getUserScores(userId: String, limit: Int): Response<ApiResponse<List<GlobalScore>>> {
        val userScores = mockScores
            .filter { it.userId == userId }
            .sortedByDescending { it.score }
            .take(limit)
        
        return Response.success(ApiResponse(success = true, data = userScores))
    }
    
    override suspend fun getUserProfile(userId: String): Response<ApiResponse<UserProfile>> {
        val userProfile = mockUsers[userId]
        return if (userProfile != null) {
            Response.success(ApiResponse(success = true, data = userProfile))
        } else {
            Response.success(ApiResponse(success = false, error = "User not found"))
        }
    }
    
    override suspend fun healthCheck(): Response<ApiResponse<String>> {
        return Response.success(ApiResponse(success = true, data = "API is healthy"))
    }
    
    init {
        // Add some mock data for development
        addMockData()
    }
    
    private fun addMockData() {
        // Mock data removed - app will only show real scores
        // No sample users or scores will be added
    }
}

package com.athreya.mathworkout.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages progress tracking for Math Tricks
 * Stores completion status, best scores, and unlocked tricks
 */
class TrickProgressManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "trick_progress",
        Context.MODE_PRIVATE
    )
    
    private val _progressState = MutableStateFlow<Map<String, TrickProgress>>(emptyMap())
    val progressState: StateFlow<Map<String, TrickProgress>> = _progressState.asStateFlow()
    
    init {
        loadProgress()
    }
    
    /**
     * Get progress for a specific trick
     */
    fun getTrickProgress(trickId: String): TrickProgress {
        return _progressState.value[trickId] ?: TrickProgress(
            trickId = trickId,
            isCompleted = false,
            bestScore = 0,
            totalAttempts = 0,
            lastAttemptDate = 0L
        )
    }
    
    /**
     * Save practice results and update progress
     */
    fun savePracticeResult(trickId: String, score: Int, totalQuestions: Int) {
        val currentProgress = getTrickProgress(trickId)
        
        // Update progress
        val newProgress = currentProgress.copy(
            isCompleted = score >= (totalQuestions * 0.7).toInt(), // 70% to complete
            bestScore = maxOf(currentProgress.bestScore, score),
            totalAttempts = currentProgress.totalAttempts + 1,
            lastAttemptDate = System.currentTimeMillis(),
            totalQuestions = totalQuestions
        )
        
        // Save to preferences
        prefs.edit().apply {
            putBoolean("${trickId}_completed", newProgress.isCompleted)
            putInt("${trickId}_best_score", newProgress.bestScore)
            putInt("${trickId}_total_attempts", newProgress.totalAttempts)
            putLong("${trickId}_last_attempt", newProgress.lastAttemptDate)
            putInt("${trickId}_total_questions", newProgress.totalQuestions)
            apply()
        }
        
        // Update state
        val updatedMap = _progressState.value.toMutableMap()
        updatedMap[trickId] = newProgress
        _progressState.value = updatedMap
    }
    
    /**
     * Check if a trick is unlocked based on progression system
     * First 2 tricks are always unlocked, others unlock after completing previous trick
     */
    fun isTrickUnlocked(trickId: String): Boolean {
        val allTricks = MathTricks.getAllTricks()
        val trickIndex = allTricks.indexOfFirst { it.id == trickId }
        
        // First 2 tricks are always unlocked
        if (trickIndex < 2) return true
        
        // Check if previous trick is completed
        val previousTrick = allTricks.getOrNull(trickIndex - 1)
        return previousTrick?.let { isTrickCompleted(it.id) } ?: false
    }
    
    /**
     * Get the next locked trick
     */
    fun getNextLockedTrick(): MathTrick? {
        return MathTricks.getAllTricks().firstOrNull { !isTrickUnlocked(it.id) }
    }
    
    /**
     * Get total number of unlocked tricks
     */
    fun getUnlockedTricksCount(): Int {
        return MathTricks.getAllTricks().count { isTrickUnlocked(it.id) }
    }
    
    /**
     * Check if a trick is completed
     */
    fun isTrickCompleted(trickId: String): Boolean {
        return prefs.getBoolean("${trickId}_completed", false)
    }
    
    /**
     * Get best score for a trick
     */
    fun getBestScore(trickId: String): Int {
        return prefs.getInt("${trickId}_best_score", 0)
    }
    
    /**
     * Get total number of completed tricks
     */
    fun getCompletedTricksCount(): Int {
        return MathTricks.getAllTricks().count { trick ->
            isTrickCompleted(trick.id)
        }
    }
    
    /**
     * Get completion percentage
     */
    fun getOverallProgress(): Int {
        val total = MathTricks.getAllTricks().size
        val completed = getCompletedTricksCount()
        return if (total > 0) (completed * 100 / total) else 0
    }
    
    /**
     * Reset progress for a specific trick
     */
    fun resetTrickProgress(trickId: String) {
        prefs.edit().apply {
            remove("${trickId}_completed")
            remove("${trickId}_best_score")
            remove("${trickId}_total_attempts")
            remove("${trickId}_last_attempt")
            remove("${trickId}_total_questions")
            apply()
        }
        
        val updatedMap = _progressState.value.toMutableMap()
        updatedMap.remove(trickId)
        _progressState.value = updatedMap
    }
    
    /**
     * Reset all progress
     */
    fun resetAllProgress() {
        prefs.edit().clear().apply()
        _progressState.value = emptyMap()
    }
    
    /**
     * Load all progress from preferences
     */
    private fun loadProgress() {
        val progressMap = mutableMapOf<String, TrickProgress>()
        
        MathTricks.getAllTricks().forEach { trick ->
            val progress = TrickProgress(
                trickId = trick.id,
                isCompleted = prefs.getBoolean("${trick.id}_completed", false),
                bestScore = prefs.getInt("${trick.id}_best_score", 0),
                totalAttempts = prefs.getInt("${trick.id}_total_attempts", 0),
                lastAttemptDate = prefs.getLong("${trick.id}_last_attempt", 0L),
                totalQuestions = prefs.getInt("${trick.id}_total_questions", 10)
            )
            
            if (progress.totalAttempts > 0) {
                progressMap[trick.id] = progress
            }
        }
        
        _progressState.value = progressMap
    }
    
    /**
     * Calculate XP earned from a practice session
     */
    fun calculateXP(score: Int, totalQuestions: Int, isFirstTime: Boolean): Int {
        val baseXP = score * 10 // 10 XP per correct answer
        val bonusXP = if (score == totalQuestions) 50 else 0 // Perfect score bonus
        val firstTimeBonus = if (isFirstTime) 100 else 0 // First completion bonus
        
        return baseXP + bonusXP + firstTimeBonus
    }
    
    /**
     * Check for milestone achievements
     */
    fun checkMilestones(): List<TrickMilestone> {
        val milestones = mutableListOf<TrickMilestone>()
        val completedCount = getCompletedTricksCount()
        val perfectScores = MathTricks.getAllTricks().count { trick ->
            val progress = getTrickProgress(trick.id)
            progress.bestScore == progress.totalQuestions
        }
        
        // Completion milestones
        when (completedCount) {
            1 -> milestones.add(TrickMilestone("first_trick", "First Trick Mastered! 🎯", "You completed your first math trick!"))
            3 -> milestones.add(TrickMilestone("three_tricks", "Triple Threat! 🔥", "You've mastered 3 tricks!"))
            5 -> milestones.add(TrickMilestone("five_tricks", "High Five! ✋", "5 tricks completed!"))
            8 -> milestones.add(TrickMilestone("almost_there", "Almost There! 🚀", "Just 3 more tricks to go!"))
            11 -> milestones.add(TrickMilestone("all_tricks", "TRICK MASTER! 🏆", "You've mastered ALL tricks!"))
        }
        
        // Perfect score milestones
        when (perfectScores) {
            1 -> milestones.add(TrickMilestone("first_perfect", "Perfect! 💯", "Your first perfect score!"))
            5 -> milestones.add(TrickMilestone("perfectionist", "Perfectionist! ⭐", "5 perfect scores!"))
            11 -> milestones.add(TrickMilestone("flawless", "FLAWLESS! 👑", "Perfect on ALL tricks!"))
        }
        
        return milestones
    }
}

/**
 * Milestone achievement data
 */
data class TrickMilestone(
    val id: String,
    val title: String,
    val description: String
)

/**
 * Data class representing progress for a single trick
 */
data class TrickProgress(
    val trickId: String,
    val isCompleted: Boolean,
    val bestScore: Int,
    val totalAttempts: Int,
    val lastAttemptDate: Long,
    val totalQuestions: Int = 10
) {
    fun getScorePercentage(): Int {
        return if (totalQuestions > 0) (bestScore * 100 / totalQuestions) else 0
    }
}

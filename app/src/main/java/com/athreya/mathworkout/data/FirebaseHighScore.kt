package com.athreya.mathworkout.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firebase Firestore representation of a high score.
 * This class is optimized for cloud storage and global leaderboards.
 * 
 * Firestore requires no-argument constructors and mutable properties
 * for automatic serialization/deserialization.
 * 
 * @property documentId Auto-generated Firestore document ID
 * @property playerName Player's display name for leaderboard
 * @property deviceId Unique device identifier
 * @property gameMode Game mode (ADDITION_SUBTRACTION, MULTIPLICATION_DIVISION, etc.)
 * @property difficulty Difficulty level (EASY, MEDIUM, COMPLEX)
 * @property timeTaken Total time in milliseconds including penalties
 * @property wrongAttempts Number of incorrect answers
 * @property points Base points before multiplier
 * @property bonusMultiplier Bonus multiplier (1.0 for regular, 1.5-3.0 for daily challenges)
 * @property finalScore Final score after applying multiplier
 * @property isDailyChallenge Whether this was a daily challenge
 * @property timestamp Server timestamp when score was uploaded
 * @property localTimestamp Original timestamp from device
 */
data class FirebaseHighScore(
    @DocumentId
    var documentId: String = "",
    var playerName: String = "",
    var deviceId: String = "",
    var gameMode: String = "",
    var difficulty: String = "",
    var timeTaken: Long = 0L,
    var wrongAttempts: Int = 0,
    var points: Int = 0,
    var bonusMultiplier: Float = 1.0f,
    var finalScore: Int = 0,
    var isDailyChallenge: Boolean = false,
    @ServerTimestamp
    var timestamp: Date? = null,
    var localTimestamp: Long = 0L
) {
    companion object {
        /**
         * Convert Room HighScore to Firebase format
         */
        fun fromHighScore(highScore: HighScore): FirebaseHighScore {
            return FirebaseHighScore(
                documentId = highScore.firebaseId ?: "",
                playerName = highScore.playerName,
                deviceId = highScore.deviceId,
                gameMode = highScore.gameMode,
                difficulty = highScore.difficulty,
                timeTaken = highScore.timeTaken,
                wrongAttempts = highScore.wrongAttempts,
                points = highScore.points,
                bonusMultiplier = highScore.bonusMultiplier,
                finalScore = highScore.finalScore,
                isDailyChallenge = highScore.isDailyChallenge,
                localTimestamp = highScore.timestamp
            )
        }
        
        /**
         * Convert Firebase format to Room HighScore
         */
        fun toHighScore(firebaseScore: FirebaseHighScore): HighScore {
            return HighScore(
                firebaseId = firebaseScore.documentId,
                playerName = firebaseScore.playerName,
                deviceId = firebaseScore.deviceId,
                gameMode = firebaseScore.gameMode,
                difficulty = firebaseScore.difficulty,
                timeTaken = firebaseScore.timeTaken,
                wrongAttempts = firebaseScore.wrongAttempts,
                points = firebaseScore.points,
                bonusMultiplier = firebaseScore.bonusMultiplier,
                finalScore = firebaseScore.finalScore,
                isDailyChallenge = firebaseScore.isDailyChallenge,
                timestamp = firebaseScore.localTimestamp,
                synced = true,
                isGlobal = true
            )
        }
    }
}
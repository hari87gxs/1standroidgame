package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a multiplayer game session.
 * Multiplayer games allow real-time competition with friends.
 */
@Entity(tableName = "multiplayer_games")
data class MultiplayerGame(
    @PrimaryKey
    val gameId: String, // Firebase game session ID
    val hostPlayerId: String,
    val hostPlayerName: String,
    val opponentPlayerId: String? = null,
    val opponentPlayerName: String? = null,
    val gameMode: String,
    val difficulty: String,
    val status: GameStatus,
    val hostScore: Int = 0,
    val opponentScore: Int = 0,
    val hostCompleted: Boolean = false,
    val opponentCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val winnerId: String? = null
)

enum class GameStatus {
    WAITING,      // Waiting for opponent to join
    IN_PROGRESS,  // Game is active
    COMPLETED,    // Game finished
    CANCELLED     // Game cancelled
}

/**
 * Firebase model for multiplayer game data
 */
data class MultiplayerGameFirebase(
    val gameId: String = "",
    val hostPlayerId: String = "",
    val hostPlayerName: String = "",
    val opponentPlayerId: String? = null,
    val opponentPlayerName: String? = null,
    val gameMode: String = "",
    val difficulty: String = "",
    val status: String = GameStatus.WAITING.name,
    val hostScore: Int = 0,
    val opponentScore: Int = 0,
    val hostCompleted: Boolean = false,
    val opponentCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val winnerId: String? = null
) {
    /**
     * Convert to Room entity
     */
    fun toMultiplayerGame(): MultiplayerGame {
        return MultiplayerGame(
            gameId = gameId,
            hostPlayerId = hostPlayerId,
            hostPlayerName = hostPlayerName,
            opponentPlayerId = opponentPlayerId,
            opponentPlayerName = opponentPlayerName,
            gameMode = gameMode,
            difficulty = difficulty,
            status = GameStatus.valueOf(status),
            hostScore = hostScore,
            opponentScore = opponentScore,
            hostCompleted = hostCompleted,
            opponentCompleted = opponentCompleted,
            createdAt = createdAt,
            startedAt = startedAt,
            completedAt = completedAt,
            winnerId = winnerId
        )
    }
    
    companion object {
        /**
         * Convert from Room entity
         */
        fun fromMultiplayerGame(game: MultiplayerGame): MultiplayerGameFirebase {
            return MultiplayerGameFirebase(
                gameId = game.gameId,
                hostPlayerId = game.hostPlayerId,
                hostPlayerName = game.hostPlayerName,
                opponentPlayerId = game.opponentPlayerId,
                opponentPlayerName = game.opponentPlayerName,
                gameMode = game.gameMode,
                difficulty = game.difficulty,
                status = game.status.name,
                hostScore = game.hostScore,
                opponentScore = game.opponentScore,
                hostCompleted = game.hostCompleted,
                opponentCompleted = game.opponentCompleted,
                createdAt = game.createdAt,
                startedAt = game.startedAt,
                completedAt = game.completedAt,
                winnerId = game.winnerId
            )
        }
    }
}

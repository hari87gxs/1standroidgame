package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a high score record.
 * This class defines the structure of our high scores database table.
 * 
 * Room is Android's database abstraction layer over SQLite.
 * The @Entity annotation marks this class as a database table.
 * 
 * @param id Auto-generated primary key for the database record
 * @param gameMode The game mode this score was achieved in
 * @param difficulty The difficulty level this score was achieved on
 * @param timeTaken The total time taken in milliseconds (including penalties)
 * @param wrongAttempts Number of incorrect answers during the game
 * @param timestamp When this score was achieved (in milliseconds since epoch)
 */
@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: String,
    val difficulty: String,
    val timeTaken: Long, // in milliseconds
    val wrongAttempts: Int,
    val timestamp: Long = System.currentTimeMillis()
)
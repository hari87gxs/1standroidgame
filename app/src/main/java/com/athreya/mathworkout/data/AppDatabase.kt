package com.athreya.mathworkout.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.athreya.mathworkout.data.social.Group
import com.athreya.mathworkout.data.social.GroupMember
import com.athreya.mathworkout.data.social.Challenge
import com.athreya.mathworkout.data.social.GroupDao
import com.athreya.mathworkout.data.social.GroupMemberDao
import com.athreya.mathworkout.data.social.ChallengeDao

/**
 * Room Database class that serves as the main access point for the persisted data.
 * 
 * The @Database annotation includes:
 * - entities: List of all entities (tables) in this database
 * - version: Database version number (increment when making schema changes)
 * - exportSchema: Whether to export the database schema to a file
 * 
 * This class follows the Singleton pattern to ensure only one instance exists.
 */
@Database(
    entities = [
        HighScore::class,
        DailyChallenge::class,
        Achievement::class,
        DailyStreak::class,
        TimedChallenge::class,
        MultiplayerGame::class,
        Group::class,
        GroupMember::class,
        Challenge::class,
        com.athreya.mathworkout.data.avatar.Avatar::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Abstract function that returns the DAO for high scores.
     * Room will implement this function automatically.
     */
    abstract fun highScoreDao(): HighScoreDao
    abstract fun dailyChallengeDao(): DailyChallengeDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyStreakDao(): DailyStreakDao
    abstract fun timedChallengeDao(): TimedChallengeDao
    abstract fun multiplayerGameDao(): MultiplayerGameDao
    
    // Social feature DAOs
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun challengeDao(): ChallengeDao
    
    // Avatar DAO
    abstract fun avatarDao(): com.athreya.mathworkout.data.avatar.AvatarDao
    
    companion object {
        /**
         * Migration from version 1 to version 2.
         * Adds new columns for global leaderboard functionality.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns with default values
                database.execSQL("ALTER TABLE high_scores ADD COLUMN firebaseId TEXT")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN playerName TEXT NOT NULL DEFAULT 'Anonymous'")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN deviceId TEXT NOT NULL DEFAULT 'unknown'")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN isGlobal INTEGER NOT NULL DEFAULT 1")
            }
        }
        
        /**
         * Migration from version 2 to version 3.
         * Adds new tables for enhanced features: daily challenges, achievements, streaks, timed challenges, and multiplayer.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create daily_challenges table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_challenges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        gameMode TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        bonusMultiplier REAL NOT NULL,
                        completed INTEGER NOT NULL,
                        timeTaken INTEGER NOT NULL,
                        wrongAttempts INTEGER NOT NULL,
                        completedTimestamp INTEGER
                    )
                """.trimIndent())
                
                // Create achievements table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        requirement INTEGER NOT NULL,
                        unlocked INTEGER NOT NULL,
                        progress INTEGER NOT NULL,
                        unlockedTimestamp INTEGER,
                        xpReward INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create daily_streak table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_streak (
                        id INTEGER PRIMARY KEY NOT NULL,
                        currentStreak INTEGER NOT NULL,
                        longestStreak INTEGER NOT NULL,
                        lastPlayedDate TEXT,
                        totalDaysPlayed INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create timed_challenges table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS timed_challenges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        gameMode TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        targetTime INTEGER NOT NULL,
                        actualTime INTEGER NOT NULL,
                        questionsAnswered INTEGER NOT NULL,
                        correctAnswers INTEGER NOT NULL,
                        wrongAttempts INTEGER NOT NULL,
                        completed INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Create multiplayer_games table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS multiplayer_games (
                        gameId TEXT PRIMARY KEY NOT NULL,
                        hostPlayerId TEXT NOT NULL,
                        hostPlayerName TEXT NOT NULL,
                        opponentPlayerId TEXT,
                        opponentPlayerName TEXT,
                        gameMode TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        status TEXT NOT NULL,
                        hostScore INTEGER NOT NULL,
                        opponentScore INTEGER NOT NULL,
                        hostCompleted INTEGER NOT NULL,
                        opponentCompleted INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        startedAt INTEGER,
                        completedAt INTEGER,
                        winnerId TEXT
                    )
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 3 to version 4.
         * Adds points system columns to high_scores table.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for points system
                database.execSQL("ALTER TABLE high_scores ADD COLUMN points INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN bonusMultiplier REAL NOT NULL DEFAULT 1.0")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN finalScore INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE high_scores ADD COLUMN isDailyChallenge INTEGER NOT NULL DEFAULT 0")
                
                // Update existing records with calculated points based on time
                // For backwards compatibility: faster time = higher points
                // Base formula: 10000 points - (timeTaken in seconds)
                database.execSQL("""
                    UPDATE high_scores 
                    SET points = MAX(0, 10000 - (timeTaken / 1000) - (wrongAttempts * 500)),
                        finalScore = MAX(0, 10000 - (timeTaken / 1000) - (wrongAttempts * 500))
                    WHERE points = 0
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 5 to version 6.
         * Fixes GroupMember table to use composite primary key (groupId, memberId)
         * to prevent duplicate members.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table with composite primary key
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS group_members_new (
                        groupId TEXT NOT NULL,
                        memberId TEXT NOT NULL,
                        memberName TEXT NOT NULL,
                        role TEXT NOT NULL,
                        joinedAt INTEGER NOT NULL,
                        totalScore INTEGER NOT NULL,
                        gamesPlayed INTEGER NOT NULL,
                        challengesWon INTEGER NOT NULL,
                        challengesLost INTEGER NOT NULL,
                        lastActiveAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        synced INTEGER NOT NULL,
                        PRIMARY KEY(groupId, memberId)
                    )
                """.trimIndent())
                
                // Copy data from old table, removing duplicates
                database.execSQL("""
                    INSERT OR REPLACE INTO group_members_new 
                    SELECT DISTINCT groupId, memberId, memberName, role, joinedAt, 
                           totalScore, gamesPlayed, challengesWon, challengesLost, 
                           lastActiveAt, isActive, synced
                    FROM group_members
                """.trimIndent())
                
                // Drop old table
                database.execSQL("DROP TABLE group_members")
                
                // Rename new table
                database.execSQL("ALTER TABLE group_members_new RENAME TO group_members")
            }
        }
        
        /**
         * Migration from version 6 to version 7.
         * Adds avatars table for mathematician avatar system with educational trivia.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create avatars table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS avatars (
                        avatarId TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        era TEXT NOT NULL,
                        category TEXT NOT NULL,
                        xpCost INTEGER NOT NULL,
                        rarity TEXT NOT NULL,
                        trivia TEXT NOT NULL,
                        contribution TEXT NOT NULL,
                        funFact TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 7 to version 8.
         * Refreshes avatars table to include updated image URLs from Wikipedia.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop and recreate avatars table to ensure fresh data with image URLs
                database.execSQL("DROP TABLE IF EXISTS avatars")
                database.execSQL("""
                    CREATE TABLE avatars (
                        avatarId TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        era TEXT NOT NULL,
                        category TEXT NOT NULL,
                        xpCost INTEGER NOT NULL,
                        rarity TEXT NOT NULL,
                        trivia TEXT NOT NULL,
                        contribution TEXT NOT NULL,
                        funFact TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 8 to version 9.
         * Adds emoji column for visual representation.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop and recreate to add emoji column
                database.execSQL("DROP TABLE IF EXISTS avatars")
                database.execSQL("""
                    CREATE TABLE avatars (
                        avatarId TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        imageUrl TEXT NOT NULL,
                        emoji TEXT NOT NULL DEFAULT '🎓',
                        era TEXT NOT NULL,
                        category TEXT NOT NULL,
                        xpCost INTEGER NOT NULL,
                        rarity TEXT NOT NULL,
                        trivia TEXT NOT NULL,
                        contribution TEXT NOT NULL,
                        funFact TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
        
        /**
         * Singleton instance of the database.
         * @Volatile ensures that this field is immediately visible to all threads.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Get the singleton instance of the database.
         * 
         * This function uses the double-checked locking pattern to ensure
         * thread safety while avoiding synchronization overhead after
         * the instance is created.
         * 
         * @param context Application context used to build the database
         * @return The singleton database instance
         */
        fun getDatabase(context: Context): AppDatabase {
            // If instance already exists, return it
            return INSTANCE ?: synchronized(this) {
                // Double-check that instance is still null inside the synchronized block
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "math_workout_database"
                )
                    // Add migrations for schema changes
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    // Fallback to destructive migration during development
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
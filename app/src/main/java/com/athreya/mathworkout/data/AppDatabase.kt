package com.athreya.mathworkout.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

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
    entities = [HighScore::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Abstract function that returns the DAO for high scores.
     * Room will implement this function automatically.
     */
    abstract fun highScoreDao(): HighScoreDao
    
    companion object {
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
                    // Add any database configurations here
                    // .addMigrations() - for database migrations
                    // .fallbackToDestructiveMigration() - recreate DB if migration fails
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}